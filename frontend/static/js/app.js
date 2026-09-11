const $ = (s) => document.querySelector(s);
const userId = () => $('#user-id').value;
const getId = (x) => x.id ?? x.eventId;

async function api(path, options = {}) {
    const res = await fetch(path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'X-EWM-USER-ID': userId(),
            ...(options.headers || {}),
        },
    });
    const text = await res.text();
    if (!res.ok) {
        let message = res.statusText || 'Ошибка запроса';
        try { message = JSON.parse(text).message || message; } catch (e) { /* тело не JSON */ }
        throw new Error(message);
    }
    return text ? JSON.parse(text) : null;
}

function toast(msg, isError = false) {
    const el = $('#toast');
    el.textContent = msg;
    el.className = 'toast' + (isError ? ' error' : '');
    setTimeout(() => el.classList.add('hidden'), 2500);
}

function card(e, score) {
    const id = getId(e);
    const title = e.title || e.name || `Мероприятие #${id}`;
    const rating = e.rating ?? 0;
    const scoreLine = score != null ? `<p class="score">score: ${score.toFixed(3)}</p>` : '';
    return `
    <div class="card">
      <h3>${title}</h3>
      <p class="annotation">${e.annotation || ''}</p>
      <p>⭐ рейтинг: ${rating}</p>
      ${scoreLine}
      <div class="actions">
        <button onclick="viewEvent(${id})">👁 Просмотр</button>
        <button onclick="registerEvent(${id})">✅ Записаться</button>
        <button onclick="likeEvent(${id})">❤️ Лайк</button>
      </div>
    </div>`;
}

async function loadEvents() {
    try {
        const events = await api('/events?from=0&size=20');
        $('#events').innerHTML = events.map(e => card(e)).join('') || '<p>Пока пусто</p>';
    } catch (err) {
        toast('Ошибка загрузки мероприятий: ' + err.message, true);
    }
}

async function loadRecommendations() {
    try {
        const recs = await api('/events/recommendations');
        $('#recommendations').innerHTML =
            recs.map(r => card(r, r.score)).join('') ||
            '<p>Рекомендаций пока нет — посмотри или лайкни мероприятия!</p>';
    } catch (err) {
        $('#recommendations').innerHTML = '<p>Рекомендации недоступны</p>';
    }
}

function refresh() {
    loadEvents();
    loadRecommendations();
}

async function viewEvent(id) {
    try {
        await api(`/events/${id}`);
        toast('Просмотр засчитан 👀');
        refresh();
    } catch (e) { toast(e.message, true); }
}

async function registerEvent(id) {
    try {
        await api(`/users/${userId()}/requests?eventId=${id}`, { method: 'POST' });
        toast('Заявка создана ✅');
        refresh();
    } catch (e) { toast(e.message, true); }
}

async function likeEvent(id) {
    try {
        await api(`/events/${id}/like`, { method: 'PUT' });
        toast('Лайк отправлен ❤️');
        refresh();
    } catch (e) { toast('Лайк не прошёл: ' + e.message, true); }
}

$('#user-id').addEventListener('change', refresh);
refresh();