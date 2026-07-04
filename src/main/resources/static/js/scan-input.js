/* ============================================================
   Ô QUÉT MÃ chống bộ gõ tiếng Việt (Unikey/Telex biến W->Ư, aa->â, tone marks...).
   Cách tiếp cận: ĐỂ bộ gõ chèn bình thường (không mất ký tự), rồi TỰ ĐỘNG
   giải-Telex ký tự tiếng Việt về ASCII gốc (vì mã SKU/serial/vận đơn/barcode đều là ASCII).
   Áp cho input có class .js-scan hoặc thuộc tính data-scan.
   ============================================================ */
(function () {
  "use strict";

  // Đảo ngược 1 ký tự tiếng Việt (precomposed) về chuỗi phím Telex gốc.
  function reverseChar(ch) {
    if (ch === 'đ') return 'dd';
    if (ch === 'Đ') return 'DD';
    const nfd = ch.normalize('NFD');
    const base = nfd[0];
    const marks = nfd.slice(1);
    // Nếu không phải ký tự Latin có dấu -> giữ nguyên
    if (!/[a-zA-Z]/.test(base) || marks.length === 0) return ch;

    const has = m => marks.indexOf(m) !== -1;
    let tone = '';
    if (has('́')) tone = 's';       // sắc
    else if (has('̀')) tone = 'f';  // huyền
    else if (has('̉')) tone = 'r';  // hỏi
    else if (has('̃')) tone = 'x';  // ngã
    else if (has('̣')) tone = 'j';  // nặng

    const lower = base.toLowerCase();
    let keys;
    if (has('̛')) {                 // móc (horn): ơ, ư
      if (lower === 'o') keys = 'ow';
      else if (lower === 'u') keys = 'w'; // ư  (phím W)
      else keys = base;
    } else if (has('̂')) {          // mũ (circumflex): â ê ô  <- gõ đôi
      keys = base + base;
    } else if (has('̆')) {          // trăng (breve): ă  <- aw
      keys = base + 'w';
    } else {
      keys = base;                       // chỉ có dấu thanh trên nguyên âm thường
    }
    // Giữ hoa/thường theo ký tự gốc
    if (base === base.toUpperCase()) { keys = keys.toUpperCase(); tone = tone.toUpperCase(); }
    return keys + tone;
  }

  function deTelex(s) {
    if (!s) return s;
    let out = '';
    for (const ch of s) {
      // ASCII giữ nguyên cho nhanh
      out += (ch.charCodeAt(0) < 128) ? ch : reverseChar(ch);
    }
    return out;
  }

  function harden(el, onEnter) {
    if (!el || el.dataset.scanHardened) return;
    el.dataset.scanHardened = '1';
    el.setAttribute('autocomplete', 'off');
    el.setAttribute('autocorrect', 'off');
    el.setAttribute('autocapitalize', 'off');
    el.setAttribute('spellcheck', 'false');

    function sanitize() {
      const v = el.value;
      const c = deTelex(v);
      if (c !== v) {
        const atEnd = el.selectionStart === v.length && el.selectionEnd === v.length;
        el.value = c;
        if (atEnd) { try { el.selectionStart = el.selectionEnd = c.length; } catch (e) { } }
      }
    }

    // Sau khi bộ gõ chèn xong -> chuẩn hoá về ASCII
    el.addEventListener('input', function (e) { if (!e.isComposing) sanitize(); });
    el.addEventListener('compositionend', sanitize);
    // Trước khi xử lý Enter (quét xong) -> chuẩn hoá rồi mới chạy callback
    el.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        sanitize();
        if (typeof onEnter === 'function') { e.preventDefault(); onEnter(el.value, e); }
      }
    });
    el.addEventListener('blur', sanitize);
  }

  function auto() {
    document.querySelectorAll('input[data-scan], input.js-scan').forEach(function (el) { harden(el); });
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', auto);
  else auto();

  window.OMSScan = { harden: harden, deTelex: deTelex };
})();
