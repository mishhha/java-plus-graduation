const $ = (s) => document.querySelector(s);
const userId = () => $('#user-id').value;
const getId = (x) => x.id ?? x.eventId;

let eventsCache = new Map(); // id -> мероприятие (для обогащения рекомендаций)

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
    const scoreLine = score != null
        ? `<p class="score"> Рекомендуем (${(score * 100).toFixed(0)}%)</p>`
        : '';
    return `
<div class="card">
    <h3>${title}</h3>
    <p class="annotation">${e.annotation || ''}</p>
    <p> рейтинг: ${rating}</p>
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
        const events = await api('/events?from=0&size=100');
        eventsCache = new Map(events.map(e => [getId(e), e])); // запоминаем для рекомендаций
        $('#events').innerHTML = events.map(e => card(e)).join('') || '<p>Пока пусто</p>';
    } catch (err) {
        toast('Ошибка загрузки мероприятий: ' + err.message, true);
    }
}

async function loadRecommendations() {
    const box = $('#recommendations');
    try {
        const recs = await api('/events/recommendations');
        if (!recs || !recs.length) {
            box.innerHTML = '<p>Рекомендаций пока нет — посмотри или лайкни мероприятия!</p>';
            return;
        }
        // Склейка: id+score из gRPC + title/annotation/rating из кэша мероприятий
        box.innerHTML = recs.map(r => {
            const id = getId(r);
            const event = eventsCache.get(id) || { id };
            return card({ ...event, id }, r.score);
        }).join('');
    } catch (err) {
        box.innerHTML = '<p>Рекомендации недоступны</p>';
    }
}

async function refresh() {
    await loadEvents();        // сначала события (заполняет кэш)
    await loadRecommendations(); // потом рекомендации (используют кэш)
}

async function viewEvent(id) {
    try {
        const event = await api(`/events/${id}`); // этот же вызов фиксирует VIEW в статистике
        openModal(event);
        toast('Просмотр засчитан 👀');
    } catch (e) { toast(e.message, true); }
}

async function registerEvent(id) {
    try { await api(`/users/${userId()}/requests?eventId=${id}`, { method: 'POST' }); toast('Заявка создана ✅'); refresh(); }
    catch (e) { toast(e.message, true); }
}

async function likeEvent(id) {
    try { await api(`/events/${id}/like`, { method: 'PUT' }); toast('Лайк отправлен ❤️'); refresh(); }
    catch (e) { toast('Лайк не прошёл: ' + e.message, true); }
}

function openModal(e) {
    const date = (e.eventDate || '').replace('T', ' ');
    $('#modal-body').innerHTML = `
        <h2>${e.title || 'Мероприятие'}</h2>
        <p class="modal-annotation">${e.annotation || ''}</p>
        <div class="row"><span>Категория</span><span>${e.category?.name || '—'}</span></div>
        <div class="row"><span>Дата и время</span><span>${date}</span></div>
        <div class="row"><span>Рейтинг</span><span>⭐ ${e.rating ?? 0}</span></div>
        <div class="row"><span>Вход</span><span>${e.paid ? 'Платный' : 'Бесплатный'}</span></div>
        <div class="row"><span>Лимит участников</span><span>${e.participantLimit ? e.participantLimit : 'без лимита'}</span></div>
        <div class="row"><span>Координаты</span><span>${e.location ? e.location.lat + ', ' + e.location.lon : '—'}</span></div>
        <div class="row"><span>Организатор</span><span>${e.initiator?.name || '—'}</span></div>
        <h3>&gt; Описание</h3>
        <p class="modal-description">${e.description || ''}</p>
    `;
    $('#modal-overlay').classList.remove('hidden');
}

function closeModal() {
    $('#modal-overlay').classList.add('hidden');
    refresh(); // после закрытия обновляем списки (рейтинг мог измениться)
}

$('#user-id').addEventListener('change', refresh);
refresh();

$('#modal-overlay').addEventListener('click', (ev) => {
    if (ev.target.id === 'modal-overlay') closeModal();
});
document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape' && !$('#modal-overlay').classList.contains('hidden')) closeModal();
});