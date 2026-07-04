/* ============================================================
   Xem & ghép video đóng gói ở TRANG CHI TIẾT ĐƠN HÀNG.
   - Video lưu ở MÁY LOCAL (File System Access API). Ta đọc lại thư mục đã chọn
     lúc đóng gói (lưu trong IndexedDB 'pk-store') để phát 2 video: toàn cảnh + cam QR.
   - Nút "Ghép video": tổng hợp (PiP) 2 video thành video thứ 3, lưu vào cùng thư mục.
   - Fix lỗi KHÔNG TUA ĐƯỢC của WebM (MediaRecorder không ghi duration): dùng
     "duration hack" (tua tới cuối rồi về đầu) để buộc trình duyệt tính lại độ dài.
   ============================================================ */
(function () {
  "use strict";
  const API = "/api/v1/packing";

  /* ---------- IndexedDB (cùng store với trang đóng gói) ---------- */
  function idb() {
    return new Promise((res, rej) => {
      const r = indexedDB.open('pk-store', 1);
      r.onupgradeneeded = () => r.result.createObjectStore('kv'); r.onsuccess = () => res(r.result); r.onerror = () => rej(r.error);
    });
  }
  async function idbGet(k) {
    const db = await idb(); return new Promise((res, rej) => {
      const tx = db.transaction('kv', 'readonly');
      const rq = tx.objectStore('kv').get(k); rq.onsuccess = () => res(rq.result); rq.onerror = () => rej(rq.error);
    });
  }
  async function idbSet(k, v) {
    const db = await idb(); return new Promise((res, rej) => {
      const tx = db.transaction('kv', 'readwrite');
      tx.objectStore('kv').put(v, k); tx.oncomplete = () => res(); tx.onerror = () => rej(tx.error);
    });
  }

  function pickMime() {
    const c = [['video/mp4;codecs=h264,aac', 'mp4'], ['video/mp4', 'mp4'],
    ['video/webm;codecs=vp9', 'webm'], ['video/webm;codecs=vp8', 'webm'], ['video/webm', 'webm']];
    for (const [m, ext] of c) { if (window.MediaRecorder && MediaRecorder.isTypeSupported(m)) return { mime: m, ext }; }
    return { mime: '', ext: 'webm' };
  }

  // FIX KHÔNG TUA ĐƯỢC: WebM từ MediaRecorder có duration = Infinity -> tua tới cuối 1 lần để trình duyệt tính lại.
  function fixSeekable(video) {
    const onMeta = () => {
      if (video.duration === Infinity || isNaN(video.duration)) {
        const onSeeked = () => { video.removeEventListener('seeked', onSeeked); try { video.currentTime = 0; } catch (e) { } };
        video.addEventListener('seeked', onSeeked);
        try { video.currentTime = 1e6; } catch (e) { }
      }
    };
    video.addEventListener('loadedmetadata', onMeta, { once: true });
  }

  function once(el, ev) { return new Promise(res => el.addEventListener(ev, res, { once: true })); }

  /* ---------- Đọc thư mục video trên máy ---------- */
  async function getDirHandle(promptIfMissing) {
    let h = null;
    try { h = await idbGet('dirHandle'); } catch (e) { }
    if (!h && promptIfMissing) {
      if (!window.showDirectoryPicker) { throw new Error('Trình duyệt không hỗ trợ đọc thư mục (cần Chrome/Edge).'); }
      h = await window.showDirectoryPicker({ mode: 'readwrite' });
      try { await idbSet('dirHandle', h); } catch (e) { }
    }
    if (!h) throw new Error('Chưa có thư mục video trên máy này.');
    const perm = await h.requestPermission({ mode: 'readwrite' });
    if (perm !== 'granted') throw new Error('Bạn chưa cấp quyền truy cập thư mục video.');
    return h;
  }

  async function fileToUrl(dirHandle, name) {
    if (!name) return null;
    try {
      const fh = await dirHandle.getFileHandle(name, { create: false });
      const file = await fh.getFile();
      return URL.createObjectURL(file);
    } catch (e) { console.warn('Không đọc được file video:', name, e); return null; }
  }

  /* ---------- Ghép 2 video (PiP) thành video thứ 3 ---------- */
  // onProgress(fraction|null, elapsedSec): fraction=null khi không đo được độ dài -> hiện thanh chạy.
  // Lưu ý: KHÔNG tua (seek) video nguồn trước khi phát — việc tua làm hỏng luồng thu (video merge bị đứng khung).
  async function mergeVideos(panoUrl, qrUrl, onProgress) {
    const vp = document.createElement('video'); vp.src = panoUrl; vp.muted = true; vp.playsInline = true; vp.preload = 'auto';
    let vq = null;
    if (qrUrl) { vq = document.createElement('video'); vq.src = qrUrl; vq.muted = true; vq.playsInline = true; vq.preload = 'auto'; }
    await Promise.all([once(vp, 'loadedmetadata'), vq ? once(vq, 'loadedmetadata') : Promise.resolve()]);
    // Chỉ dùng duration để tính % NẾU trình duyệt biết sẵn (không seek để lấy -> tránh hỏng thu).
    const totalDur = (vp.duration && isFinite(vp.duration)) ? vp.duration : null;

    const pw = vp.videoWidth || 1280, ph = vp.videoHeight || 720;
    const cv = document.createElement('canvas'); cv.width = pw; cv.height = ph;
    const ctx = cv.getContext('2d');
    const { mime, ext } = pickMime();
    const stream = cv.captureStream(30);
    const chunks = []; let rec;
    try { rec = new MediaRecorder(stream, mime ? { mimeType: mime, videoBitsPerSecond: 6_000_000 } : { videoBitsPerSecond: 6_000_000 }); }
    catch (e) { rec = new MediaRecorder(stream); }
    rec.ondataavailable = e => { if (e.data.size > 0) chunks.push(e.data); };
    const done = new Promise(res => { rec.onstop = () => res(new Blob(chunks, { type: mime || 'video/webm' })); });

    // Phát từ đầu (video mới tạo nên currentTime=0 sẵn), rồi mới start thu.
    await Promise.all([vp.play().catch(() => { }), vq ? vq.play().catch(() => { }) : Promise.resolve()]);
    rec.start(1000);

    let finished = false;
    vp.addEventListener('ended', () => { finished = true; }, { once: true });

    // CHỐNG PHÌNH FILE khi chuyển tab: tab ẩn -> TẠM DỪNG cả phát lẫn thu (không tích lũy thời gian thực),
    // quay lại -> tiếp tục. Nhờ vậy độ dài video merge luôn khớp bản gốc.
    function onVis() {
      if (document.visibilityState === 'hidden') {
        try { vp.pause(); if (vq) vq.pause(); } catch (e) { }
        try { if (rec.state === 'recording') rec.pause(); } catch (e) { }
      } else {
        try { if (rec.state === 'paused') rec.resume(); } catch (e) { }
        if (!finished) { vp.play().catch(() => { }); if (vq) vq.play().catch(() => { }); }
      }
    }
    document.addEventListener('visibilitychange', onVis);

    await new Promise(resolve => {
      let lastT = -1, lastAdvanceTs = Date.now();
      function draw() {
        if (finished) { resolve(); return; }
        const visible = document.visibilityState === 'visible';
        if (visible) {
          // Chỉ tính "kẹt" khi đang hiển thị (tab ẩn thì đã chủ động tạm dừng)
          if (vp.currentTime !== lastT) { lastT = vp.currentTime; lastAdvanceTs = Date.now(); }
          else if (Date.now() - lastAdvanceTs > 8000 && vp.currentTime > 0) { resolve(); return; }

          ctx.drawImage(vp, 0, 0, pw, ph);
          if (vq && vq.videoWidth > 0) {
            const sW = Math.round(pw / 5), sH = Math.round(sW * vq.videoHeight / vq.videoWidth);
            const x = pw - sW - 20, y = ph - sH - 20;
            ctx.drawImage(vq, x, y, sW, sH);
            ctx.strokeStyle = '#22c55e'; ctx.lineWidth = 3; ctx.strokeRect(x, y, sW, sH);
          }
        } else {
          lastAdvanceTs = Date.now(); // reset để không báo kẹt khi vừa quay lại
        }
        if (onProgress) {
          const frac = totalDur ? Math.min(0.99, vp.currentTime / totalDur) : null;
          onProgress(frac, vp.currentTime); // tiến độ theo thời điểm trong video (không theo đồng hồ thực)
        }
        requestAnimationFrame(draw);
      }
      draw();
    });
    document.removeEventListener('visibilitychange', onVis);
    try { rec.stop(); } catch (e) { }
    const blob = await done;
    return { blob, ext };
  }

  /* ---------- Giao diện ---------- */
  function el(id) { return document.getElementById(id); }

  function injectStyles() {
    if (document.getElementById('pkv-styles')) return;
    const s = document.createElement('style'); s.id = 'pkv-styles';
    s.textContent = `
      #packingVideoBox{--pkv-accent:#4f46e5;--pkv-ink:#0f172a;--pkv-muted:#64748b;--pkv-line:#e9ebf0;}
      .pkv-empty{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:12px;
        text-align:center;padding:26px 18px;border:1px solid var(--pkv-line);border-radius:14px;
        background:linear-gradient(180deg,#fbfcfe,#f6f8fb);}
      .pkv-empty-ic{width:52px;height:52px;border-radius:50%;display:flex;align-items:center;justify-content:center;
        background:#eef2ff;color:var(--pkv-accent);}
      .pkv-empty-ic .bi{font-size:24px;}
      .pkv-empty-text{font-size:13px;color:var(--pkv-muted);line-height:1.55;max-width:280px;}
      .pkv-btn{display:inline-flex;align-items:center;gap:7px;border:0;cursor:pointer;font-size:13px;font-weight:600;
        padding:9px 18px;border-radius:10px;color:#fff;background:var(--pkv-accent);
        box-shadow:0 2px 8px rgba(79,70,229,.28);transition:filter .15s,transform .05s;}
      .pkv-btn:hover{filter:brightness(1.06);} .pkv-btn:active{transform:translateY(1px);}
      .pkv-btn:disabled{opacity:.7;cursor:default;box-shadow:none;}
      .pkv-btn.ghost{background:#fff;color:var(--pkv-accent);border:1px solid #d9dcf5;box-shadow:none;}
      .pkv-btn.success{background:#16a34a;box-shadow:0 2px 8px rgba(22,163,74,.28);}
      .pkv-grid{display:flex;flex-direction:column;gap:16px;}
      .pkv-tile-head{display:flex;align-items:center;gap:7px;font-size:12px;font-weight:600;color:var(--pkv-ink);margin-bottom:7px;letter-spacing:.2px;}
      .pkv-tile-head .bi{color:var(--pkv-accent);font-size:14px;}
      .pkv-frame{position:relative;width:100%;aspect-ratio:16/9;background:#0b0f19;border-radius:12px;
        overflow:hidden;border:1px solid #1e2433;box-shadow:0 4px 14px rgba(15,23,42,.12);}
      .pkv-frame video{position:absolute;inset:0;width:100%;height:100%;object-fit:contain;background:#0b0f19;}
      .pkv-actions{display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin-top:14px;}
      .pkv-msg{font-size:12.5px;margin-bottom:12px;display:none;border-radius:10px;padding:9px 12px;line-height:1.45;}
      .pkv-msg.ok{color:#166534;background:#f0fdf4;border:1px solid #bbf7d0;}
      .pkv-msg.err{color:#b91c1c;background:#fef2f2;border:1px solid #fecaca;}
      .pkv-msg.info{color:#3730a3;background:#eef2ff;border:1px solid #e0e4f9;}
      .pkv-prog{display:none;flex:1 1 100%;min-width:160px;}
      .pkv-prog-track{height:8px;border-radius:99px;background:#e5e7eb;overflow:hidden;position:relative;}
      .pkv-prog-fill{height:100%;width:0;border-radius:99px;background:linear-gradient(90deg,#6366f1,#4f46e5);transition:width .2s;}
      .pkv-prog-fill.indet{width:35% !important;position:absolute;animation:pkvIndet 1.1s ease-in-out infinite;}
      @keyframes pkvIndet{0%{left:-35%}100%{left:100%}}
      .pkv-prog-text{font-size:11.5px;color:var(--pkv-accent);margin-top:5px;font-weight:600;}
    `;
    document.head.appendChild(s);
  }

  function tileHtml(id, icon, label, full) {
    return `<div class="pkv-tile${full ? ' pkv-tile-full' : ''}" id="${id}-wrap" style="display:none;">
        <div class="pkv-tile-head"><i class="bi ${icon}"></i>${label}</div>
        <div class="pkv-frame"><video id="${id}" controls playsinline preload="metadata"></video></div>
      </div>`;
  }

  function html(meta) {
    return `
      <div id="pkv-msg" class="pkv-msg"></div>
      <div id="pkv-empty" class="pkv-empty">
        <div class="pkv-empty-ic"><i class="bi bi-camera-reels"></i></div>
        <div class="pkv-empty-text">Video đóng gói được lưu trên máy này (thư mục đã chọn khi đóng gói).</div>
        <button type="button" id="pkv-load" class="pkv-btn"><i class="bi bi-folder2-open"></i>Hiển thị video</button>
      </div>
      <div class="pkv-grid" id="pkv-players" style="display:none;">
        ${tileHtml('pkv-pano', 'bi-camera-video', 'Toàn cảnh', false)}
        ${tileHtml('pkv-qr', 'bi-qr-code-scan', 'Cam QR', false)}
        ${tileHtml('pkv-merged', 'bi-collection-play', 'Video ghép (PiP)', true)}
      </div>
      <div class="pkv-actions" id="pkv-actionbar" style="display:none;">
        <button type="button" id="pkv-merge" class="pkv-btn success" style="display:none;"><i class="bi bi-magic"></i>Ghép 2 video</button>
        <div class="pkv-prog" id="pkv-progress">
          <div class="pkv-prog-track"><div class="pkv-prog-fill" id="pkv-prog-fill"></div></div>
          <div class="pkv-prog-text" id="pkv-prog-text"></div>
        </div>
      </div>`;
  }

  function setMsg(t, kind) { const m = el('pkv-msg'); if (m) { m.className = 'pkv-msg ' + (kind || 'info'); m.textContent = t; m.style.display = 'block'; } }
  // frac: số 0..1 -> thanh %; null -> thanh chạy (indeterminate); false -> ẩn.
  function setProgress(frac, text) {
    const p = el('pkv-progress'); const f = el('pkv-prog-fill'); const t = el('pkv-prog-text');
    if (frac === false) { if (p) p.style.display = 'none'; return; }
    if (p) p.style.display = 'block';
    if (f) {
      if (frac == null) { f.classList.add('indet'); }
      else { f.classList.remove('indet'); f.style.width = Math.max(0, Math.min(100, frac * 100)) + '%'; }
    }
    if (t) t.textContent = text || '';
  }

  async function loadAndShow(orderCode, meta) {
    const btn = el('pkv-load'); if (btn) { btn.disabled = true; btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang mở…'; }
    setMsg('Đang mở thư mục video trên máy…', 'info');
    let dir;
    try { dir = await getDirHandle(true); }
    catch (e) {
      setMsg('✗ ' + e.message + ' Hãy mở đúng máy đã dùng khi đóng gói, hoặc nhấn nút để chọn lại thư mục.', 'err');
      if (btn) { btn.disabled = false; btn.innerHTML = '<i class="bi bi-folder2-open me-1"></i>Chọn lại thư mục'; }
      return;
    }

    const panoUrl = await fileToUrl(dir, meta.pano);
    const qrUrl = await fileToUrl(dir, meta.qr);
    const mergedUrl = meta.merged ? await fileToUrl(dir, meta.merged) : null;

    if (!panoUrl && !qrUrl && !mergedUrl) {
      setMsg('✗ Không tìm thấy file video trong thư mục "' + (meta.dir || '?') + '". Kiểm tra lại thư mục đã chọn.', 'err');
      if (btn) { btn.disabled = false; btn.innerHTML = '<i class="bi bi-folder2-open me-1"></i>Chọn lại thư mục'; }
      return;
    }

    el('pkv-empty').style.display = 'none';
    el('pkv-players').style.display = '';
    el('pkv-actionbar').style.display = '';
    if (panoUrl) { const v = el('pkv-pano'); fixSeekable(v); v.src = panoUrl; el('pkv-pano-wrap').style.display = ''; }
    if (qrUrl) { const v = el('pkv-qr'); fixSeekable(v); v.src = qrUrl; el('pkv-qr-wrap').style.display = ''; }
    if (mergedUrl) { const v = el('pkv-merged'); fixSeekable(v); v.src = mergedUrl; el('pkv-merged-wrap').style.display = ''; }

    // Chỉ cho ghép khi có đủ cả 2 video nguồn (và merge sẽ ghi đè/tạo video ghép)
    const mergeBtn = el('pkv-merge');
    if (panoUrl && qrUrl && mergeBtn) {
      mergeBtn.style.display = '';
      mergeBtn.onclick = () => doMerge(orderCode, meta, dir, panoUrl, qrUrl);
    }
    setMsg('✅ Đã tải video.', 'ok');
  }

  async function doMerge(orderCode, meta, dir, panoUrl, qrUrl) {
    const mergeBtn = el('pkv-merge');
    if (mergeBtn) { mergeBtn.disabled = true; mergeBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang ghép…'; }
    setProgress(0, 'Đang ghép video…');
    setMsg('Đang ghép 2 video thành video PiP. Quá trình chạy theo độ dài video; nếu bạn chuyển tab, nó sẽ TẠM DỪNG và tự tiếp tục khi quay lại (độ dài video vẫn đúng).', 'info');
    try {
      const { blob, ext } = await mergeVideos(panoUrl, qrUrl, (frac, elapsed) => {
        if (frac == null) setProgress(null, 'Đang ghép… ' + Math.round(elapsed) + 's');
        else setProgress(frac, 'Đang ghép… ' + Math.round(frac * 100) + '%' + (elapsed ? ' (' + Math.round(elapsed) + 's)' : ''));
      });
      setProgress(null, 'Đang lưu file…');
      const base = (meta.pano || ('order_' + orderCode)).replace(/_pano\.[^.]+$/, '').replace(/\.[^.]+$/, '');
      const mergedName = base + '_merged.' + ext;
      const fh = await dir.getFileHandle(mergedName, { create: true });
      const w = await fh.createWritable(); await w.write(blob); await w.close();
      meta.merged = mergedName;
      try { await fetch(API + '/orders/' + orderCode + '/video', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ videoPath: JSON.stringify(meta) }) }); } catch (e) { }
      const url = URL.createObjectURL(blob);
      const v = el('pkv-merged'); fixSeekable(v); v.src = url; el('pkv-merged-wrap').style.display = '';
      setProgress(1, '✅ Đã ghép & lưu: ' + mergedName);
      setMsg('✅ Đã tạo video ghép (PiP) và lưu vào thư mục.', 'ok');
      if (mergeBtn) mergeBtn.style.display = 'none';
      v.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    } catch (e) {
      setProgress(false);
      setMsg('✗ Ghép lỗi: ' + (e.message || e), 'err');
      if (mergeBtn) { mergeBtn.disabled = false; mergeBtn.innerHTML = '<i class="bi bi-magic me-1"></i>Ghép 2 video'; }
    }
  }

  // API công khai: render vào #packingVideoBox từ chuỗi packingVideoPath của đơn.
  function render(orderCode, packingVideoPath) {
    const box = el('packingVideoBox');
    const card = el('packingVideoCard');
    if (!box) return;
    if (!packingVideoPath) { if (card) card.style.display = 'none'; return; }
    if (card) card.style.display = '';
    injectStyles();

    let meta = null;
    const s = String(packingVideoPath).trim();
    if (s.startsWith('{')) { try { meta = JSON.parse(s); } catch (e) { meta = null; } }
    if (!meta) {
      // Định dạng CŨ: chỉ 1 chuỗi đường dẫn (video ghép sẵn). Coi như 1 file 'merged'.
      const nm = s.split('/').pop();
      meta = { v: 1, dir: s.split('/').slice(0, -1).join('/'), merged: nm };
    }
    box.innerHTML = html(meta);
    el('pkv-load').onclick = () => loadAndShow(orderCode, meta);
  }

  window.OMSPackingVideo = { render };
})();
