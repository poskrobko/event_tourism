# Курсовой проект: Event Tourism System

## 1) Архитектура

Проект разделён на **2 независимых приложения**:
1. `backend/` — Spring Boot REST API (JSON).
2. `frontend/` — HTML/CSS/Vanilla JS + Bootstrap.

### Backend (упрощённая Clean Architecture)
- `controller` — REST endpoints.
- `service` — бизнес-логика.
- `repository` — доступ к БД через Spring Data JPA.
- `facade` — `EventFacade` собирает детали события из нескольких сервисов.
- `factory` — `TicketFactory` создаёт объекты билетов.
- `service/pricing` — Strategy для расчёта суммы заказа.

### Основные use cases (16)
1. Регистрация пользователя.
2. Логин пользователя.
3. Получение JWT.
4. Просмотр списка событий.
5. Фильтрация по дате.
6. Фильтрация по городу.
7. Фильтрация по максимальной цене.
8. Просмотр деталей события.
9. Просмотр программы события.
10. Просмотр доступных билетов.
11. Покупка билета.
12. Просмотр своих билетов.
13. Просмотр истории заказов.
14. Админ: создание локации.
15. Админ: CRUD событий (создание/удаление, база для update/read).
16. Админ: управление программой, билетами и просмотр всех заказов.

---

## 2) База данных

Flyway миграция: `backend/src/main/resources/db/migration/V1__init.sql`.

Таблицы:
- `users`
- `roles`
- `user_roles`
- `locations`
- `events`
- `event_programs`
- `tickets`
- `orders`
- `payments`

Итого: 9 таблиц.

---

## 3) Backend

### Технологии
- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Flyway

### Паттерны
- **Strategy**: `RegularPricingStrategy`, `GroupPricingStrategy`
- **Factory**: `TicketFactory`
- **Facade**: `EventFacade`

### Дополнительные требования
- DTO на `record` (пакет `dto`).
- Custom exceptions (`NotFoundException`, `BadRequestException`).
- `Optional` в репозиториях и сервисах.
- `Stream API` в преобразованиях коллекций.
- `Generics`: интерфейс `GenericMapper<T, R>`.
- Пагинация и фильтрация: `GET /api/public/events`.

### Безопасность
- JWT фильтр `JwtFilter`.
- BCrypt пароль.
- Роли: `USER`, `ADMIN`.

### Интеграции (упрощённо)
- Google Maps URL: `MapsService`.
- Google Calendar URL: `CalendarService`.

---

## 4) Frontend

Папка: `frontend/`.

### Страницы
- `pages/login.html`
- `pages/events.html`
- `pages/event-details.html`
- `pages/buy-ticket.html`
- `pages/profile.html`
- `pages/admin.html`

### UI
- Bootstrap 5 (без JS-фреймворков).
- Кастомные цвета:
  - `#FF9760` (основной)
  - `#FFD150` (акцент)
  - `#458B73` (вторичный)

---

## 5) Примеры API

### Auth
- `POST /api/auth/register`
```json
{ "email": "user@mail.com", "password": "123456", "fullName": "Ivan Ivanov" }
```
- `POST /api/auth/login`
```json
{ "email": "user@mail.com", "password": "123456" }
```

### Events
- `GET /api/public/events?date=2026-05-01&city=Sochi&maxPrice=10000&page=0&size=10`
- `GET /api/public/events/{id}`

### User
- `POST /api/user/tickets/buy`
```json
{ "ticketId": 1, "amount": 2 }
```
- `GET /api/user/orders`
- `GET /api/user/tickets`

### Admin
- `POST /api/admin/locations`
- `POST /api/admin/events`
- `DELETE /api/admin/events/{id}`
- `POST /api/admin/events/{eventId}/program`
- `POST /api/admin/events/{eventId}/tickets`
- `GET /api/admin/orders`

---

## 6) Чек-лист требований

- [x] Backend на Spring Boot 3+, Java 17, JPA, Security, JWT, PostgreSQL, Flyway.
- [x] Frontend только HTML/CSS/Vanilla JS + Bootstrap.
- [x] Frontend и Backend разделены.
- [x] 8+ таблиц (реализовано 9).
- [x] 15+ use cases (реализовано 16).
- [x] Роли USER/ADMIN и BCrypt.
- [x] Пагинация и фильтрация.
- [x] DTO через `record`.
- [x] Custom exceptions.
- [x] Optional/Stream API/Generics.
- [x] Strategy/Factory/Facade.
- [x] Интеграции Maps/Calendar (упрощённый mock через ссылки).

## Запуск

1. Поднять PostgreSQL и создать БД `event_tourism`.
2. Запустить backend:
```bash
cd backend
mvn spring-boot:run
```
3. Открыть `frontend/pages/login.html` через локальный static server.
