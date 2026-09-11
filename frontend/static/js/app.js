/* ========== Хелперы ========== */
const $ = (s) => document.querySelector(s);
let currentUserId = null;
const userId = () => currentUserId;
const getId = (x) => x.id ?? x.eventId;
const liked = new Set();       // сердечки, нажатые в этой сессии
let eventsCache = new Map();   // id -> мероприятие (для обогащения рекомендаций)

/* ========== Cookie ========== */
function getCookie(name) {
    const m = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return m ? decodeURIComponent(m[1]) : null;
}
function setCookie(name, value, days) {
    const d = new Date(Date.now() + days * 86400000).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; expires=${d}; path=/`;
}
function deleteCookie(name) {
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
}

/* ========== API ========== */
async function api(path, options = {}) {
    const res = await fetch(path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(userId() != null ? { 'X-EWM-USER-ID': userId() } : {}),
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

/* ========== Тосты ========== */
function toast(msg, isError = false) {
    const el = $('#toast');
    el.textContent = msg;
    el.className = 'toast' + (isError ? ' error' : '');
    setTimeout(() => el.classList.add('hidden'), 2500);
}

/* ========== Карточка ========== */
function card(e, score) {
    const id = getId(e);
    const title = e.title || e.name || `Мероприятие #${id}`;
    const rating = e.rating ?? 0;
    const isLiked = liked.has(id);
    const scoreLine = score != null
        ? `<p class="score">🌟 Рекомендуем (${(score * 100).toFixed(0)}%)</p>`
        : '';
    return `
<div class="card">
    <button class="like-btn ${isLiked ? 'liked' : ''}" onclick="likeEvent(${id})" title="Лайкнуть мероприятие">${isLiked ? '♥' : '♡'}</button>
    <h3>${title}</h3>
    <p class="annotation">${e.annotation || ''}</p>
    <p>⭐ рейтинг: ${rating}</p>
    ${scoreLine}
    <div class="actions">
        <button onclick="viewEvent(${id})">👁 Просмотр</button>
        <button onclick="registerEvent(${id})">✅ Записаться</button>
    </div>
</div>`;
}

/* ========== Модалка мероприятия ========== */
function openModal(e) {
    const date = (e.eventDate || '').replace('T', ' ');
    $('#modal-body').innerHTML = `
        <h2>${e.title || ''}</h2>
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
    refresh();
}

/* ========== Загрузка данных ========== */
async function loadEvents() {
    try {
        const events = await api('/events?from=0&size=100');
        eventsCache = new Map(events.map(e => [getId(e), e]));
        $('#events').innerHTML = events.map(e => card(e)).join('') || '<p>Пока пусто</p>';
    } catch (err) {
        toast('Ошибка загрузки мероприятий: ' + err.message, true);
    }
}

async function loadRecommendations() {
    const box = $('#recommendations');
    if (currentUserId == null) {
        box.innerHTML = '<p>🔑 Войдите, чтобы получить рекомендации</p>';
        return;
    }
    try {
        const recs = await api('/events/recommendations');
        if (!recs || !recs.length) {
            box.innerHTML = '<p>Рекомендаций пока нет — посмотри или лайкни мероприятия!</p>';
            return;
        }
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
    await loadEvents();
    await loadRecommendations();
}

/* ========== Действия ========== */
async function viewEvent(id) {
    try {
        const event = await api(`/events/${id}`);
        openModal(event);
        toast('Просмотр засчитан 👀');
    } catch (e) { toast(e.message, true); }
}

async function registerEvent(id) {
    if (currentUserId == null) { toast('Сначала войдите 🔑', true); showAuthOverlay(); return; }
    try {
        await api(`/users/${userId()}/requests?eventId=${id}`, { method: 'POST' });
        toast('Заявка создана ✅');
        refresh();
    } catch (e) { toast(e.message, true); }
}

async function likeEvent(id) {
    if (currentUserId == null) { toast('Сначала войдите 🔑', true); showAuthOverlay(); return; }
    try {
        await api(`/events/${id}/like`, { method: 'PUT' });
        liked.add(id);
        toast('Лайк отправлен ♥');
        refresh();
    } catch (e) { toast('Лайк не прошёл: ' + e.message, true); }
}

/* ========== Регистрация, вход, гость ========== */
let currentUserName = null;

function showAuthError(msg) { const el = $('#auth-error'); el.textContent = msg; el.classList.remove('hidden'); }
function hideAuthError() { $('#auth-error').classList.add('hidden'); }
function showAuthOverlay() { $('#auth-overlay').classList.remove('hidden'); }
function hideAuthOverlay() { $('#auth-overlay').classList.add('hidden'); }

function renderUserBadge() {
    const label = $('#user-label');
    const btn = $('#user-logout');
    if (currentUserId != null) {
        label.textContent = `Вы — ${currentUserName || 'пользователь #' + currentUserId}`;
        btn.textContent = '⎋ Выйти';
        btn.title = 'Выйти и войти под другим пользователем';
        btn.onclick = logout;
    } else {
        label.textContent = 'Гость';
        btn.textContent = '🔑 Войти';
        btn.title = 'Войти или зарегистрироваться';
        btn.onclick = () => { hideAuthError(); showAuthOverlay(); };
    }
}

async function fetchUserName(id) {
    try {
        const list = await api(`/admin/users?ids=${id}`);
        if (Array.isArray(list) && list.length) return list[0].name;
    } catch (e) { /* имя недоступно — не критично */ }
    return null;
}

async function registerUser(name, email) {
    const user = await api('/admin/users', { method: 'POST', body: JSON.stringify({ name, email }) });
    setCookie('ewm_user_id', user.id, 365);
    setCookie('ewm_user_name', name, 365);
    return user.id;
}

async function loginById(id) {
    const list = await api(`/admin/users?ids=${id}`);
    if (!Array.isArray(list) || list.length === 0) throw new Error(`Пользователь с ID ${id} не найден`);
    setCookie('ewm_user_id', list[0].id, 365);
    setCookie('ewm_user_name', list[0].name || '', 365);
    return list[0].id;
}

function logout() {
    deleteCookie('ewm_user_id');
    deleteCookie('ewm_user_name');
    currentUserId = null;
    currentUserName = null;
    renderUserBadge();
    hideAuthError();
    showAuthOverlay();
    refresh();
}

function closeAuth() {
    hideAuthError();
    hideAuthOverlay();
    toast('Вы просматриваете как гость 👤');
}

async function boot() {
    const stored = getCookie('ewm_user_id');
    if (stored) {
        currentUserId = Number(stored);
        currentUserName = getCookie('ewm_user_name') || null;
        if (!currentUserName) currentUserName = await fetchUserName(currentUserId);
        hideAuthOverlay();
    } else {
        currentUserId = null;
        currentUserName = null;
        showAuthOverlay(); // можно закрыть крестиком и остаться гостем
    }
    renderUserBadge();
    refresh();
}

$('#register-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    hideAuthError();
    const name = $('#reg-name').value.trim();
    const email = $('#reg-email').value.trim();
    if (name.length < 2) { showAuthError('Имя должно содержать минимум 2 символа'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { showAuthError('Некорректный email'); return; }
    try {
        currentUserId = await registerUser(name, email);
        currentUserName = name;
        hideAuthOverlay();
        renderUserBadge();
        toast(`Добро пожаловать, ${name}!`);
        refresh();
    } catch (e) { showAuthError(e.message); }
});

$('#login-btn').addEventListener('click', async () => {
    hideAuthError();
    const id = Number($('#login-id').value);
    if (!id || id < 1) { showAuthError('Введите корректный ID'); return; }
    try {
        currentUserId = await loginById(id);
        currentUserName = getCookie('ewm_user_name') || null;
        hideAuthOverlay();
        renderUserBadge();
        toast(`С возвращением, ${currentUserName || 'пользователь #' + currentUserId}!`);
        refresh();
    } catch (e) { showAuthError(e.message); }
});

$('#auth-close').addEventListener('click', closeAuth);

/* ========== Обработчики формы ========== */
$('#register-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    hideAuthError();
    const name = $('#reg-name').value.trim();
    const email = $('#reg-email').value.trim();
    if (name.length < 2) { showAuthError('Имя должно содержать минимум 2 символа'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { showAuthError('Некорректный email'); return; }
    try {
        currentUserId = await registerUser(name, email);
        toast(`Добро пожаловать, ${name}!`);
        enterApp();
    } catch (e) { showAuthError(e.message); }
});

$('#login-btn').addEventListener('click', async () => {
    hideAuthError();
    const id = Number($('#login-id').value);
    if (!id || id < 1) { showAuthError('Введите корректный ID'); return; }
    try {
        currentUserId = await loginById(id);
        toast(`С возвращением, пользователь #${id}!`);
        enterApp();
    } catch (e) { showAuthError(e.message); }
});

$('#user-logout').addEventListener('click', logout);

/* ========== Модалка: закрытие ========== */
$('#modal-overlay').addEventListener('click', (ev) => {
    if (ev.target.id === 'modal-overlay') closeModal();
});
document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape' && !$('#modal-overlay').classList.contains('hidden')) closeModal();
});

/* ========== Cookie-consent гейт ========== */
const CONSENT_KEY = 'ewm_cookie_consent';

$('#cookie-accept').addEventListener('click', () => {
    localStorage.setItem(CONSENT_KEY, 'accepted');
    $('#cookie-overlay').classList.add('hidden');
    boot();
});
$('#cookie-decline').addEventListener('click', () => {
    localStorage.setItem(CONSENT_KEY, 'declined');
    $('#cookie-overlay').classList.add('hidden');
    $('#access-denied').classList.remove('hidden');
});
$('#cookie-retry').addEventListener('click', () => {
    localStorage.removeItem(CONSENT_KEY);
    $('#access-denied').classList.add('hidden');
    $('#cookie-overlay').classList.remove('hidden');
});

/* ========== Точка входа ========== */
const consent = localStorage.getItem(CONSENT_KEY);
if (consent === 'accepted') {
    boot();
} else if (consent === 'declined') {
    $('#access-denied').classList.remove('hidden');
} else {
    $('#cookie-overlay').classList.remove('hidden');
}