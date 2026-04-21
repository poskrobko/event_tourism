const KEY_DB = 'et_db_v1';
const KEY_TOKEN = 'et_token';

const app = document.getElementById('app');
const nav = document.getElementById('main-nav');

const now = () => new Date().toISOString();
const uid = () => Math.random().toString(36).slice(2, 10);

class BasePriceStrategy { calc(base, qty) { return base * qty; } }
class GroupDiscountStrategy extends BasePriceStrategy { calc(base, qty) { return qty >= 5 ? base * qty * 0.9 : base * qty; } }
class EarlyBirdStrategy extends BasePriceStrategy { calc(base, qty, eventDate) { return (new Date(eventDate) > new Date() ? base * qty * 0.95 : base * qty); } }

class TicketFactory {
  static create({ eventId, userId, qty, total, email }) {
    return {
      id: `T-${Date.now()}-${uid()}`,
      eventId,
      userId,
      qty,
      total,
      email,
      status: 'PAID',
      createdAt: now(),
      qr: `QR-${uid()}-${uid()}`
    };
  }
}

function seed() {
  return {
    users: [
      { id: 'u-admin', name: 'Admin', email: 'admin@demo.com', password: 'admin', role: 'ADMIN' }
    ],
    events: [
      {
        id: 'e1',
        title: 'Gastro Weekend Tbilisi',
        description: 'Кулинарный тур с мастер-классами и дегустацией.',
        price: 120,
        location: 'Tbilisi, Georgia',
        category: 'Food',
        date: '2026-05-10',
        ticketsAvailable: 80,
        schedule: [
          { id: uid(), day: '2026-05-10', time: '10:00', title: 'Welcome brunch' },
          { id: uid(), day: '2026-05-10', time: '13:00', title: 'Wine tasting' }
        ]
      },
      {
        id: 'e2',
        title: 'Historic Walk Rome',
        description: 'Пешая экскурсия по историческим местам Рима.',
        price: 75,
        location: 'Rome, Italy',
        category: 'Culture',
        date: '2026-06-02',
        ticketsAvailable: 120,
        schedule: [{ id: uid(), day: '2026-06-02', time: '09:30', title: 'Colosseum tour' }]
      }
    ],
    orders: []
  };
}

function db() {
  const raw = localStorage.getItem(KEY_DB);
  if (!raw) {
    const initial = seed();
    localStorage.setItem(KEY_DB, JSON.stringify(initial));
    return initial;
  }
  return JSON.parse(raw);
}
function save(data) { localStorage.setItem(KEY_DB, JSON.stringify(data)); }

function makeToken(user) {
  const payload = btoa(JSON.stringify({ id: user.id, role: user.role, email: user.email, ts: Date.now() }));
  localStorage.setItem(KEY_TOKEN, payload);
}
function currentUser() {
  const t = localStorage.getItem(KEY_TOKEN);
  if (!t) return null;
  try {
    const data = JSON.parse(atob(t));
    return db().users.find(u => u.id === data.id) || null;
  } catch {
    return null;
  }
}
function logout() { localStorage.removeItem(KEY_TOKEN); route('#/auth'); }

function route(path = location.hash || '#/auth') {
  if (!location.hash) location.hash = path;
  renderNav();
  const user = currentUser();
  const [_, section, id] = location.hash.split('/');

  if (!user && section !== 'auth') return (location.hash = '#/auth');
  if (user && section === 'auth') location.hash = '#/events';

  switch (section) {
    case 'auth': return renderAuth();
    case 'events': return renderEvents();
    case 'event': return renderEventDetails(id);
    case 'tickets': return renderMyTickets();
    case 'admin': return user?.role === 'ADMIN' ? renderAdmin() : renderForbidden();
    default: return renderEvents();
  }
}

function renderNav() {
  const user = currentUser();
  if (!user) {
    nav.innerHTML = `<button class="btn" onclick="location.hash='#/auth'">Login/Register</button>`;
    return;
  }
  nav.innerHTML = `
    <button class="btn" onclick="location.hash='#/events'">Мероприятия</button>
    <button class="btn" onclick="location.hash='#/tickets'">Мои билеты</button>
    ${user.role === 'ADMIN' ? `<button class="btn warn" onclick="location.hash='#/admin'">Админ-панель</button>` : ''}
    <span class="badge">${user.name} (${user.role})</span>
    <button class="btn" onclick="logout()">Выйти</button>
  `;
}

function notify(msg) {
  app.insertAdjacentHTML('afterbegin', `<div class="notice">${msg}</div>`);
  setTimeout(() => app.querySelector('.notice')?.remove(), 2200);
}

function renderAuth() {
  app.innerHTML = document.getElementById('auth-template').innerHTML;

  app.querySelector('#login-form').onsubmit = e => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const data = db();
    const user = data.users.find(u => u.email === fd.get('email') && u.password === fd.get('password'));
    if (!user) return alert('Неверные данные');
    makeToken(user);
    route('#/events');
  };

  app.querySelector('#register-form').onsubmit = e => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const data = db();
    if (data.users.some(u => u.email === fd.get('email'))) return alert('Email уже зарегистрирован');
    const user = { id: `u-${uid()}`, name: fd.get('name'), email: fd.get('email'), password: fd.get('password'), role: 'USER' };
    data.users.push(user);
    save(data);
    makeToken(user);
    route('#/events');
  };

  app.querySelector('#recover-form').onsubmit = e => {
    e.preventDefault();
    const fd = new FormData(e.target);
    alert(`Ссылка восстановления отправлена на ${fd.get('email')} (мок).`);
  };
}

function renderEvents() {
  const data = db();
  const categories = [...new Set(data.events.map(e => e.category))];
  app.innerHTML = `
    <section class="card">
      <div class="spread"><h2>Список мероприятий</h2><span class="small">Сортировка и фильтры</span></div>
      <div class="grid-3" id="filters">
        <label>Локация <input id="f-location" placeholder="например, Rome" /></label>
        <label>Категория
          <select id="f-category"><option value="">Все</option>${categories.map(c => `<option>${c}</option>`).join('')}</select>
        </label>
        <label>Макс. цена <input id="f-price" type="number" min="0" /></label>
      </div>
      <div class="row">
        <button class="btn" id="sort-date">Сортировать по дате</button>
        <button class="btn" id="sort-price">Сортировать по цене</button>
      </div>
    </section>
    <section id="events-list" class="grid-3" style="margin-top:12px"></section>
  `;

  let list = [...data.events];
  let sortBy = null;

  const renderList = () => {
    const loc = app.querySelector('#f-location').value.toLowerCase();
    const cat = app.querySelector('#f-category').value;
    const price = Number(app.querySelector('#f-price').value || Infinity);

    let filtered = list.filter(e =>
      e.location.toLowerCase().includes(loc) &&
      (!cat || e.category === cat) &&
      e.price <= price
    );

    if (sortBy === 'date') filtered.sort((a, b) => new Date(a.date) - new Date(b.date));
    if (sortBy === 'price') filtered.sort((a, b) => a.price - b.price);

    app.querySelector('#events-list').innerHTML = filtered.map(e => `
      <article class="card event-card">
        <span class="badge">${e.category}</span>
        <h3>${e.title}</h3>
        <p>${e.description}</p>
        <p><b>${e.location}</b> · ${e.date}</p>
        <p>Цена от <b>$${e.price}</b> · Осталось: ${e.ticketsAvailable}</p>
        <button class="btn primary" onclick="location.hash='#/event/${e.id}'">Подробнее</button>
      </article>
    `).join('') || '<p class="card">Ничего не найдено</p>';
  };

  ['#f-location', '#f-category', '#f-price'].forEach(sel => app.querySelector(sel).oninput = renderList);
  app.querySelector('#sort-date').onclick = () => { sortBy = 'date'; renderList(); };
  app.querySelector('#sort-price').onclick = () => { sortBy = 'price'; renderList(); };
  renderList();
}

function renderEventDetails(id) {
  const data = db();
  const event = data.events.find(e => e.id === id);
  const user = currentUser();
  if (!event) return renderNotFound('Мероприятие не найдено');

  const grouped = event.schedule.reduce((acc, s) => {
    (acc[s.day] = acc[s.day] || []).push(s);
    return acc;
  }, {});

  app.innerHTML = `
    <section class="card">
      <div class="spread"><h2>${event.title}</h2><span class="badge">${event.category}</span></div>
      <p>${event.description}</p>
      <p><b>Локация:</b> ${event.location}</p>
      <p><b>Дата:</b> ${event.date} · <b>Цена:</b> $${event.price}</p>
      <iframe class="map" loading="lazy" src="https://maps.google.com/maps?q=${encodeURIComponent(event.location)}&output=embed"></iframe>
      <h3>Программа мероприятия</h3>
      ${Object.entries(grouped).map(([day, items]) => `
        <div class="card">
          <b>${day}</b>
          <ul>${items.sort((a,b) => a.time.localeCompare(b.time)).map(i => `<li>${i.time} — ${i.title}</li>`).join('')}</ul>
        </div>
      `).join('')}
    </section>

    <section class="card" style="margin-top:12px">
      <h3>Покупка билета</h3>
      <form id="buy-form" class="grid-3">
        <label>Количество <input type="number" min="1" max="10" name="qty" value="1" required /></label>
        <label>Стратегия расчёта
          <select name="strategy">
            <option value="base">BasePriceStrategy</option>
            <option value="group">GroupDiscountStrategy</option>
            <option value="early">EarlyBirdStrategy</option>
          </select>
        </label>
        <label>Email для PDF билета <input type="email" name="email" value="${user.email}" required /></label>
        <button class="btn accent" type="submit">Подтвердить покупку</button>
      </form>
      <p class="small">После покупки заказ попадёт в «Мои билеты», отобразится QR/ID, и будет имитация отправки PDF на почту.</p>
      <button class="btn" id="calendar-btn">Добавить в Google Calendar (мок)</button>
    </section>
  `;

  app.querySelector('#calendar-btn').onclick = () => alert('Событие добавлено в Google Calendar (мок).');

  app.querySelector('#buy-form').onsubmit = e => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const qty = Number(fd.get('qty'));
    if (qty > event.ticketsAvailable) return alert('Недостаточно билетов в наличии');

    const strategyMap = {
      base: new BasePriceStrategy(),
      group: new GroupDiscountStrategy(),
      early: new EarlyBirdStrategy()
    };
    const strategy = strategyMap[fd.get('strategy')] || strategyMap.base;
    const total = Math.round(strategy.calc(event.price, qty, event.date) * 100) / 100;

    const ticket = TicketFactory.create({ eventId: event.id, userId: user.id, qty, total, email: fd.get('email') });
    event.ticketsAvailable -= qty;

    data.orders.push(ticket);
    save(data);
    generatePdfTicket(ticket, event, user);

    notify(`Покупка подтверждена! Ticket ID: ${ticket.id}, QR: ${ticket.qr}`);
    setTimeout(() => location.hash = '#/tickets', 900);
  };
}

function generatePdfTicket(ticket, event, user) {
  const pdfBody = `%PDF-1.1\nTicket:${ticket.id}\nUser:${user.name}\nEvent:${event.title}\nTotal:${ticket.total}\n%%EOF`;
  const blob = new Blob([pdfBody], { type: 'application/pdf' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `ticket-${ticket.id}.pdf`;
  a.click();
  URL.revokeObjectURL(url);
  alert(`PDF билет отправлен на ${ticket.email} (мок) и скачан локально.`);
}

function renderMyTickets() {
  const user = currentUser();
  const data = db();
  const orders = data.orders.filter(o => o.userId === user.id).sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt));

  app.innerHTML = `
    <section class="card">
      <h2>Мои билеты и история заказов</h2>
      ${orders.length ? '' : '<p>Пока нет заказов.</p>'}
      <div class="grid-2">
        ${orders.map(o => {
          const event = data.events.find(e => e.id === o.eventId);
          const cls = o.status === 'PAID' ? 'ok' : (o.status === 'PENDING' ? 'pending' : 'cancelled');
          return `
          <article class="card">
            <div class="spread"><b>${event?.title || 'Unknown event'}</b><span class="badge ${cls}">${o.status}</span></div>
            <p>Ticket ID: <b>${o.id}</b></p>
            <p>Количество: ${o.qty} · Сумма: $${o.total}</p>
            <p>QR: ${o.qr}</p>
            <p class="small">${new Date(o.createdAt).toLocaleString()}</p>
          </article>`;
        }).join('')}
      </div>
    </section>
  `;
}

function renderAdmin() {
  const data = db();
  app.innerHTML = `
    <section class="card">
      <h2>Админ-панель</h2>
      <p class="small">CRUD мероприятий, программа, билеты, заказы пользователей.</p>
    </section>

    <section class="card" style="margin-top:12px">
      <h3>Создать/обновить мероприятие</h3>
      <form id="event-form" class="grid-3">
        <input name="id" placeholder="ID (для редактирования)" />
        <input name="title" placeholder="Название" required />
        <input name="date" type="date" required />
        <input name="location" placeholder="Локация" required />
        <input name="category" placeholder="Категория" required />
        <input name="price" type="number" min="0" placeholder="Цена" required />
        <input name="ticketsAvailable" type="number" min="1" placeholder="Кол-во билетов" required />
        <textarea name="description" placeholder="Описание" required></textarea>
        <button class="btn primary" type="submit">Сохранить</button>
      </form>
    </section>

    <section class="card" style="margin-top:12px">
      <h3>Все мероприятия</h3>
      <div id="admin-events"></div>
    </section>

    <section class="card" style="margin-top:12px">
      <h3>Все заказы пользователей</h3>
      <table class="table">
        <thead><tr><th>ID</th><th>Пользователь</th><th>Событие</th><th>Сумма</th><th>Статус</th><th>Действия</th></tr></thead>
        <tbody id="orders-body"></tbody>
      </table>
    </section>
  `;

  const renderAdminEvents = () => {
    app.querySelector('#admin-events').innerHTML = data.events.map(e => `
      <article class="card" style="margin-bottom:10px">
        <div class="spread">
          <b>${e.title}</b>
          <div class="row">
            <button class="btn" onclick="adminEdit('${e.id}')">Редактировать</button>
            <button class="btn" onclick="adminDelete('${e.id}')">Удалить</button>
          </div>
        </div>
        <p>${e.date} · ${e.location} · $${e.price} · билетов: ${e.ticketsAvailable}</p>
        <h4>Программа</h4>
        <ul>${e.schedule.map(s => `<li>${s.day} ${s.time} — ${s.title} <button class='btn link' onclick="adminDeleteSchedule('${e.id}','${s.id}')">удалить</button></li>`).join('')}</ul>
        <form class="row" onsubmit="adminAddSchedule(event, '${e.id}')">
          <input type="date" name="day" required />
          <input type="time" name="time" required />
          <input name="title" placeholder="Название пункта" required />
          <button class="btn">Добавить пункт</button>
        </form>
      </article>
    `).join('');
  };

  const renderOrders = () => {
    app.querySelector('#orders-body').innerHTML = data.orders.map(o => {
      const user = data.users.find(u => u.id === o.userId);
      const event = data.events.find(e => e.id === o.eventId);
      return `
        <tr>
          <td>${o.id}</td><td>${user?.email || '-'}</td><td>${event?.title || '-'}</td><td>$${o.total}</td>
          <td>${o.status}</td>
          <td>
            <button class="btn link" onclick="adminSetStatus('${o.id}','PAID')">Оплачено</button>
            <button class="btn link" onclick="adminSetStatus('${o.id}','CANCELLED')">Отмена</button>
            <button class="btn link" onclick="adminSetStatus('${o.id}','REFUND')">Возврат</button>
          </td>
        </tr>
      `;
    }).join('');
  };

  window.adminEdit = (id) => {
    const e = data.events.find(x => x.id === id);
    const f = app.querySelector('#event-form');
    Object.entries(e).forEach(([k, v]) => f.elements[k] && (f.elements[k].value = v));
  };

  window.adminDelete = (id) => {
    if (!confirm('Удалить мероприятие?')) return;
    data.events = data.events.filter(e => e.id !== id);
    save(data); renderAdmin();
  };

  window.adminAddSchedule = (ev, eventId) => {
    ev.preventDefault();
    const fd = new FormData(ev.target);
    const e = data.events.find(x => x.id === eventId);
    e.schedule.push({ id: uid(), day: fd.get('day'), time: fd.get('time'), title: fd.get('title') });
    save(data); renderAdmin();
  };

  window.adminDeleteSchedule = (eventId, sid) => {
    const e = data.events.find(x => x.id === eventId);
    e.schedule = e.schedule.filter(s => s.id !== sid);
    save(data); renderAdmin();
  };

  window.adminSetStatus = (oid, status) => {
    const o = data.orders.find(x => x.id === oid);
    o.status = status;
    save(data); renderAdmin();
  };

  app.querySelector('#event-form').onsubmit = ev => {
    ev.preventDefault();
    const fd = new FormData(ev.target);
    const item = {
      id: fd.get('id') || `e-${uid()}`,
      title: fd.get('title'),
      description: fd.get('description'),
      price: Number(fd.get('price')),
      location: fd.get('location'),
      category: fd.get('category'),
      date: fd.get('date'),
      ticketsAvailable: Number(fd.get('ticketsAvailable')),
      schedule: []
    };
    const existing = data.events.find(e => e.id === item.id);
    if (existing) Object.assign(existing, { ...item, schedule: existing.schedule });
    else data.events.push(item);
    save(data);
    ev.target.reset();
    renderAdmin();
  };

  renderAdminEvents();
  renderOrders();
}

function renderNotFound(msg) { app.innerHTML = `<section class='card'><h3>${msg}</h3></section>`; }
function renderForbidden() { app.innerHTML = `<section class='card'><h3>403: Нет доступа</h3></section>`; }

window.addEventListener('hashchange', route);
route();
