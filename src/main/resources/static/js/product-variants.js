/*
 * OMSVariants — trình biên tập phân loại hàng kiểu Shopee (dùng chung cho trang tạo & sửa sản phẩm).
 *
 * Mô hình:
 *   - Tối đa 2 nhóm phân loại (tier). Mỗi tier có: tên + danh sách tùy chọn.
 *   - CHỈ tier 1 (nhóm phân loại đầu tiên - thường là hình dáng/màu) có ẢNH cho từng tùy chọn.
 *     Tier 2 không cần ảnh (đúng như Shopee).
 *   - Bảng "Danh sách phân loại hàng" là tích Descartes của các tùy chọn, mỗi dòng nhập Giá/Kho/SKU/Mã vạch.
 *     KHÔNG còn cột ảnh trên từng dòng tổ hợp.
 *   - Khi lưu: ảnh của mỗi dòng tổ hợp = ảnh của tùy chọn tier-1 tương ứng (propagate),
 *     nên ProductVariant.imageUrl vẫn có dữ liệu -> trang danh sách/đơn hàng hiển thị như cũ (không cần đổi DB).
 *
 * API:
 *   OMSVariants.init(opts)  -> khởi tạo. opts:
 *       tierListId, matrixWrapId, addTierBtnId  (id các phần tử DOM)
 *       priceInputId, skuInputId, nameInputId, manageStockId, singleStockAreaId
 *       preload: [{name, options:[{value,image}]}]  (tùy chọn, cho trang sửa)
 *   OMSVariants.refresh()   -> vẽ lại (gọi khi đổi tên/SKU/giá sản phẩm)
 *   OMSVariants.collect()   -> {attributes:[{name,values}], variants:[{variantName,sku,imageUrl,price,costPrice,stockQuantity,barcodes}]}
 *   OMSVariants.hasTiers()  -> boolean
 */
(function () {
    const SEP = '';
    let opts = {};
    let tiers = [];            // [{name, options:[{value, image}]}]
    let cellData = {};         // key(comboValues.join(SEP)) -> {price,cost,stock,sku,barcode}

    function $(id) {
        return document.getElementById(id);
    }

    function slug(name) {
        if (!name || !name.trim()) return 'AUTO';
        return name.normalize('NFD').replace(/[̀-ͯ]/g, '')
            .replace(/Đ/g, 'D').replace(/đ/g, 'd')
            .replace(/[^a-zA-Z0-9\s-]/g, '').trim()
            .replace(/\s+/g, '-').toUpperCase();
    }

    function esc(s) {
        return (s || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function basePrice() {
        const el = opts.priceInputId && $(opts.priceInputId);
        if (!el) return 0;
        const n = parseFloat(String(el.value || '').replace(/[^0-9.]/g, ''));
        return isNaN(n) ? 0 : n;
    }

    function baseSku() {
        const manual = opts.skuInputId && $(opts.skuInputId) ? $(opts.skuInputId).value.trim() : '';
        const name = opts.nameInputId && $(opts.nameInputId) ? $(opts.nameInputId).value.trim() : '';
        return manual ? manual : slug(name);
    }

    function isManaged() {
        return !opts.manageStockId || !$(opts.manageStockId) || $(opts.manageStockId).checked;
    }

    // Trang sửa: tồn kho khóa (điều chỉnh qua chức năng kho), không cho sửa trong bảng
    function stockLocked() {
        return !!opts.lockStock;
    }

    // Danh sách tier có ít nhất 1 tùy chọn hợp lệ (có tên giá trị)
    function validTiers() {
        return tiers
            .map(t => ({name: (t.name || '').trim() || 'Phân loại', options: t.options.filter(o => (o.value || '').trim() !== '')}))
            .filter(t => t.options.length > 0);
    }

    function cartesian(arrs) {
        let r = [[]];
        arrs.forEach(arr => {
            const next = [];
            r.forEach(prev => arr.forEach(v => next.push(prev.concat([v]))));
            r = next;
        });
        return r;
    }

    // ---------- RENDER TIERS ----------
    function renderTiers() {
        const wrap = $(opts.tierListId);
        if (!wrap) return;
        let html = '';
        tiers.forEach((tier, ti) => {
            const isFirst = ti === 0;
            html += `
            <div class="var-tier card border shadow-sm mb-3" data-ti="${ti}" style="background:#fbfcfe;">
              <div class="card-body p-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <label class="form-label fw-bold mb-0">Phân loại ${ti + 1}</label>
                  <i class="bi bi-x-lg text-muted var-remove-tier" role="button" title="Xóa nhóm phân loại"></i>
                </div>
                <input type="text" class="form-control oms-input var-tier-name mb-3" placeholder="VD: ${isFirst ? 'Màu sắc / Phiên bản' : 'Size / Switch'}" value="${esc(tier.name)}">
                <label class="form-label small text-muted">Tùy chọn</label>
                <div class="var-options d-flex flex-column gap-2">`;
            tier.options.forEach((opt, oi) => {
                html += `
                  <div class="var-option d-flex align-items-center gap-2" data-oi="${oi}">
                    <input type="text" class="form-control oms-input var-option-value" placeholder="Nhập tùy chọn (VD: Đỏ)" value="${esc(opt.value)}">
                    <i class="bi bi-trash text-muted var-remove-option flex-shrink-0" role="button" title="Xóa tùy chọn"></i>
                  </div>`;
            });
            html += `
                </div>
                <button type="button" class="btn btn-link text-decoration-none p-0 mt-2 fw-medium var-add-option">
                    <i class="bi bi-plus-circle me-1"></i>Thêm tùy chọn
                </button>
              </div>
            </div>`;
        });
        wrap.innerHTML = html;
        // Cho phép thêm phân loại 3, 4, ... n (không giới hạn) -> luôn hiện nút thêm
    }

    // ---------- RENDER MATRIX ----------
    function renderMatrix() {
        const wrap = $(opts.matrixWrapId);
        const singleArea = opts.singleStockAreaId ? $(opts.singleStockAreaId) : null;
        if (!wrap) return;

        const vts = validTiers();
        if (vts.length === 0) {
            wrap.innerHTML = '';
            wrap.classList.add('d-none');
            if (singleArea) singleArea.classList.remove('d-none');
            return;
        }
        if (singleArea) singleArea.classList.add('d-none');
        wrap.classList.remove('d-none');

        const combos = cartesian(vts.map(t => t.options.map(o => o.value.trim())));
        const bp = basePrice();
        const bs = baseSku();
        // Số dòng mỗi nhóm phân loại 1 = tích số tùy chọn của các phân loại còn lại (để gộp ô + ảnh dùng chung)
        const groupSize = vts.slice(1).reduce((a, t) => a * t.options.length, 1);

        // Header
        let head = '<tr>';
        vts.forEach((t, i) => head += `<th class="text-start ps-3">${esc(t.name)}${i === 0 ? ' <span class="text-muted fw-normal">(ảnh)</span>' : ''}</th>`);
        head += `<th style="width:150px;">Giá bán</th><th style="width:150px;">Giá nhập (Vốn)</th>`
            + `<th style="width:110px;">Kho hàng</th><th style="width:170px;">SKU phân loại</th>`
            + `<th style="width:180px;">Mã vạch (GTIN)</th></tr>`;

        // Bulk apply row
        let bulk = `<tr class="var-bulk-row" style="background:#f8fafc;">
            <td colspan="${vts.length}" class="text-end pe-3 fw-semibold text-muted">Áp dụng cho tất cả phân loại</td>
            <td><input type="number" class="form-control form-control-sm text-end oms-input" id="bulkPrice" placeholder="Giá"></td>
            <td><input type="number" class="form-control form-control-sm text-end oms-input" id="bulkCost" placeholder="Vốn"></td>
            <td><input type="number" class="form-control form-control-sm text-center oms-input" id="bulkStock" placeholder="Kho"></td>
            <td colspan="2"><button type="button" class="btn btn-sm btn-primary w-100 var-bulk-apply">Áp dụng</button></td>
        </tr>`;

        // Rows
        let body = '';
        combos.forEach((combo, idx) => {
            const key = combo.join(SEP);
            const saved = cellData[key] || {};
            const suffix = combo.join('-');
            const autoSku = bs === 'AUTO' ? slug(suffix) : `${bs}-${slug(suffix)}`;
            const price = saved.price != null ? saved.price : bp;
            const cost = saved.cost != null ? saved.cost : 0;
            const stock = saved.stock != null ? saved.stock : 0;
            const sku = saved.sku != null ? saved.sku : autoSku;
            const barcode = saved.barcode != null ? saved.barcode : '';

            body += `<tr class="var-combo-row" data-key="${esc(key)}">`;
            combo.forEach((val, ci) => {
                if (ci === 0) {
                    // Cột phân loại 1: GỘP ô theo nhóm (rowspan) + 1 ô ẢNH dùng chung cho cả nhóm
                    if (idx % groupSize === 0) {
                        const img = tierOptionImage(0, val);
                        const imgUi = `
                            <label class="var-group-img d-block mt-2 mx-auto" role="button" data-val="${esc(val)}"
                                   title="Ảnh dùng chung cho nhóm '${esc(val)}'"
                                   style="width:60px;height:60px;border:1px dashed #cbd5e1;border-radius:8px;display:flex;align-items:center;justify-content:center;overflow:hidden;background:#fff;">
                                <span class="text-muted var-group-img-icon ${img ? 'd-none' : ''}" style="font-size:12px;text-align:center;line-height:1.1;"><i class="bi bi-camera d-block fs-6"></i>Thêm ảnh</span>
                                <img class="var-group-img-preview ${img ? '' : 'd-none'}" src="${esc(img)}" style="width:100%;height:100%;object-fit:cover;">
                                <input type="file" accept="image/*" class="d-none var-group-img-input">
                            </label>`;
                        body += `<td rowspan="${groupSize}" class="text-center align-middle bg-light" data-val="${esc(val)}">
                                    <div class="fw-medium text-dark">${esc(val)}</div>${imgUi}</td>`;
                    }
                    // các dòng còn lại trong nhóm: bỏ ô (đã bị rowspan gộp)
                } else {
                    body += `<td class="text-start ps-3 align-middle bg-light"><span class="fw-medium text-dark">${esc(val)}</span></td>`;
                }
            });
            body += `
                <td><input type="number" class="form-control form-control-sm text-end oms-input var-price" value="${esc(String(price))}"></td>
                <td><input type="number" class="form-control form-control-sm text-end oms-input var-cost" value="${esc(String(cost))}"></td>
                <td><input type="number" class="form-control form-control-sm text-center oms-input var-stock ${stockLocked() ? 'bg-light' : ''}" value="${esc(String(stock))}" ${(isManaged() && !stockLocked()) ? '' : 'disabled'}></td>
                <td><input type="text" class="form-control form-control-sm oms-input var-sku text-primary fw-medium" value="${esc(sku)}"></td>
                <td><input type="text" class="form-control form-control-sm oms-input var-barcode" value="${esc(barcode)}" placeholder="Nhiều mã cách nhau dấu phẩy"></td>
            </tr>`;
        });

        wrap.innerHTML = `
            <h6 class="fw-bold mb-3 text-dark">Danh sách phân loại hàng</h6>
            <div class="table-responsive">
                <table class="table table-bordered align-middle mb-0">
                    <thead>${head}</thead>
                    <tbody>${bulk}${body}</tbody>
                </table>
            </div>`;
    }

    function tierOptionImage(tierIndex, value) {
        const t = tiers[tierIndex];
        if (!t) return '';
        const o = t.options.find(o => (o.value || '').trim() === (value || '').trim());
        return o ? (o.image || '') : '';
    }

    // Trước khi vẽ lại, lưu giá trị người dùng đã nhập trong bảng vào cellData
    function snapshotMatrix() {
        const wrap = $(opts.matrixWrapId);
        if (!wrap) return;
        wrap.querySelectorAll('tr.var-combo-row').forEach(row => {
            const key = row.getAttribute('data-key');
            cellData[key] = {
                price: numOrNull(row.querySelector('.var-price')),
                cost: numOrNull(row.querySelector('.var-cost')),
                stock: numOrNull(row.querySelector('.var-stock')),
                sku: strOrNull(row.querySelector('.var-sku')),
                barcode: strOrNull(row.querySelector('.var-barcode'))
            };
        });
    }

    function numOrNull(el) {
        return el && el.value !== '' ? el.value : null;
    }

    function strOrNull(el) {
        return el ? el.value : null;
    }

    function render() {
        snapshotMatrix();
        renderTiers();
        renderMatrix();
    }

    // ---------- EVENTS ----------
    function bindEvents() {
        const tierWrap = $(opts.tierListId);
        const matrixWrap = $(opts.matrixWrapId);

        // Tier name / option value input -> re-render (debounce nhẹ để giữ focus khỏi giật khi gõ)
        if (tierWrap) {
            tierWrap.addEventListener('input', function (e) {
                if (e.target.classList.contains('var-tier-name')) {
                    const ti = tierIndexOf(e.target);
                    if (ti >= 0) tiers[ti].name = e.target.value;
                    // Chỉ cần vẽ lại header bảng -> vẽ lại matrix (không đụng tier để không mất focus)
                    scheduleMatrixOnly();
                } else if (e.target.classList.contains('var-option-value')) {
                    const {ti, oi} = optionIndexOf(e.target);
                    if (ti >= 0) tiers[ti].options[oi].value = e.target.value;
                    scheduleMatrixOnly();
                }
            });

            tierWrap.addEventListener('click', function (e) {
                if (e.target.closest('.var-add-option')) {
                    const ti = tierIndexOf(e.target);
                    if (ti >= 0) {
                        tiers[ti].options.push({value: '', image: ''});
                        render();
                    }
                } else if (e.target.closest('.var-remove-option')) {
                    const {ti, oi} = optionIndexOf(e.target);
                    if (ti >= 0) {
                        tiers[ti].options.splice(oi, 1);
                        if (tiers[ti].options.length === 0) tiers[ti].options.push({value: '', image: ''});
                        render();
                    }
                } else if (e.target.closest('.var-remove-tier')) {
                    const ti = tierIndexOf(e.target);
                    if (ti >= 0) {
                        tiers.splice(ti, 1);
                        render();
                    }
                }
            });

        }

        if (matrixWrap) {
            matrixWrap.addEventListener('click', function (e) {
                if (e.target.closest('.var-bulk-apply')) {
                    applyBulk();
                }
            });
            // Ảnh dùng chung theo nhóm phân loại 1 (đặt trong bảng biến thể)
            matrixWrap.addEventListener('change', function (e) {
                if (e.target.classList.contains('var-group-img-input')) {
                    handleGroupImage(e.target);
                }
            });
        }

        const addBtn = $(opts.addTierBtnId);
        if (addBtn) {
            addBtn.addEventListener('click', function () {
                tiers.push({name: '', options: [{value: '', image: ''}]});
                render();
            });
        }
    }

    let matrixTimer = null;

    function scheduleMatrixOnly() {
        clearTimeout(matrixTimer);
        matrixTimer = setTimeout(() => {
            snapshotMatrix();
            renderMatrix();
        }, 250);
    }

    function tierIndexOf(el) {
        const card = el.closest('.var-tier');
        return card ? parseInt(card.getAttribute('data-ti'), 10) : -1;
    }

    function optionIndexOf(el) {
        const ti = tierIndexOf(el);
        const opt = el.closest('.var-option');
        const oi = opt ? parseInt(opt.getAttribute('data-oi'), 10) : -1;
        return {ti, oi};
    }

    // Upload ảnh dùng chung cho một nhóm phân loại 1 (từ bảng biến thể) -> lưu vào tùy chọn tier-1
    async function handleGroupImage(input) {
        if (!input.files || !input.files.length) return;
        const label = input.closest('.var-group-img');
        const val = label ? label.getAttribute('data-val') : null;
        if (val == null || !tiers[0]) return;
        const opt = tiers[0].options.find(o => (o.value || '').trim() === val.trim());
        if (!opt) return;
        try {
            opt.image = await opts.uploadFn(input.files[0]);
            render();
        } catch (err) {
            if (window.Toast) Toast.fire({icon: 'error', title: 'Tải ảnh thất bại!'});
        }
    }

    function applyBulk() {
        const wrap = $(opts.matrixWrapId);
        const p = $('bulkPrice') ? $('bulkPrice').value : '';
        const c = $('bulkCost') ? $('bulkCost').value : '';
        const s = $('bulkStock') ? $('bulkStock').value : '';
        wrap.querySelectorAll('tr.var-combo-row').forEach(row => {
            if (p !== '') row.querySelector('.var-price').value = p;
            if (c !== '') row.querySelector('.var-cost').value = c;
            if (s !== '') {
                const st = row.querySelector('.var-stock');
                if (!st.disabled) st.value = s;
            }
        });
    }

    // ---------- PUBLIC ----------
    function init(options) {
        opts = options || {};
        opts.uploadFn = opts.uploadFn || (async () => {
            throw new Error('no upload fn');
        });
        tiers = [];
        cellData = {};
        if (Array.isArray(opts.preload) && opts.preload.length) {
            opts.preload.forEach(t => {
                tiers.push({
                    name: t.name || '',
                    options: (t.options && t.options.length ? t.options : [{value: '', image: ''}])
                        .map(o => ({value: o.value || '', image: o.image || ''}))
                });
            });
        }
        // Nếu có preload kèm sẵn dữ liệu ô (giá/kho/sku theo tổ hợp) -> nạp vào cellData
        if (opts.preloadCells && typeof opts.preloadCells === 'object') {
            cellData = Object.assign({}, opts.preloadCells);
        }
        bindEvents();
        render();
    }

    function hasTiers() {
        return validTiers().length > 0;
    }

    function collect() {
        snapshotMatrix();
        const vts = validTiers();
        const attributes = vts.map(t => ({name: t.name, values: t.options.map(o => o.value.trim())}));

        const variants = [];
        const wrap = $(opts.matrixWrapId);
        if (wrap) {
            wrap.querySelectorAll('tr.var-combo-row').forEach(row => {
                // Lấy tổ hợp từ data-key (ô phân loại 1 bị gộp rowspan nên không đọc theo ô được)
                const parts = (row.getAttribute('data-key') || '').split(SEP).filter(x => x !== '');
                const firstVal = parts.length ? parts[0] : '';
                variants.push({
                    variantName: parts.join(' - '),
                    sku: (row.querySelector('.var-sku') || {}).value || '',
                    imageUrl: tierOptionImage(0, firstVal),   // propagate ảnh tier-1 xuống tổ hợp
                    price: parseFloat((row.querySelector('.var-price') || {}).value) || 0,
                    costPrice: parseFloat((row.querySelector('.var-cost') || {}).value) || 0,
                    stockQuantity: parseInt((row.querySelector('.var-stock') || {}).value) || 0,
                    barcodes: (((row.querySelector('.var-barcode') || {}).value) || '').split(',').map(s => s.trim()).filter(Boolean)
                });
            });
        }
        return {attributes, variants};
    }

    window.OMSVariants = {init, refresh: render, collect, hasTiers, SEP};
})();
