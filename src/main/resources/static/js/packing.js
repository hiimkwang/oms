/* ============================================================
   Trạm đóng gói & quay video — chạy hoàn toàn ở trình duyệt máy client.
   - Camera quét MÃ VẬN ĐƠN (tự tạo đơn nháp trên OMS).
   - Máy quét USB quét SKU sản phẩm + serial -> thêm vào đơn.
   - Video ghép PiP, lưu thẳng vào thư mục trên máy (File System Access API).
   CSRF: layout.html đã tự chèn header cho mọi fetch.
   ============================================================ */
(function () {
  "use strict";
  const $ = id => document.getElementById(id);
  const API = "/api/v1/packing";

  const state = {
    running:false, recording:false,
    panoStream:null, qrStream:null, sameDevice:false,
    order:null,                 // {orderCode, trackingCode}
    pending:null,               // {sku,name,price} đang chờ serial
    // Ghi 2 luồng RIÊNG: toàn cảnh (pano) + cam QR. Merge thành video thứ 3 làm ở trang chi tiết đơn.
    recorderPano:null, chunksPano:[], recorderQr:null, chunksQr:[],
    recStartTs:0, autoStopTimer:null,
    rafId:null, frames:0, fpsTs:0,
    scanBusy:false, frameCounter:0, lastBox:null, lastBoxTs:0,
    lastTracking:"", mime:"", ext:"webm", audioCtx:null, dirHandle:null
  };

  /* ---------- ZXing (quét vận đơn qua camera) ---------- */
  let zxReader=null, nativeDetector=null;
  function initScanner(){
    if('BarcodeDetector' in window){
      try{ nativeDetector = new window.BarcodeDetector({formats:['code_128','qr_code','ean_13','code_39']}); }catch(e){ nativeDetector=null; }
    }
    if(!nativeDetector && window.ZXing){
      const hints = new Map();
      hints.set(ZXing.DecodeHintType.TRY_HARDER, true);
      hints.set(ZXing.DecodeHintType.POSSIBLE_FORMATS, [
        ZXing.BarcodeFormat.CODE_128, ZXing.BarcodeFormat.QR_CODE,
        ZXing.BarcodeFormat.EAN_13, ZXing.BarcodeFormat.CODE_39 ]);
      zxReader = new ZXing.MultiFormatReader();
      zxReader.setHints(hints);
    }
  }
  const scanCanvas = document.createElement('canvas');
  const scanCtx = scanCanvas.getContext('2d', {willReadFrequently:true});
  async function scanFromVideo(video, vw, vh){
    const sw = Math.min(640, vw), sh = Math.round(vh * sw / vw);
    if(sw<=0||sh<=0) return null;
    scanCanvas.width=sw; scanCanvas.height=sh;
    scanCtx.drawImage(video,0,0,sw,sh);
    if(nativeDetector){
      try{
        const codes = await nativeDetector.detect(scanCanvas);
        if(codes && codes.length){
          const c=codes[0], bb=c.boundingBox;
          return {text:c.rawValue, box:{x:bb.x,y:bb.y,w:bb.width,h:bb.height,sw,sh}};
        }
      }catch(e){}
      return null;
    }
    if(zxReader){
      try{
        const img=scanCtx.getImageData(0,0,sw,sh), d=img.data;
        const gray=new Uint8ClampedArray(sw*sh);
        for(let i=0,j=0;i<d.length;i+=4,j++) gray[j]=(d[i]*0.299+d[i+1]*0.587+d[i+2]*0.114)|0;
        const lum=new ZXing.RGBLuminanceSource(gray,sw,sh);
        const bmp=new ZXing.BinaryBitmap(new ZXing.HybridBinarizer(lum));
        const res=zxReader.decode(bmp);
        let box=null; const pts=res.getResultPoints&&res.getResultPoints();
        if(pts&&pts.length){let mnX=1e9,mnY=1e9,mxX=0,mxY=0;
          pts.forEach(p=>{const x=p.getX(),y=p.getY();if(x<mnX)mnX=x;if(y<mnY)mnY=y;if(x>mxX)mxX=x;if(y>mxY)mxY=y;});
          box={x:mnX,y:mnY,w:mxX-mnX,h:mxY-mnY,sw,sh};}
        return {text:res.getText(), box};
      }catch(e){}finally{ if(zxReader.reset) zxReader.reset(); }
    }
    return null;
  }

  /* ---------- Chẩn đoán + thiết bị ---------- */
  function diag(html){ $('diag').innerHTML += html + "\n"; }
  function diagReset(){ $('diag').innerHTML=""; }
  async function listDevices(){
    diagReset();
    diag("• Secure context: " + (window.isSecureContext?"<b>CÓ</b>":"<span class='bad'>KHÔNG</span>"));
    if(!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia){
      diag("<span class='bad'>• Trình duyệt không hỗ trợ camera. Dùng Chrome/Edge.</span>"); return;
    }
    let granted=false;
    try{ const t=await navigator.mediaDevices.getUserMedia({video:true}); t.getTracks().forEach(x=>x.stop()); granted=true;
      diag("• Cấp quyền camera: <b>OK</b>");
    }catch(e){
      diag("<span class='bad'>• getUserMedia: "+e.name+" — "+e.message+"</span>");
      if(e.name==='NotAllowedError') diag("  → Bị chặn. Mở Windows: Settings ▸ Privacy ▸ Camera, bật quyền cho trình duyệt.");
      else if(e.name==='NotFoundError') diag("  → Không thấy webcam. Cắm camera USB rồi thử lại.");
      else if(e.name==='NotReadableError') diag("  → Camera đang bị app khác chiếm (Zoom/Teams/GuardCam…).");
    }
    let devs=[];
    try{ devs=(await navigator.mediaDevices.enumerateDevices()).filter(d=>d.kind==='videoinput'); }catch(e){}
    diag("• Số camera: <b>"+devs.length+"</b>");
    const camPano=$('camPano'), camQr=$('camQr');
    camPano.innerHTML=''; camQr.innerHTML='';
    const none=document.createElement('option'); none.value=''; none.textContent='— Không dùng —';
    camQr.appendChild(none);
    devs.forEach((d,i)=>{
      const label=d.label||('Camera '+(i+1));
      const o1=document.createElement('option'); o1.value=d.deviceId; o1.textContent=label; camPano.appendChild(o1);
      const o2=document.createElement('option'); o2.value=d.deviceId; o2.textContent=label; camQr.appendChild(o2);
    });
    if(devs[0]) camPano.value=devs[0].deviceId;
    if(devs[1]) camQr.value=devs[1].deviceId;
    applySettings(); // khôi phục lựa chọn đã lưu (đè auto-select nếu thiết bị còn tồn tại)
    if(devs.length>0) diag(granted?"✅ Sẵn sàng.":"⚠ Bấm 'Cấp quyền' lần nữa.");
  }

  function parseRes(v){ const [w,h]=v.split('x').map(Number); return {w,h}; }
  function pickMime(){
    const c=[['video/mp4;codecs=h264,aac','mp4'],['video/mp4','mp4'],
      ['video/webm;codecs=vp9','webm'],['video/webm;codecs=vp8','webm'],['video/webm','webm']];
    for(const [m,ext] of c){ if(window.MediaRecorder && MediaRecorder.isTypeSupported(m)) return {mime:m,ext}; }
    return {mime:'',ext:'webm'};
  }

  /* ---------- Camera ---------- */
  const panoCanvas=$('panoCanvas'), pctx=panoCanvas.getContext('2d');
  const qrCanvas=$('qrCanvas'), qctx=qrCanvas.getContext('2d');
  const recCanvas=document.createElement('canvas'); const rctx=recCanvas.getContext('2d');
  let vPano, vQr;

  // Mở 1 camera theo deviceId
  function openStream(deviceId, res){
    return navigator.mediaDevices.getUserMedia({video:{deviceId:{exact:deviceId},width:{ideal:res.w},height:{ideal:res.h},frameRate:{ideal:30}},audio:false});
  }
  // Đợi tới khi <video> thực sự ra hình (videoWidth>0) hoặc hết thời gian
  function waitVideoReady(v, ms){
    return new Promise(resolve=>{
      if(v.videoWidth>0){ resolve(true); return; }
      let done=false;
      const finish=ok=>{ if(done) return; done=true;
        v.removeEventListener('loadedmetadata',onEvt); v.removeEventListener('playing',onEvt);
        clearInterval(t); clearTimeout(to); resolve(ok); };
      const onEvt=()=>{ if(v.videoWidth>0) finish(true); };
      v.addEventListener('loadedmetadata',onEvt); v.addEventListener('playing',onEvt);
      const t=setInterval(()=>{ if(v.videoWidth>0) finish(true); }, 50);
      const to=setTimeout(()=>finish(v.videoWidth>0), ms);
    });
  }
  // Gắn stream vào <video>, play (thử lại 1 lần nếu play lỗi), rồi đợi ra hình
  async function attachReady(v, stream, ms){
    v.srcObject=stream;
    try{ await v.play(); }catch(e){ try{ await v.play(); }catch(_){} }
    return waitVideoReady(v, ms);
  }

  async function startSystem(){
    vPano=vPano||$('vPano'); vQr=vQr||$('vQr');
    const panoId=$('camPano').value, qrId=$('camQr').value;
    if(!panoId){ await listDevices(); if(!$('camPano').value){ diag("<span class='bad'>Chưa có camera để bắt đầu.</span>"); return; } }
    // Đọc lại id sau listDevices (phòng khi vừa auto-chọn thiết bị)
    const finalPanoId=$('camPano').value, finalQrId=$('camQr').value;
    const rp=parseRes($('resPano').value), rq=parseRes($('resQr').value);
    state.sameDevice = finalQrId && finalQrId===finalPanoId;
    try{
      // 1) Cam TOÀN CẢNH
      state.panoStream=await openStream(finalPanoId, rp);
      await attachReady(vPano, state.panoStream, 4000);

      // 2) Cam QR (nếu có, và khác thiết bị): đợi ra hình, THỬ LẠI 1 LẦN nếu chưa lên
      if(finalQrId && !state.sameDevice){
        state.qrStream=await openStream(finalQrId, rq);
        let ok=await attachReady(vQr, state.qrStream, 3000);
        if(!ok){
          // Warm-up chưa kịp -> đóng và mở lại 1 lần
          try{ state.qrStream.getTracks().forEach(t=>t.stop()); }catch(e){}
          await new Promise(r=>setTimeout(r,350));
          state.qrStream=await openStream(finalQrId, rq);
          ok=await attachReady(vQr, state.qrStream, 4000);
        }
        if(!ok){ diag("<span class='bad'>Cam QR chưa lên hình. Thử bấm 'Tắt camera' rồi 'Bắt đầu' lại.</span>"); }
      }
    }catch(e){ diag("<span class='bad'>Không mở được camera: "+e.message+"</span>"); return; }

    $('qrFloat').style.display=(finalQrId && !state.sameDevice)?'block':'none';
    const {mime,ext}=pickMime(); state.mime=mime; state.ext=ext;
    $('codecName').textContent=mime?mime.split(';')[0].replace('video/',''):'mặc định';

    state.running=true;
    setState('ready','SẴN SÀNG QUÉT VẬN ĐƠN');
    $('scanDot').className='eye live'; $('scanText').textContent='đang dò vận đơn…';
    $('startBtn').disabled=true; $('stopSysBtn').disabled=false;
    state.fpsTs=performance.now(); state.frames=0;
    renderLoop();
  }

  async function stopSystem(){
    // Nếu đang có đơn dở -> hoàn tất (lưu video) trước khi tắt, tránh mất video
    if(state.order){ await finishOrder(); }
    state.running=false;
    cancelAnimationFrame(state.rafId);
    [state.panoStream,state.qrStream].forEach(s=>{ if(s) s.getTracks().forEach(t=>t.stop()); });
    state.panoStream=state.qrStream=null;
    setState('idle','CHƯA KẾT NỐI');
    $('overlayStatus').textContent='Đã tắt camera';
    $('scanDot').className='eye'; $('scanText').textContent='đang tắt';
    $('startBtn').disabled=false; $('stopSysBtn').disabled=true; $('finishBtn').disabled=true;
    $('skuInput').disabled=true; $('serialInput').disabled=true; $('saveRecipientBtn').disabled=true;
    pctx.clearRect(0,0,panoCanvas.width,panoCanvas.height);
    $('qrFloat').style.display='none';
  }

  function fmtTime(d){const p=n=>String(n).padStart(2,'0');
    return `${p(d.getDate())}/${p(d.getMonth()+1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;}

  function drawOverlay(ctx,w,h){
    const fs=Math.round(h/24);
    ctx.font=`bold ${fs}px Segoe UI,sans-serif`; ctx.textBaseline='top';
    ctx.lineWidth=Math.max(2,fs/8); ctx.strokeStyle='rgba(0,0,0,.7)';
    const t=fmtTime(new Date());
    ctx.strokeText(t,18,16); ctx.fillStyle='#ffe600'; ctx.fillText(t,18,16);
    if(state.recording && state.order){
      const o='ĐƠN: '+(state.order.trackingCode||state.order.orderCode);
      ctx.strokeText(o,18,16+fs*1.3); ctx.fillStyle='#ff5b5b'; ctx.fillText(o,18,16+fs*1.3);
      ctx.fillStyle='#ef4444'; ctx.beginPath(); ctx.arc(w-40,40,12,0,7); ctx.fill();
      ctx.fillStyle='#fff'; ctx.font=`bold ${Math.round(fs*0.8)}px sans-serif`; ctx.fillText('REC',w-112,28);
    }
  }
  function drawQrInto(ctx,cw,ch){
    if($('rotateQr').checked){ ctx.save(); ctx.translate(cw/2,ch/2); ctx.rotate(-Math.PI/2);
      ctx.drawImage(vQr,-ch/2,-cw/2,ch,cw); ctx.restore();
    } else ctx.drawImage(vQr,0,0,cw,ch);
  }

  function renderLoop(){
    if(!state.running) return;
    state.rafId=requestAnimationFrame(renderLoop);
    if(!vPano || vPano.videoWidth===0) return;
    const pw=vPano.videoWidth, ph=vPano.videoHeight;
    if(panoCanvas.width!==pw){ panoCanvas.width=pw; panoCanvas.height=ph; recCanvas.width=pw; recCanvas.height=ph; }
    pctx.drawImage(vPano,0,0,pw,ph); drawOverlay(pctx,pw,ph);

    const useQr=state.qrStream && vQr.videoWidth>0;
    if(useQr){
      const rot=$('rotateQr').checked;
      const cw=rot?vQr.videoHeight:vQr.videoWidth, ch=rot?vQr.videoWidth:vQr.videoHeight;
      if(qrCanvas.width!==cw){qrCanvas.width=cw; qrCanvas.height=ch;}
      drawQrInto(qctx,cw,ch);
      if(state.lastBox && performance.now()-state.lastBoxTs<600){
        const b=state.lastBox;
        const vw=vQr.videoWidth, vh=vQr.videoHeight;
        const sxv=vw/b.sw, syv=vh/b.sh;                 // scan-space -> video-space
        const x0=b.x*sxv, y0=b.y*syv, x1=(b.x+b.w)*sxv, y1=(b.y+b.h)*syv;
        let rx,ry,rw,rh;
        if(rot){ rx=y0; ry=vw-x1; rw=y1-y0; rh=x1-x0; }  // video (px,py) -> canvas (py, vw-px)
        else { rx=x0; ry=y0; rw=x1-x0; rh=y1-y0; }
        qctx.strokeStyle='#22c55e'; qctx.lineWidth=Math.max(3,cw/200);
        qctx.strokeRect(rx,ry,rw,rh);
      }
    }
    // (Không còn ghi canvas ghép PiP tại đây: ta ghi thẳng 2 luồng panoCanvas + qrCanvas,
    //  và merge thành video thứ 3 ở trang chi tiết đơn khi cần.)
    state.frames++; const now=performance.now();
    if(now-state.fpsTs>=1000){ $('fps').textContent=state.frames; state.frames=0; state.fpsTs=now; }
    if(state.recording){ const s=Math.floor((Date.now()-state.recStartTs)/1000);
      $('recTime').textContent=`${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`; }

    state.frameCounter++;
    if(!state.scanBusy && state.frameCounter%6===0){
      const sv=useQr?vQr:vPano, svw=useQr?vQr.videoWidth:pw, svh=useQr?vQr.videoHeight:ph;
      state.scanBusy=true;
      scanFromVideo(sv,svw,svh).then(r=>{
        if(r&&r.text){ if(r.box){state.lastBox=r.box; state.lastBoxTs=performance.now();} onTrackingScanned(r.text.trim()); }
      }).finally(()=>{ state.scanBusy=false; });
    }
  }

  /* ---------- Quay video (2 luồng riêng: toàn cảnh + cam QR) ---------- */
  const VIDEO_BITRATE = 6_000_000; // ~6 Mbps: nét hơn hẳn mức 4 Mbps cũ
  function newRecorder(canvasEl, chunksArr){
    const stream=canvasEl.captureStream(30);
    let rec;
    try{ rec=new MediaRecorder(stream, state.mime?{mimeType:state.mime,videoBitsPerSecond:VIDEO_BITRATE}:{videoBitsPerSecond:VIDEO_BITRATE}); }
    catch(e){ rec=new MediaRecorder(stream); }
    rec.ondataavailable=e=>{ if(e.data.size>0) chunksArr.push(e.data); };
    rec.start(1000);
    return rec;
  }
  function startRecording(){
    state.chunksPano=[]; state.chunksQr=[];
    state.recorderPano=newRecorder(panoCanvas, state.chunksPano);
    // Chỉ ghi cam QR riêng khi có camera QR thực sự đang chạy
    const useQr = state.qrStream && vQr && vQr.videoWidth>0;
    state.recorderQr = useQr ? newRecorder(qrCanvas, state.chunksQr) : null;
    state.recording=true; state.recStartTs=Date.now();
    const sec=parseInt($('autoStop').value,10);
    if(sec>0){ clearTimeout(state.autoStopTimer);
      state.autoStopTimer=setTimeout(()=>{ if(state.recording) finishOrder(); }, sec*1000); }
  }
  function stopOne(rec, chunksArr){
    return new Promise(resolve=>{
      if(!rec){ resolve(null); return; }
      rec.onstop=()=>resolve(chunksArr.length ? new Blob(chunksArr,{type:state.mime||'video/webm'}) : null);
      try{ rec.stop(); }catch(e){ resolve(null); }
    });
  }
  // Trả về {pano:Blob|null, qr:Blob|null}
  async function stopRecording(){
    if(!state.recording){ return {pano:null, qr:null}; }
    state.recording=false; clearTimeout(state.autoStopTimer);
    const [pano, qr] = await Promise.all([
      stopOne(state.recorderPano, state.chunksPano),
      stopOne(state.recorderQr, state.chunksQr)
    ]);
    state.recorderPano=state.recorderQr=null;
    return {pano, qr};
  }

  /* ---------- Lưu video vào thư mục máy ---------- */
  async function saveVideoToFolder(blob, filename){
    if(!blob){ return null; }
    if(state.dirHandle){
      try{
        const perm=await state.dirHandle.requestPermission({mode:'readwrite'});
        if(perm==='granted'){
          const fh=await state.dirHandle.getFileHandle(filename,{create:true});
          const w=await fh.createWritable(); await w.write(blob); await w.close();
          return state.dirHandle.name + '/' + filename;
        }
      }catch(e){ console.warn('Ghi thư mục lỗi, chuyển sang tải về:',e); }
    }
    // Fallback: tải về thư mục Downloads
    const url=URL.createObjectURL(blob); const a=document.createElement('a');
    a.href=url; a.download=filename; document.body.appendChild(a); a.click(); a.remove();
    setTimeout(()=>URL.revokeObjectURL(url),10000);
    return 'Downloads/'+filename;
  }

  /* ---------- API OMS ---------- */
  async function apiJSON(url, opts){
    const r=await fetch(url, Object.assign({headers:{'Content-Type':'application/json'}}, opts));
    if(!r.ok){ let msg='Lỗi '+r.status; try{const j=await r.json(); msg=j.message||j.error||msg;}catch(e){} throw new Error(msg); }
    return r.status===204?null:r.json();
  }

  const STATUS_VI={DRAFT:'Nháp',CREATED:'Khởi tạo',CONFIRMED:'Đã xác nhận',PROCESSING:'Đang xử lý',
    SHIPPING:'Đang giao',COMPLETED:'Hoàn thành',CANCELLED:'Đã hủy',RETURNED:'Hoàn trả'};

  async function onTrackingScanned(code){
    code=(code||'').trim();
    if(!code || code.length<5) return;
    if(state.order && code===state.order.trackingCode) return;   // đang đóng chính đơn này
    if(code===state.lastTracking) return;                        // đã xử lý/cảnh báo mã này rồi -> bỏ qua (chống loop)
    if(state.pendingTrack===code) return;
    state.pendingTrack=code;
    await startTrackingFlow(code, true);
    state.pendingTrack=null;
  }

  // Luồng xử lý 1 mã vận đơn (dùng cho cả camera lẫn nút Tạo đơn / nhập tay)
  async function startTrackingFlow(code, fromCamera){
    code=(code||'').trim(); if(code.length<3) return;
    if(state.order && code===state.order.trackingCode) return;
    if(!state.running){ toast('Hãy bấm "Bắt đầu hệ thống" (bật camera) trước khi tạo đơn.','err'); return; }
    // 1. Kiểm tra vận đơn đã có đơn chưa
    let ex=null;
    try{ ex=await apiJSON(API+'/orders/by-tracking/'+encodeURIComponent(code)); }catch(e){}
    if(ex && ex.exists){
      // ĐÃ CÓ ĐƠN -> chỉ cảnh báo MỘT LẦN, KHÔNG tạo/không quay/không điền
      state.lastTracking=code;   // nhớ mã đã cảnh báo -> chống bắn toast liên tục
      errBeep();
      $('trackingInput').value=code;
      const lbl=STATUS_VI[ex.status]||ex.status||'';
      const link=$('openOrderLink'); link.href='/ui/orders/detail/'+ex.orderCode; link.classList.remove('d-none');
      $('orderCodeBox').textContent=ex.orderCode;
      toast('Vận đơn này đã có đơn '+ex.orderCode+' ('+lbl+') — bấm "Mở đơn" để xem, KHÔNG tạo lại.','err');
      return;
    }
    // 2. Chưa có đơn
    if(fromCamera && !$('autoCreate').checked){   // camera quét nhưng tắt tự tạo -> chỉ điền, chờ bấm nút
      state.lastTracking=code; $('trackingInput').value=code;
      toast('Đã quét vận đơn '+code+' — bấm "Tạo đơn"'); return;
    }
    if(fromCamera) beep();
    if(state.order){ await finishOrder(); }   // đóng đơn đang dở trước khi mở đơn mới
    $('trackingInput').value=code;
    await createDraft(code);
  }

  async function createDraft(trackingCode){
    try{
      const data=await apiJSON(API+'/orders',{method:'POST',body:JSON.stringify({trackingCode})});
      state.order={orderCode:data.orderCode, trackingCode:trackingCode};
      state.lastTracking=trackingCode;
      $('orderCodeBox').textContent=data.orderCode;
      $('trackingInput').value=trackingCode||'';
      const link=$('openOrderLink'); link.href='/ui/orders/detail/'+data.orderCode; link.classList.remove('d-none');
      setState('rec','ĐANG GHI');
      $('skuInput').disabled=false; $('serialInput').disabled=false; $('finishBtn').disabled=false; $('saveRecipientBtn').disabled=false;
      renderItems(data);
      if(state.running && !state.recording) startRecording();
      toast('Đã tạo đơn '+data.orderCode+' • đang ghi hình','ok');
      $('skuInput').focus();
    }catch(e){ toast('Không tạo được đơn: '+e.message,'err'); }
  }

  async function finishOrder(){
    if(!state.order){ return; }
    // Flush ô đang gõ (vd giá vừa sửa) để lưu trước khi hoàn tất
    if(document.activeElement && document.activeElement.blur) document.activeElement.blur();
    // Tự lưu thông tin khách nếu người dùng đã nhập mà chưa bấm Lưu
    const nm=$('recipientName').value.trim(), ph=$('recipientPhone').value.trim(), ad=$('recipientAddr').value.trim();
    if(nm||ph||ad){ try{ await apiJSON(API+'/orders/'+state.order.orderCode+'/recipient',
      {method:'PUT',body:JSON.stringify({recipientName:nm,recipientPhone:ph,shippingAddress:ad})}); }catch(e){} }
    const order=state.order; state.order=null; state.pending=null; $('pendingBox').style.display='none';
    const {pano, qr}=await stopRecording();
    const base=(order.trackingCode||order.orderCode).replace(/[^\w.-]/g,'_')+'_'+order.orderCode;
    let panoName=null, qrName=null;
    if(pano){ panoName=base+'_pano.'+state.ext; await saveVideoToFolder(pano, panoName); }
    if(qr){ qrName=base+'_qr.'+state.ext; await saveVideoToFolder(qr, qrName); }
    // Lưu đường dẫn dạng JSON (v2): tên 2 file + thư mục, để trang chi tiết đọc lại & merge.
    if(panoName || qrName){
      const meta={ v:2, dir:(state.dirHandle && state.dirHandle.name) || 'Downloads', pano:panoName, qr:qrName, ext:state.ext };
      try{ await apiJSON(API+'/orders/'+order.orderCode+'/video',{method:'POST',body:JSON.stringify({videoPath:JSON.stringify(meta)})}); }catch(e){}
    }
    if(state.running){ setState('ready','SẴN SÀNG QUÉT VẬN ĐƠN'); }
    $('skuInput').disabled=true; $('serialInput').disabled=true; $('finishBtn').disabled=true; $('saveRecipientBtn').disabled=true;
    $('recTime').textContent='00:00';
    resetOrderPanel();
    const savedInfo=[panoName&&'toàn cảnh', qrName&&'cam QR'].filter(Boolean).join(' + ');
    toast('Đã lưu đơn '+order.orderCode+(savedInfo?' • video: '+savedInfo:''),'ok');
  }

  /* ---------- Quét sản phẩm + serial ---------- */
  async function onSkuScanned(sku){
    sku=(sku||'').trim(); if(!sku) return;
    if(!state.order){ toast('Chưa có đơn. Hãy quét vận đơn trước.','err'); errBeep(); return; }
    if($('requireSerial').checked){
      // tra cứu để hiển thị tên, chờ serial
      try{
        const v=await apiJSON(API+'/lookup/'+encodeURIComponent(sku));
        state.pending={sku:v.sku, name:v.name, price:v.price};
        $('pendingBox').style.display='block';
        $('pendingBox').innerHTML='⏳ Chờ serial cho: <b>'+v.name+'</b> ('+v.sku+'). Quét serial hoặc bấm Enter ở ô serial để bỏ qua.';
        beep(); $('skuInput').value=''; $('serialInput').focus();
      }catch(e){ errBeep(); $('pendingBox').style.display='block'; $('pendingBox').innerHTML='<span class="text-danger">✗ '+e.message+'</span>'; }
    }else{
      await addItem(sku, null);
      $('skuInput').value=''; $('skuInput').focus();
    }
  }
  async function onSerialScanned(serial){
    serial=(serial||'').trim();
    if(!state.pending){ // không có sản phẩm chờ -> bỏ qua
      $('serialInput').value=''; $('skuInput').focus(); return; }
    const sku=state.pending.sku;
    state.pending=null; $('pendingBox').style.display='none';
    await addItem(sku, serial||null);
    $('serialInput').value=''; $('skuInput').value=''; $('skuInput').focus();
  }
  async function addItem(sku, serialNumber){
    try{
      const data=await apiJSON(API+'/orders/'+state.order.orderCode+'/items',
        {method:'POST',body:JSON.stringify({sku, serialNumber, quantity:1})});
      renderItems(data); beep();
    }catch(e){ errBeep(); toast('Không thêm được sản phẩm: '+e.message,'err'); }
  }

  function fmtInt(n){ return Number(n||0).toLocaleString('vi-VN'); }
  function renderItems(data){
    const body=$('itemsBody');
    if(!data.items || data.items.length===0){
      body.innerHTML='<div class="pk-empty"><i class="bi bi-inbox"></i>Chưa có sản phẩm — quét mã để thêm</div>';
    }else{
      body.innerHTML=data.items.map(it=>`<div class="pk-item" data-id="${it.id}">
        <span class="nm">${esc(it.productName)}</span>
        <span class="qty">${it.quantity}</span>
        <span class="sn">${esc(it.serialNumber||'—')}</span>
        <input class="pk-price-in" type="text" inputmode="numeric" data-id="${it.id}" value="${fmtInt(it.unitPrice)}" title="Sửa đơn giá (Enter)">
        <button class="pk-del" data-id="${it.id}" title="Xoá dòng">&times;</button>
      </div>`).join('');
    }
    $('totalBox').textContent=vnd(data.totalAmount);
  }
  async function updateItemPrice(id, val){
    if(!state.order || !id) return;
    const price=parseInt(String(val).replace(/[^\d]/g,''))||0;
    try{ const data=await apiJSON(API+'/orders/'+state.order.orderCode+'/items/'+id,{method:'PUT',body:JSON.stringify({unitPrice:price})}); renderItems(data); }
    catch(e){ toast('Không sửa được giá: '+e.message,'err'); }
  }
  async function removeItemLine(id){
    if(!state.order || !id) return;
    try{ const data=await apiJSON(API+'/orders/'+state.order.orderCode+'/items/'+id,{method:'DELETE'}); renderItems(data); beep(); }
    catch(e){ toast('Không xoá được dòng: '+e.message,'err'); }
  }

  async function saveRecipient(){
    if(!state.order) return;
    try{
      const data=await apiJSON(API+'/orders/'+state.order.orderCode+'/recipient',{method:'PUT',body:JSON.stringify({
        recipientName:$('recipientName').value, recipientPhone:$('recipientPhone').value,
        shippingAddress:$('recipientAddr').value })});
      toast('Đã lưu thông tin khách.','ok'); renderItems(data);
    }catch(e){ toast('Lỗi lưu người nhận: '+e.message,'err'); }
  }

  /* ---------- Lưu/khôi phục cấu hình (localStorage) ---------- */
  const PK_KEYS=['camPano','camQr','resPano','resQr','autoStop','rotateQr','mergePip','beepOn','autoCreate','requireSerial'];
  function saveSettings(){
    const o={};
    PK_KEYS.forEach(id=>{ const el=$(id); if(!el) return; o[id]=(el.type==='checkbox')?el.checked:el.value; });
    try{ localStorage.setItem('pk-settings', JSON.stringify(o)); }catch(e){}
  }
  function applySettings(){
    let o={}; try{ o=JSON.parse(localStorage.getItem('pk-settings')||'{}'); }catch(e){}
    PK_KEYS.forEach(id=>{ if(!(id in o)) return; const el=$(id); if(!el) return;
      if(el.type==='checkbox'){ el.checked=!!o[id]; }
      else if(el.tagName==='SELECT'){ if([...el.options].some(op=>op.value===o[id])) el.value=o[id]; }
      else { el.value=o[id]; }
    });
  }
  function bindSettingsPersist(){ PK_KEYS.forEach(id=>{ const el=$(id); if(el) el.addEventListener('change', saveSettings); }); }

  /* ---------- Thư mục lưu (IndexedDB) ---------- */
  function idb(){ return new Promise((res,rej)=>{ const r=indexedDB.open('pk-store',1);
    r.onupgradeneeded=()=>r.result.createObjectStore('kv'); r.onsuccess=()=>res(r.result); r.onerror=()=>rej(r.error); }); }
  async function idbSet(k,v){ const db=await idb(); return new Promise((res,rej)=>{const tx=db.transaction('kv','readwrite');
    tx.objectStore('kv').put(v,k); tx.oncomplete=()=>res(); tx.onerror=()=>rej(tx.error); }); }
  async function idbGet(k){ const db=await idb(); return new Promise((res,rej)=>{const tx=db.transaction('kv','readonly');
    const rq=tx.objectStore('kv').get(k); rq.onsuccess=()=>res(rq.result); rq.onerror=()=>rej(rq.error); }); }

  async function pickFolder(){
    if(!window.showDirectoryPicker){ alert('Trình duyệt không hỗ trợ chọn thư mục (cần Chrome/Edge). Video sẽ tải về Downloads.'); return; }
    try{
      const h=await window.showDirectoryPicker({mode:'readwrite', startIn:'documents'});
      state.dirHandle=h; $('folderName').value=h.name;
      await idbSet('dirHandle',h);
    }catch(e){
      if(e && e.name==='AbortError') return; // người dùng bấm Hủy
      alert('Không chọn được thư mục này (trình duyệt chặn ổ đĩa gốc và thư mục hệ thống như C:\\, Windows, Program Files).\n'
        +'Hãy tạo một thư mục thường, ví dụ: Documents\\VideoDongGoi hoặc D:\\VideoDongGoi, rồi chọn nó.');
    }
  }
  async function restoreFolder(){
    try{ const h=await idbGet('dirHandle');
      if(h){ state.dirHandle=h; $('folderName').value=h.name+' (xác nhận lại khi lưu)'; }
    }catch(e){}
  }

  /* ---------- Tiện ích ---------- */
  function esc(s){ return (s==null?'':String(s)).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }
  function vnd(n){ n=Number(n||0); return n.toLocaleString('vi-VN')+'₫'; }

  // Máy trạng thái: 'idle' | 'ready' | 'rec'
  function setState(kind, text){
    const map={idle:'is-idle',ready:'is-ready',rec:'is-rec'};
    const el=$('sysStatus'); el.className='pk-state '+(map[kind]||'is-idle');
    el.innerHTML='<span class="d"></span>'+esc(text);
    if($('overlayStatus')) $('overlayStatus').textContent=text;
    const badge=$('overlayBadge'); if(badge) badge.classList.toggle('is-rec', kind==='rec');
    const v=$('pkVideo'); if(v) v.classList.toggle('recording', kind==='rec');
  }
  function toast(msg, kind){
    const wrap=$('pkToasts'); if(!wrap){ console.log('[packing]',msg); return; }
    const t=document.createElement('div'); t.className='pk-toast '+(kind||'');
    const ic=kind==='ok'?'bi-check-circle-fill':(kind==='err'?'bi-exclamation-triangle-fill':'bi-info-circle-fill');
    t.innerHTML='<i class="bi '+ic+'"></i><span></span>'; t.querySelector('span').textContent=msg;
    wrap.appendChild(t);
    setTimeout(()=>{ t.style.transition='opacity .3s'; t.style.opacity='0'; setTimeout(()=>t.remove(),300); }, 3800);
  }
  function resetOrderPanel(){
    $('trackingInput').value=''; $('orderCodeBox').textContent='chưa có';
    $('recipientName').value=''; $('recipientPhone').value=''; $('recipientAddr').value='';
    $('pendingBox').style.display='none'; state.pending=null;
    renderItems({items:[],totalAmount:0});
    $('openOrderLink').classList.add('d-none');
  }
  function beep(){ if(!$('beepOn').checked) return; tone(1800,0.12); }
  function errBeep(){ tone(380,0.22); }
  function tone(freq,dur){ try{ state.audioCtx=state.audioCtx||new (window.AudioContext||window.webkitAudioContext)();
    const ac=state.audioCtx,o=ac.createOscillator(),g=ac.createGain(); o.type='sine'; o.frequency.value=freq;
    o.connect(g); g.connect(ac.destination); g.gain.setValueAtTime(0.25,ac.currentTime);
    g.gain.exponentialRampToValueAtTime(0.0001,ac.currentTime+dur); o.start(); o.stop(ac.currentTime+dur);}catch(e){} }

  /* ---------- Đọc phím VẬT LÝ để chống bộ gõ tiếng Việt (Telex biến W->Ư...) ---------- */
  // Máy quét mã (DS4308...) gõ như bàn phím; nếu bật bộ gõ TV, ký tự bị biến đổi.
  // Ta dựng chuỗi từ event.code (phím vật lý) nên không phụ thuộc bộ gõ.
  function scanChar(e){
    const c=e.code;
    let m=/^Key([A-Z])$/.exec(c); if(m) return e.shiftKey?m[1]:m[1].toLowerCase();
    m=/^Digit([0-9])$/.exec(c); if(m) return e.shiftKey?')!@#$%^&*('[+m[1]]:m[1];
    m=/^Numpad([0-9])$/.exec(c); if(m) return m[1];
    const map={Minus:['-','_'],Equal:['=','+'],Period:['.','>'],Comma:[',','<'],Slash:['/','?'],
      Backslash:['\\','|'],Semicolon:[';',':'],Quote:["'",'"'],BracketLeft:['[','{'],BracketRight:[']','}'],
      NumpadDecimal:['.','.'],NumpadSubtract:['-','-'],NumpadAdd:['+','+'],NumpadDivide:['/','/'],NumpadMultiply:['*','*'],Space:[' ',' ']};
    if(map[c]) return e.shiftKey?map[c][1]:map[c][0];
    return null;
  }
  // Giải-Telex: đưa ký tự tiếng Việt do bộ gõ tạo về ASCII gốc (W->Ư biến ngược Ư->W, aa->â biến ngược â->aa...).
  function reverseTelexChar(ch){
    if(ch==='đ') return 'dd';
    if(ch==='Đ') return 'DD';
    const nfd=ch.normalize('NFD'); const base=nfd[0]; const marks=nfd.slice(1);
    if(!/[a-zA-Z]/.test(base) || marks.length===0) return ch;
    const has=m=>marks.indexOf(m)!==-1;
    let tone='';
    if(has('́')) tone='s'; else if(has('̀')) tone='f'; else if(has('̉')) tone='r';
    else if(has('̃')) tone='x'; else if(has('̣')) tone='j';
    const lower=base.toLowerCase(); let keys;
    if(has('̛')){ keys = lower==='o' ? 'ow' : (lower==='u' ? 'w' : base); }
    else if(has('̂')){ keys=base+base; }
    else if(has('̆')){ keys=base+'w'; }
    else keys=base;
    if(base===base.toUpperCase()){ keys=keys.toUpperCase(); tone=tone.toUpperCase(); }
    return keys+tone;
  }
  function deTelex(s){ if(!s) return s; let out=''; for(const ch of s){ out += ch.charCodeAt(0)<128 ? ch : reverseTelexChar(ch); } return out; }

  function attachScan(el, onEnter){
    el.setAttribute('autocomplete','off'); el.setAttribute('spellcheck','false');
    function sanitize(){
      const v=el.value, c=deTelex(v);
      if(c!==v){ const atEnd=el.selectionStart===v.length && el.selectionEnd===v.length;
        el.value=c; if(atEnd){ try{ el.selectionStart=el.selectionEnd=c.length; }catch(e){} } }
    }
    el.addEventListener('input', e=>{ if(!e.isComposing) sanitize(); });
    el.addEventListener('compositionend', sanitize);
    el.addEventListener('keydown', e=>{
      if(e.key==='Enter'){ sanitize(); e.preventDefault(); onEnter(el.value); }
    });
    el.addEventListener('blur', sanitize);
  }

  /* ---------- Sự kiện ---------- */
  function bind(){
    $('startBtn').onclick=startSystem;
    $('stopSysBtn').onclick=stopSystem;
    $('finishBtn').onclick=()=>finishOrder();
    $('grantBtn').onclick=listDevices;
    $('pickFolderBtn').onclick=pickFolder;
    $('saveRecipientBtn').onclick=saveRecipient;
    attachScan($('trackingInput'), v=>{ const c=(v||'').trim(); if(c) startTrackingFlow(c,false); });
    attachScan($('skuInput'), v=>onSkuScanned(v));
    attachScan($('serialInput'), v=>onSerialScanned(v));
    $('itemsBody').addEventListener('change', e=>{ if(e.target.classList && e.target.classList.contains('pk-price-in')) updateItemPrice(e.target.dataset.id, e.target.value); });
    $('itemsBody').addEventListener('keydown', e=>{ if(e.key==='Enter' && e.target.classList && e.target.classList.contains('pk-price-in')){ e.preventDefault(); e.target.blur(); } });
    $('itemsBody').addEventListener('click', e=>{ const b=e.target.closest && e.target.closest('.pk-del'); if(b) removeItemLine(b.dataset.id); });
    bindSettingsPersist();
  }

  document.addEventListener('DOMContentLoaded', ()=>{
    if(!$('startBtn')) return;
    initScanner(); bind(); restoreFolder();
    const pm=pickMime(); $('codecName').textContent=pm.mime?pm.mime.split(';')[0].replace('video/',''):'—';
    // thử liệt kê camera sẵn (tên sẽ ẩn cho tới khi cấp quyền)
    if(navigator.mediaDevices && navigator.mediaDevices.enumerateDevices){ listDevices().catch(()=>{}); }
  });
})();
