/* ========== Хелперы ========== */
const $ = (s) => document.querySelector(s);
const getId = (x) => x.id ?? x.eventId;

let currentUserId = null;
let currentRole = 'user';
let adminPage = 0;
let adminStates = '';
const ADMIN_SIZE = 5;

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
};

function getCookie(name) {
    const m = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return m ? decodeURIComponent(m[1]) : null;
}
function deleteCookie(name) {
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
}

async function api(path, options = {}) {
    const res = await fetch(path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(currentUserId != null ? { 'X-EWM-USER-ID': currentUserId } : {}),
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

/* ========== Доступ ========== */
function initAccess() {
    currentUserId = Number(getCookie('ewm_user_id')) || null;
    currentRole = getCookie('ewm_role') || 'user';
    if (!currentUserId || currentRole !== 'admin') {
        $('#admin-denied').classList.remove('hidden');
        $('#admin-panel').classList.add('hidden');
        return false;
    }
    const name = getCookie('ewm_user_name');
    $('#admin-user-label').textContent = `Вы — ${name || 'пользователь #' + currentUserId} (админ)`;
    $('#admin-denied').classList.add('hidden');
    $('#admin-panel').classList.remove('hidden');
    return true;
}

/* ========== Список мероприятий ========== */
async function loadAdminEvents() {
    const from = adminPage * ADMIN_SIZE;
    let url = `/admin/events?from=${from}&size=${ADMIN_SIZE}`;
    if (adminStates) url += `&states=${adminStates}`;
    try {
        const list = await api(url);
        $('#admin-events').innerHTML = list.length
            ? list.map(adminCard).join('')
            : '<p>Пусто</p>';
        $('#admin-page').textContent = `стр. ${adminPage + 1}`;
        $('#admin-prev').disabled = adminPage === 0;
        $('#admin-next').disabled = list.length < ADMIN_SIZE;
    } catch (e) {
        $('#admin-events').innerHTML = `<p>Ошибка загрузки: ${e.message}</p>`;
    }
}

function adminCard(e) {
    const id = getId(e);
    return `
<div class="card admin-card">
    <h3>${e.title || `Мероприятие #${id}`}</h3>
    <p class="annotation">${e.annotation || ''}</p>
    <p>Статус: <span class="state-${(e.state || '').toLowerCase()}">${e.state || '—'}</span></p>
    <p>${(e.eventDate || '').replace('T', ' ')}</p>
    <div class="actions">
        ${e.state !== 'PUBLISHED' ? `<button onclick="publishEvent(${id})"> Опубликовать</button>` : ''}
        ${e.state !== 'CANCELED' ? `<button onclick="rejectEvent(${id})"> Отклонить</button>` : ''}
    </div>
</div>`;
}

async function publishEvent(id) {
    try {
        await api(`/admin/events/${id}`, { method: 'PATCH', body: JSON.stringify({ stateAction: 'PUBLISH_EVENT' }) });
        toast('Мероприятие опубликовано');
        loadAdminEvents();
    } catch (e) { toast(e.message, true); }
}

async function rejectEvent(id) {
    try {
        await api(`/admin/events/${id}`, { method: 'PATCH', body: JSON.stringify({ stateAction: 'REJECT_EVENT' }) });
        toast('Мероприятие отклонено');
        loadAdminEvents();
    } catch (e) { toast(e.message, true); }
}

/* ========== Слушатели ========== */
document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        adminStates = btn.dataset.states;
        adminPage = 0;
        loadAdminEvents();
    });
});
$('#admin-prev').addEventListener('click', () => {
    if (adminPage > 0) { adminPage--; loadAdminEvents(); }
});
$('#admin-next').addEventListener('click', () => {
    adminPage++;
    loadAdminEvents();
});
$('#admin-logout').addEventListener('click', () => {
    deleteCookie('ewm_user_id');
    deleteCookie('ewm_user_name');
    deleteCookie('ewm_role');
    location.href = '/index.html';
});

/* ========== Старт ========== */
if (initAccess()) loadAdminEvents();