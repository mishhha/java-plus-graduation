/* ========== Хелперы ========== */
const $ = (s) => document.querySelector(s);
const getId = (x) => x.id ?? x.eventId;

let currentUserId = null;
let currentUserName = null;
const userId = () => currentUserId;

const liked = new Set();       // сердечки, нажатые в этой сессии
let eventsCache = new Map();   // id -> мероприятие

const CONSENT_KEY = 'ewm_cookie_consent';

/* ========== Фирменные терминальные глифы (для нового функционала) ========== */
const GLYPH = {
    view:   '◉',    // просмотр
    ok:     '✓',    // одобрить / записаться / опубликовать
    reject: '✕',    // отклонить / отменить
    like:   '♥',    // лайк (нажат)
    like0:  '♡',    // лайк (не нажат)
    rating: '★',    // рейтинг
    rec:    '»',    // рекомендация
    my:     '▣',    // мои события
    req:    '▤',    // мои участия
    list:   '▦',    // список мероприятий
    open:   '◈',    // открыть детали
    admin:  '#',    // роль админа
    user:   '○',    // роль пользователя
    login:  '>',    // вход
    date:   '◷',    // дата и время
    crowd:  '∑',    // участники
    state:  '●',    // статус
    create: '[+]',  // создать
    info:   '[i]',  // информация
    denied: '[!]',  // доступ запрещён
    wait:   '…',    // ожидание модерации
    yes:    '[Y]',  // да
    no:     '[N]',  // нет
    comm:   '//',   // комментарии (значок на карточке)
    edit:   '✎',    // редактировать
    del:    '✕',    // удалить (reuse существующего символа, но для контекста комментариев)
};

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

/* ========== Доступ: гость vs пользователь ========== */
function isGuest() {
    return currentUserId == null;
}

function renderUserBadge() {
    const label = $('#user-label');
    const btn = $('#user-logout');
    if (isGuest()) {
        label.textContent = 'Гость';
        btn.textContent = 'Войти';
        btn.title = 'Войти или зарегистрироваться';
        btn.onclick = () => { hideAuthError(); showAuthOverlay(); };
    } else {
        const suffix = currentRole === 'admin' ? ' (админ)' : '';
        label.textContent = `Вы — ${currentUserName || 'пользователь #' + currentUserId}${suffix}`;
        btn.textContent = '⎋ Выйти';
        btn.title = 'Выйти и войти под другим пользователем';
        btn.onclick = logout;
    }
}

function applyAccessRules() {
    renderUserBadge();
    const guest = isGuest();
    $('#section-recommendations').classList.toggle('hidden', guest);
    $('#section-my').classList.toggle('hidden', guest);
    $('#create-event-btn').classList.toggle('hidden', guest);
    $('#admin-link').classList.toggle('hidden', !(currentRole === 'admin' && !guest));
}

function requireAuth() {
    toast('Войдите или зарегистрируйтесь — действие доступно только пользователям');
    hideAuthError();
    showAuthOverlay();
}

/* ========== Карточки ========== */
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
        <button onclick="registerEvent(${id})">${GLYPH.ok} Записаться</button>
        <button onclick="openEventComments(${id})">${GLYPH.comm}</button>
    </div>
</div>`;
}

function createdCard(e) {
    const id = getId(e);
    const limit = e.participantLimit ?? 0;
    const confirmed = e.confirmedRequests ?? 0;
    const stateLine = STATE_LABEL[e.state] || e.state || '—';
    return `
<div class="card">
    <h3>${e.title || `Мероприятие #${id}`}</h3>
    <p class="annotation">${e.annotation || ''}</p>
    <p>${stateLine}</p>
    <p>Участники: <span class="mono">${progressBar(confirmed, limit)}</span></p>
    <div class="actions">
        <button onclick="openMyEvent(${id})">Открыть</button>
    </div>
</div>`;
}

/* ========== Кабинет организатора ========== */
const STATE_LABEL = {
    PENDING:  ' Ожидает модерации',
    PUBLISHED:' Опубликовано',
    CANCELED: ' Отменено',
};

function progressBar(filled, total) {
    if (!total) return '[ без лимита ]';
    const width = 12;
    const done = Math.round((Math.min(filled, total) / total) * width);
    return '[' + '█'.repeat(done) + '░'.repeat(width - done) + '] ' + filled + '/' + total;
}

async function openMyEvent(id) {
    if (isGuest()) { requireAuth(); return; }
    let e;
    try {
        e = await api(`/users/${currentUserId}/events/${id}`); // владелец: state + confirmedRequests
    } catch (err) {
        try { e = await api(`/events/${id}`); }                // фолбэк: публичная карточка
        catch (e2) { toast(e2.message, true); return; }
    }
    renderMyEventModal(e);
}

function renderMyEventModal(e) {
    const limit = e.participantLimit ?? 0;
    const confirmed = e.confirmedRequests ?? 0;
    $('#modal-body').innerHTML = `
        <h2>${e.title || ''}</h2>
        <p class="modal-annotation">${e.annotation || ''}</p>
        <div class="row"><span>Статус модерации</span><span class="state-${(e.state || '').toLowerCase()}">${STATE_LABEL[e.state] || e.state || '—'}</span></div>
        <div class="row"><span>Участники</span><span class="mono">${progressBar(confirmed, limit)}</span></div>
        <div class="row"><span>Подтверждено</span><span>${confirmed}</span></div>
        <div class="row"><span>Лимит</span><span>${limit ? limit : 'без лимита'}</span></div>
        <div class="row"><span>Категория</span><span>${e.category?.name || '—'}</span></div>
        <div class="row"><span>Дата и время</span><span>${(e.eventDate || '').replace('T', ' ')}</span></div>
        <div class="row"><span>Рейтинг</span><span>⭐ ${e.rating ?? 0}</span></div>
        <h3>＞ Описание</h3>
        <p class="modal-description">${e.description || ''}</p>
    `;
    $('#modal-overlay').classList.remove('hidden');
}

function requestCard(r) {
    const id = r.event;
    const ev = eventsCache.get(id) || {};
    return `
<div class="card">
    <h3>${ev.title || `Мероприятие #${id}`}</h3>
    <p class="annotation">${ev.annotation || ''}</p>
    <p> Статус участия: ${r.status || '—'}</p>
    <div class="actions">
        <button onclick="viewEvent(${id})">👁 Просмотр</button>
    </div>
</div>`;
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
    if (isGuest()) { $('#recommendations').innerHTML = ''; return; }
    try {
        const recs = await api('/events/recommendations');
        $('#recommendations').innerHTML = recs.map(r => {
            const id = getId(r);
            const event = eventsCache.get(id) || { id };
            return card({ ...event, id }, r.score);
        }).join('') || '<p>Рекомендаций пока нет — посмотри или лайкни мероприятия!</p>';
    } catch (err) {
        $('#recommendations').innerHTML = '<p>Рекомендации недоступны</p>';
    }
}

async function loadMy() {
    if (isGuest()) { $('#my-created').innerHTML = ''; $('#my-events').innerHTML = ''; return; }
    try {
        const mine = await api(`/users/${currentUserId}/events?from=0&size=50`);
        // догружаем полные версии (state, confirmedRequests, participantLimit) параллельно
        const full = await Promise.all((mine || []).map(e =>
            api(`/users/${currentUserId}/events/${getId(e)}`).catch(() => e)
        ));
        $('#my-created').innerHTML = full.length
            ? full.map(createdCard).join('')
            : '<p>У вас пока нет созданных мероприятий</p>';
    } catch (e) {
        $('#my-created').innerHTML = '<p>Не удалось загрузить мои события</p>';
    }
    try {
        const reqs = await api(`/users/${currentUserId}/requests`);
        $('#my-events').innerHTML = (reqs && reqs.length)
            ? reqs.map(requestCard).join('')
            : '<p>Вы пока ни в чём не участвуете — запишитесь на мероприятие!</p>';
    } catch (e) {
        $('#my-events').innerHTML = '<p>Не удалось загрузить участия</p>';
    }
}

async function refresh() {
    applyAccessRules();
    await loadEvents();
    await loadRecommendations();
    await loadMy();
}

/* ========== Модалка карточки мероприятия ========== */
function openModal(e, activeTab = 'details') {
    const date = (e.eventDate || '').replace('T', ' ');

    // Вкладка "Детали"
    $('#tab-details').innerHTML = `
        <h2>${e.title || ''}</h2>
        <p class="modal-annotation">${e.annotation || ''}</p>
        <div class="row"><span>Категория</span><span>${e.category?.name || '—'}</span></div>
        <div class="row"><span>Дата и время</span><span>${date}</span></div>
        <div class="row"><span>Рейтинг</span><span>★ ${e.rating ?? 0}</span></div>
        <div class="row"><span>Вход</span><span>${e.paid ? 'Платный' : 'Бесплатный'}</span></div>
        <div class="row"><span>Лимит участников</span><span>${e.participantLimit ? e.participantLimit : 'без лимита'}</span></div>
        <div class="row"><span>Координаты</span><span>${e.location ? e.location.lat + ', ' + e.location.lon : '—'}</span></div>
        <div class="row"><span>Организатор</span><span>${e.initiator?.name || '—'}</span></div>
        <h3>＞ Описание</h3>
        <p class="modal-description">${e.description || ''}</p>
    `;

    // Вкладка "Комментарии" (пока заглушка, реализуем на Шаге 3)
    $('#tab-comments').innerHTML = '<p>Комментариев пока нет</p>';

    // Активируем нужную вкладку
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelector(`.tab[data-tab="${activeTab}"]`).classList.add('active');
    $('#tab-details').classList.toggle('hidden', activeTab !== 'details');
    $('#tab-comments').classList.toggle('hidden', activeTab !== 'comments');

    $('#modal-overlay').classList.remove('hidden');
}

function closeModal() {
    $('#modal-overlay').classList.add('hidden');
    refresh();
}

/* ========== Действия над мероприятием ========== */
async function viewEvent(id) {
    if (isGuest()) { requireAuth(); return; }
    try {
        const event = await api(`/events/${id}`);
        openModal(event);
        toast('Просмотр засчитан 👀');
    } catch (e) { toast(e.message, true); }
}

async function openEventComments(id) {
    if (isGuest()) { requireAuth(); return; }
    try {
        const event = await api(`/events/${id}`);
        openModal(event, 'comments'); // сразу на вкладку комментариев
        toast('Просмотр засчитан ◉');
    } catch (e) { toast(e.message, true); }
}

async function registerEvent(id) {
    if (isGuest()) { requireAuth(); return; }
    try {
        await api(`/users/${userId()}/requests?eventId=${id}`, { method: 'POST' });
        toast('Заявка создана ✅');
        refresh();
    } catch (e) { toast(e.message, true); }
}

async function likeEvent(id) {
    if (isGuest()) { requireAuth(); return; }
    try {
        await api(`/events/${id}/like`, { method: 'PUT' });
        liked.add(id);
        toast('Лайк отправлен ❤️');
        refresh();
    } catch (e) { toast('Лайк не прошёл: ' + e.message, true); }
}

async function loadCategories() {
    const sel = $('#ce-category');
    try {
        const cats = await api('/categories?from=0&size=100');
        sel.innerHTML = cats.length
            ? cats.map(c => `<option value="${c.id}">${c.name}</option>`).join('')
            : '<option value="">— нет категорий —</option>';
    } catch (e) {
        sel.innerHTML = '<option value="">— не удалось загрузить —</option>';
    }
}

function openCategoryInput() {
    $('#cat-new-row').classList.remove('hidden');
    $('#ce-category-new').value = '';
    $('#ce-category-new').focus();
}

function cancelCategory() {
    $('#cat-new-row').classList.add('hidden');
}

async function submitCategory() {
    const name = $('#ce-category-new').value.trim();
    if (!name) { showCreateError('Введите название категории'); return; }
    try {
        const cat = await api('/admin/categories', {
            method: 'POST',
            body: JSON.stringify({ name }),
        });
        toast('Категория создана ✅');
        await loadCategories();
        $('#ce-category').value = cat.id;   // сразу выбираем новую категорию
        cancelCategory();
    } catch (e) {
        showCreateError('Не удалось создать категорию: ' + e.message);
    }
}

$('#ce-category-new').addEventListener('keydown', (ev) => {
    if (ev.key === 'Enter') { ev.preventDefault(); submitCategory(); }
});

/* ========== Создание мероприятия ========== */
function openCreateModal() {
    loadCategories();
    if (isGuest()) { requireAuth(); return; }
    hideCreateError();
    $('#create-overlay').classList.remove('hidden');
}
function closeCreateModal() {
    $('#create-overlay').classList.add('hidden');
}
function showCreateError(msg) {
    const el = $('#create-error');
    el.textContent = msg;
    el.classList.remove('hidden');
}
function hideCreateError() {
    $('#create-error').classList.add('hidden');
}

/* ========== Регистрация / вход / гость ========== */
function showAuthError(msg) { const el = $('#auth-error'); el.textContent = msg; el.classList.remove('hidden'); }
function hideAuthError() { $('#auth-error').classList.add('hidden'); }
function showAuthOverlay() { $('#auth-overlay').classList.remove('hidden'); }
function hideAuthOverlay() { $('#auth-overlay').classList.add('hidden'); }

async function fetchUserName(id) {
    try {
        const list = await api(`/admin/users?ids=${id}`);
        if (Array.isArray(list) && list.length) return list[0].name;
    } catch (e) { /* не критично */ }
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

function enterApp() {
    hideAuthOverlay();
    refresh();
}

function closeAuth() {
    hideAuthError();
    hideAuthOverlay();
    toast('Вы просматриваете как гость');
    refresh();
}

function logout() {
    deleteCookie('ewm_user_id');
    deleteCookie('ewm_user_name');
    deleteCookie('ewm_role');
    currentUserId = null;
    currentUserName = null;
    currentRole = 'user';
    liked.clear();
    showAuthOverlay();
    refresh();
}

async function boot() {
    currentRole = getCookie('ewm_role') || 'user';
    const stored = getCookie('ewm_user_id');
    if (stored) {
        currentUserId = Number(stored);
        currentUserName = getCookie('ewm_user_name') || null;
        if (!currentUserName) currentUserName = await fetchUserName(currentUserId);
        enterApp();
    } else {
        currentUserId = null;
        currentUserName = null;
        showAuthOverlay();
        refresh();
    }
}

/* ========== Роль ========== */
let currentRole = 'user';

function captureRole() {
    const role = document.querySelector('input[name="role"]:checked')?.value || 'user';
    setCookie('ewm_role', role, 365);
    return role;
}

/* ========== Слушатели ========== */
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const target = tab.dataset.tab;
        $('#tab-details').classList.toggle('hidden', target !== 'details');
        $('#tab-comments').classList.toggle('hidden', target !== 'comments');
    });
});

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

$('#auth-close').addEventListener('click', closeAuth);

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
        currentRole = captureRole();
        enterApp();
        toast(`Добро пожаловать, ${name}!`);
    } catch (e) { showAuthError(e.message); }
});

$('#login-btn').addEventListener('click', async () => {
    hideAuthError();
    const id = Number($('#login-id').value);
    if (!id || id < 1) { showAuthError('Введите корректный ID'); return; }
    try {
        currentUserId = await loginById(id);
        currentUserName = getCookie('ewm_user_name') || null;
        currentRole = captureRole();
        enterApp();
        toast(`С возвращением, ${currentUserName || 'пользователь #' + currentUserId}!`);
    } catch (e) { showAuthError(e.message); }
});

$('#create-event-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    hideCreateError();
    const body = {
        title: $('#ce-title').value.trim(),
        annotation: $('#ce-annotation').value.trim(),
        description: $('#ce-description').value.trim(),
        category: Number($('#ce-category').value) || null,
        eventDate: $('#ce-date').value.trim(),
        location: { lat: 55.75, lon: 37.62 },
        paid: $('#ce-paid').checked,
        participantLimit: Number($('#ce-limit').value),
        requestModeration: false,
    };
    if (!$('#ce-category').value) {
        showCreateError('Сначала создайте категорию (кнопка ➕ Новая)');
        return;
    }
    if (body.title.length < 3) { showCreateError('Название — минимум 3 символа'); return; }
    if (body.annotation.length < 20 || body.description.length < 20) {
        showCreateError('Аннотация и описание — минимум 20 символов'); return;
    }
    try {
        await api(`/users/${currentUserId}/events`, { method: 'POST', body: JSON.stringify(body) });
        closeCreateModal();
        toast('Мероприятие создано! Ожидает публикации');
        refresh();
    } catch (e) { showCreateError(e.message); }
});

$('#create-overlay').addEventListener('click', (ev) => {
    if (ev.target.id === 'create-overlay') closeCreateModal();
});
$('#modal-overlay').addEventListener('click', (ev) => {
    if (ev.target.id === 'modal-overlay') closeModal();
});
document.addEventListener('keydown', (ev) => {
    if (ev.key !== 'Escape') return;
    if (!$('#create-overlay').classList.contains('hidden')) { closeCreateModal(); return; }
    if (!$('#modal-overlay').classList.contains('hidden')) { closeModal(); return; }
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