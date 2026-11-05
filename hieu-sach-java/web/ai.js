'use strict';

function initStoreChat() {
  const launcher = $('#ai-launcher');
  const chat = $('#ai-chat');
  const feed = $('#ai-chat-feed');
  const log = $('#ai-chat-log');
  const form = $('#ai-chat-form');
  const input = $('#ai-question');
  const send = $('.ai-send', form);
  const status = $('#ai-chat-status');
  const suggestions = $('#ai-suggestions');
  const previous = [];

  function show(open) {
    chat.hidden = !open;
    launcher.setAttribute('aria-expanded', String(open));
    launcher.setAttribute('aria-label', open ? 'Đóng trợ lý AI' : 'Mở trợ lý AI');
    if (open) feed.scrollTop = feed.scrollHeight;
    (open ? input : launcher).focus();
  }

  function addMessage(text, role, note) {
    const bubble = document.createElement('div');
    bubble.className = `ai-message ai-message-${role}`;
    const content = document.createElement('p');
    content.textContent = text;
    bubble.append(content);
    if (note) {
      const caption = document.createElement('small');
      caption.textContent = note;
      bubble.append(caption);
    }
    log.append(bubble);
    feed.scrollTop = feed.scrollHeight;
  }

  launcher.addEventListener('click', () => show(chat.hidden));
  $('#ai-chat-close').addEventListener('click', () => show(false));
  chat.addEventListener('keydown', evt => {
    if (evt.key === 'Escape') show(false);
  });
  suggestions.addEventListener('click', evt => {
    const option = evt.target.closest('button');
    if (!option) return;
    input.value = option.textContent;
    form.requestSubmit();
  });
  form.addEventListener('submit', async evt => {
    evt.preventDefault();
    const question = input.value.trim();
    if (send.disabled || !question) return;
    send.disabled = true;
    suggestions.hidden = true;
    input.value = '';
    input.focus();
    const body = new URLSearchParams({question});
    previous.forEach(text => body.append('previous', text));
    addMessage(question, 'user');
    status.hidden = false;
    status.textContent = 'Đang tra cứu dữ liệu cửa hàng…';
    feed.scrollTop = feed.scrollHeight;
    try {
      const data = await api('ai/chat', body);
      addMessage(data.answer, 'assistant', data.note && `Trả lời cơ bản · ${data.note}`);
      previous.push(question);
      if (previous.length > 4) previous.shift();
    } catch (err) {
      addMessage(err.message || 'Chưa gửi được câu hỏi. Bạn có thể thử lại.', 'assistant');
      if (!input.value) input.value = question;
    } finally {
      status.hidden = true;
      send.disabled = false;
    }
  });
}

function aiReportPanel() {
  const month = new Intl.DateTimeFormat('en-CA', {year: 'numeric', month: '2-digit', timeZone: 'Asia/Ho_Chi_Minh'}).format(new Date());
  return `<section class="panel ai-report" aria-labelledby="ai-report-title">
    <div class="ai-report-head">
      <span class="ai-emblem" aria-hidden="true">✦</span>
      <div>
        <p class="eyebrow">TRỢ LÝ TRI THỨC</p>
        <h2 id="ai-report-title">Phân tích theo tháng</h2>
        <p class="ai-report-description">So sánh với ba tháng trước và xem gợi ý cho tháng tới.</p>
      </div>
    </div>
    <form class="ai-report-form" id="ai-report-form">
      <div class="field"><label for="ai-month">Tháng phân tích</label>
        <input id="ai-month" name="month" type="month" value="${month}" max="${month}" min="1900-01" required>
      </div>
      <div class="field"><label for="ai-branch">Chi nhánh</label>
        <select id="ai-branch" name="branch">${options(branches, '', 'Tất cả chi nhánh')}</select>
      </div>
      <button type="submit" class="button primary"><span aria-hidden="true">✦</span> Tạo phân tích</button>
    </form>
    <div class="ai-report-output" id="ai-report-output" hidden></div>
  </section>`;
}

function aiNarrative(text) {
  const headings = new Set(['So sánh', 'Nhận xét', 'Đánh giá', 'Việc nên làm']);
  return text.split('\n').map(line => {
    const title = line.trim().replace(/^[#*\s]+|[:*\s]+$/g, '');
    return headings.has(title) ? `<strong>${e(title)}</strong>` : e(line);
  }).join('\n');
}

function aiReportResult(data) {
  const metrics = [
    ['revenue', 'Doanh thu', money],
    ['quantity', 'Sách bán', v => nf.format(v) + ' cuốn'],
    ['invoices', 'Hóa đơn hoàn tất', v => nf.format(v)],
    ['purchaseCost', 'Chi phí nhập', money]
  ];
  const signed = v => v > 0 ? '+' : v < 0 ? '−' : '';
  const [current] = data.periods;
  return `<div class="ai-report-context"><strong>${e(data.scope)}</strong> · ${day(current.from)} – ${day(current.to)}
    <p>Mỗi kỳ trong bảng ghi rõ khoảng ngày dùng để đối chiếu.</p></div>
    <div class="ai-compare">${metrics.map(([key, label, format]) => {
      const {difference, percent} = data.changesFromPrevious[key];
      return `<article><span>${label}</span><strong>${format(current[key])}</strong>
        <small>${signed(difference)}${format(Math.abs(difference))}
          ${percent == null ? '· Mốc trước bằng 0' : `(${signed(percent)}${nf.format(Math.abs(percent))}%)`}
          <span>so với kỳ trước</span>
        </small></article>`;
    }).join('')}</div>
    <div class="ai-analysis">
      <div class="ai-analysis-label"><span aria-hidden="true">✦</span> ${data.source === 'deepseek' ? 'Phân tích bằng DeepSeek' : 'Tóm tắt từ dữ liệu cửa hàng'}</div>
      ${data.note ? `<p class="ai-analysis-note">${e(data.note)}</p>` : ''}
      <div class="ai-analysis-text">${aiNarrative(data.answer)}</div>
    </div>
    ${table(['Kỳ đối chiếu', 'Doanh thu', 'Sách bán', 'Hóa đơn hoàn tất', 'Chi phí nhập'], data.periods.map(period => [
      cell(day(period.from) + ' – ' + day(period.to)), ...metrics.map(([key, , format]) => cell(format(period[key])))
    ]))}`;
}

function bindAiReport() {
  const form = $('#ai-report-form');
  const output = $('#ai-report-output');
  const submit = $('button[type="submit"]', form);
  form.addEventListener('submit', async evt => {
    evt.preventDefault();
    if (submit.disabled) return;
    const body = new URLSearchParams(new FormData(form));
    submit.disabled = true;
    submit.textContent = 'Đang phân tích…';
    output.hidden = false;
    output.innerHTML = '<p class="ai-report-loading" role="status">Đang đối chiếu số liệu theo tháng…</p>';
    try {
      const result = await api('ai/report', body);
      if (output.isConnected) output.innerHTML = aiReportResult(result);
    } catch (err) {
      if (output.isConnected) output.innerHTML = `<p class="ai-report-loading" role="alert">${e(err.message)}</p>`;
    } finally {
      submit.disabled = false;
      submit.innerHTML = '<span aria-hidden="true">✦</span> Tạo phân tích';
    }
  });
}
