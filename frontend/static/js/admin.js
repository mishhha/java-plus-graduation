/* ========== Хелперы ========== */
const $ = (s) => document.querySelector(s);
const getId = (x) => x.id ?? x.eventId;

let currentUserId = null;
let currentUserName = null;
const userId = () => currentUserId;

let currentEventId = null;
let commentPage = 0;
let commentsCache = new Map();
const COMMENT_SIZE = 5;

let adminPage = 0;
let adminStates = '';
const ADMIN_SIZE = 5;
const adminCache = new Map();

/* ========== Фирменные терминальные глифы ========== */
const GLYPH = {
    yes:    '[Y]',
    no:     '[N]',
    menu:   '⋮',
    view:   '◉',
    ok:     '✓',
    reject: '✕',
    like:   '♥',
    like0:  '♡',
    rating: '★',
    rec:    '»',
    my:     '▣',
    req:    '▤',
    list:   '▦',
    open:   '◈',
    admin:  '#',
    user:   '○',
    login:  '>',
    date:   '◷',
    crowd:  '∑',
    state:  '●',
    create: '[+]',
    info:   '[i]',
    denied: '[!]',
    wait:   '…',
    comm:   '//',
    edit:   '✎',
    del:    '✕',
};

/* ========== Cookie ========== */
function getCookie(name) {
    const m = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
    return m ? decodeURIComponent(m[1]) : null;
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

/* ========== Гейт админа ========== */
(function guard() {
    const role = getCookie('ewm_role');
    const id = getCookie('ewm_user_id');
    if (role !== 'admin' || !id) {
        location.href = '/index.html';
        return;
    }
    currentUserId = Number(id);
    currentUserName = getCookie('ewm_user_name');
    $('#admin-user-label').textContent = `Вы — ${currentUserName || 'пользователь #' + currentUserId} (админ)`;
})();

/* ========== Список мероприятий ========== */
async function loadAdminEvents() {
    const from = adminPage * ADMIN_SIZE;
    let url = `/admin/events?from=${from}&size=${ADMIN_SIZE}`;
    if (adminStates) url += `&states=${adminStates}`;
    try {
        const list = await api(url);
        adminCache.clear();
        list.forEach(e => adminCache.set(getId(e), e));
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
    <p>${GLYPH.state} Статус: <span class="state-${(e.state || '').toLowerCase()}">${e.state || '—'}</span></p>
    <p>${GLYPH.date} ${(e.eventDate || '').replace('T', ' ')}</p>
    <div class="actions">
        ${e.state !== 'PUBLISHED' ? `<button onclick="publishEvent(${id})">${GLYPH.ok} Опубликовать</button>` : ''}
        ${e.state !== 'CANCELED' ? `<button onclick="rejectEvent(${id})">${GLYPH.reject} Отклонить</button>` : ''}
        <button onclick="openAdminEvent(${id})">${GLYPH.open} Открыть</button>
        <button onclick="openAdminComments(${id})" title="Комментарии события">${GLYPH.comm}</button>
    </div>
</div>`;
}

async function publishEvent(id) {
    try {
        await api(`/admin/events/${id}`, { method: 'PATCH', body: JSON.stringify({ stateAction: 'PUBLISH_EVENT' }) });
        toast(`${GLYPH.ok} Мероприятие опубликовано`);
        loadAdminEvents();
    } catch (e) { toast(e.message, true); }
}

async function rejectEvent(id) {
    try {
        await api(`/admin/events/${id}`, { method: 'PATCH', body: JSON.stringify({ stateAction: 'REJECT_EVENT' }) });
        toast(`${GLYPH.reject} Мероприятие отклонено`);
        loadAdminEvents();
    } catch (e) { toast(e.message, true); }
}

/* ========== Модалка события с вкладками ========== */
function openAdminEvent(id) {
    const e = adminCache.get(id);
    if (e) openModal(e, 'details');
}
function openAdminComments(id) {
    const e = adminCache.get(id);
    if (e) openModal(e, 'comments');
}

function openModal(e, activeTab = 'details') {
    currentEventId = getId(e);
    const date = (e.eventDate || '').replace('T', ' ');

    $('#tab-details').innerHTML = `
        <h2>${e.title || ''}</h2>
        <p class="modal-annotation">${e.annotation || ''}</p>
        <div class="row"><span>Категория</span><span>${e.category?.name || '—'}</span></div>
        <div class="row"><span>Дата и время</span><span>${date}</span></div>
        <div class="row"><span>Рейтинг</span><span>${GLYPH.rating} ${e.rating ?? 0}</span></div>
        <div class="row"><span>Статус</span><span class="state-${(e.state || '').toLowerCase()}">${e.state || '—'}</span></div>
        <div class="row"><span>Вход</span><span>${e.paid ? 'Платный' : 'Бесплатный'}</span></div>
        <div class="row"><span>Лимит участников</span><span>${e.participantLimit ? e.participantLimit : 'без лимита'}</span></div>
        <div class="row"><span>Организатор</span><span>${e.initiator?.name || '—'}</span></div>
        <h3>＞ Описание</h3>
        <p class="modal-description">${e.description || ''}</p>
    `;

    $('#tab-comments').innerHTML = '<div id="comments-container"></div>';

    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelector(`.tab[data-tab="${activeTab}"]`).classList.add('active');
    $('#tab-details').classList.toggle('hidden', activeTab !== 'details');
    $('#tab-comments').classList.toggle('hidden', activeTab !== 'comments');

    $('#modal-overlay').classList.remove('hidden');

    if (activeTab === 'comments') {
        commentPage = 0;
        loadComments();
    }
}

function closeModal() {
    $('#modal-overlay').classList.add('hidden');
}

/* ========== Комментарии: админ смотрит и удаляет ========== */
async function loadComments() {
    const container = $('#comments-container');
    if (!container || !currentEventId) return;
    const from = commentPage * COMMENT_SIZE;
    try {
        const comments = await api(`/events/${currentEventId}/comments?from=${from}&size=${COMMENT_SIZE}`);
        if (!comments.length && commentPage > 0) {
            commentPage--;
            return loadComments();
        }
        renderComments(comments);
    } catch (e) {
        container.innerHTML = `<p>Ошибка загрузки: ${e.message}</p>`;
    }
}

function renderComments(comments) {
    const container = $('#comments-container');
    if (!comments.length) {
        container.innerHTML = '<p>Комментариев пока нет</p>';
        return;
    }
    commentsCache = new Map(comments.map(c => [c.id, c]));
    const commentsHtml = comments.map(c => `
        <div class="comment" id="comment-${c.id}">
            <div class="comment-header">
                <span>${GLYPH.user} ${escapeHtml(c.authorName || 'Unknown')}</span>
                <span class="comment-header-right">
                    <span>${GLYPH.date} ${formatDate(c.created)}</span>
                    <button type="button" class="comment-menu-btn" onclick="toggleCommentMenu(${c.id}, event)">${GLYPH.menu}</button>
                </span>
            </div>
            <div class="comment-text">${escapeHtml(c.text)}</div>
            ${c.edited ? `<div class="comment-edited">${GLYPH.edit} изменено ${formatDate(c.edited)}</div>` : ''}
            <div class="comment-menu hidden" id="comment-menu-${c.id}">
                <button type="button" class="comment-menu-item danger" onclick="deleteComment(${c.id})">${GLYPH.del} Удалить</button>
            </div>
        </div>`).join('');

    const pagerHtml = `
        <div class="pager">
            <button id="comments-prev" type="button" ${commentPage === 0 ? 'disabled' : ''}>‹ Назад</button>
            <span>стр. ${commentPage + 1}</span>
            <button id="comments-next" type="button" ${comments.length < COMMENT_SIZE ? 'disabled' : ''}>Вперёд ›</button>
        </div>
    `;

    container.innerHTML = commentsHtml + pagerHtml;

    $('#comments-prev').addEventListener('click', () => {
        if (commentPage > 0) { commentPage--; loadComments(); }
    });
    $('#comments-next').addEventListener('click', () => {
        if (comments.length === COMMENT_SIZE) { commentPage++; loadComments(); }
    });
}

function toggleCommentMenu(id, ev) {
    if (ev) ev.stopPropagation();
    const menu = document.getElementById(`comment-menu-${id}`);
    const wasOpen = !menu.classList.contains('hidden');
    closeAllCommentMenus();
    if (!wasOpen) menu.classList.remove('hidden');
}

function closeAllCommentMenus() {
    document.querySelectorAll('.comment-menu').forEach(m => m.classList.add('hidden'));
}

async function deleteComment(commentId) {
    try {
        await api(`/admin/comments/${commentId}`, { method: 'DELETE' });
        toast(`${GLYPH.del} Комментарий удалён`);
        loadComments();
    } catch (e) {
        toast(e.message, true);
    }
}

/* ========== Утилиты ========== */
function formatDate(isoString) {
    if (!isoString) return '—';
    return isoString.replace('T', ' ').substring(0, 16);
}
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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

document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const target = tab.dataset.tab;
        $('#tab-details').classList.toggle('hidden', target !== 'details');
        $('#tab-comments').classList.toggle('hidden', target !== 'comments');
        if (target === 'comments') loadComments();
    });
});

$('#modal-overlay').addEventListener('click', (ev) => {
    if (ev.target.id === 'modal-overlay') closeModal();
});
document.addEventListener('keydown', (ev) => {
    if (ev.key === 'Escape' && !$('#modal-overlay').classList.contains('hidden')) closeModal();
});
document.addEventListener('click', (ev) => {
    if (!ev.target.closest('.comment-menu') && !ev.target.closest('.comment-menu-btn')) {
        closeAllCommentMenus();
    }
});

$('#admin-logout').addEventListener('click', () => {
    deleteCookie('ewm_user_id');
    deleteCookie('ewm_user_name');
    deleteCookie('ewm_role');
    location.href = '/index.html';
});

/* ========== Точка входа ========== */
loadAdminEvents();