# sprint8
# My Market App (с платежами и кешем)

Веб-приложение для просмотра товаров, добавления в корзину и оформления заказов.

## Требования

- Docker & Docker Compose
- Java 21+
- Maven 3.8+

## Сервисы и порты

| Сервис | Порт | Описание |
|--------|------|----------|
| Web-приложение (my-market-app) | 8080 | Основное приложение |
| Сервис платежей (payment-service) | 8081 | Обработка платежей |
| Keycloak | 8082 | Сервер авторизации (OAuth2.0) |
| Redis | 6379 | Кеш товаров |

## Технологии

- **Backend**: Spring Boot 3.5.6, Spring WebFlux
- **Security**: Spring Security + OAuth2.0 + Keycloak
- **Database**: H2 (R2DBC)
- **Cache**: Redis (Reactive)
- **Frontend**: Thymeleaf
- **Build**: Maven

## Аутентификация и авторизация (OAuth2.0)

Приложение использует **Keycloak** в качестве Identity Provider для OAuth2.0 аутентификации:

- **Keycloak URL**: http://localhost:8082
- **Realm**: master
- **Client ID**: my-market-app
- **Grant Type**: authorization_code
- **Scopes**: openid, profile, email

OAuth2.0 конфигурация обеспечивает:
- Защиту всех endpoints (кроме публичных)
- Интеграцию с платежным сервисом через авторизованные клиенты
- Reactive OAuth2 клиент для асинхронной обработки

## Кеширование (Redis)

**Redis** используется для кеширования товаров:
- **Порт**: 6379
- **Тип**: Reactive Redis
- **TTL**: 5 минут
- **Ключи**: `item:{id}`

Кеш автоматически инвалидируется при обновлении корзины.

## Сборка

```bash
mvn clean package
Запуск (Docker Compose)
bash
docker-compose up
При первом запуске Keycloak автоматически создаст необходимые настройки.

Запуск отдельных сервисов
bash
# Только основное приложение (с зависимостями в Docker)
docker-compose up my-market-app

# Только Keycloak
docker-compose up keycloak

# Только Redis
docker-compose up redis

# Только сервис платежей
docker-compose up payment-service
Тесты
bash
# Все тесты
mvn test

# Только unit-тесты
mvn test -Dgroups=unit

# Только integration-тесты
mvn test -Dgroups=integration

# Тесты с профилем test (без Keycloak)
mvn test -Dspring.profiles.active=test
Доступ к сервисам
Витрина: http://localhost:8080

Сервис платежей:

Баланс: http://localhost:8081/api/balance (GET)

Платеж: http://localhost:8081/api/payment (POST)

Keycloak Admin Console: http://localhost:8082 (admin/admin)

Redis CLI: docker exec -it redis redis-cli

Тестовые учетные записи
Username	Password	Роль
user	user	USER
admin	admin	ADMIN
API Endpoints
Метод	Endpoint	Описание	Auth
GET	/items	Список товаров (с пагинацией, поиском, сортировкой)	PUBLIC
GET	/items/{id}	Детали товара	PUBLIC
POST	/items	Обновление корзины (action=PLUS/MINUS/DELETE)	USER
GET	/cart/items	Просмотр корзины	USER
POST	/buy	Оформление заказа	USER
GET	/orders	Список заказов	USER
GET	/orders/{id}	Детали заказа	USER
Функциональность
Главная страница (/items)
Просмотр товаров в виде карточек (3 в строке)

Поиск по названию и описанию

Сортировка: без сортировки, по алфавиту, по цене

Пагинация: 2, 5, 10, 20, 50, 100 товаров на странице

Управление количеством товара в корзине прямо на главной странице

Кеширование товаров в Redis

Страница товара (/items/{id})
Детальная информация о товаре

Изображение, название, описание, цена

Управление количеством в корзине

Корзина (/cart/items)
Список товаров, добавленных в корзину

Изменение количества товаров

Удаление товаров из корзины

Отображение общей суммы заказа

Кнопка оформления заказа

Интеграция с платежным сервисом

Заказы (/orders)
Список всех оформленных заказов

Отображение товаров в каждом заказе

Сумма каждого заказа

Страница заказа (/orders/{id})
Детальная информация о заказе

Список товаров с ценами и количеством

Общая сумма заказа

Уведомление об успешной покупке

Переменные окружения
Переменная	Описание	Значение по умолчанию
KEYCLOAK_URL	URL Keycloak сервера	http://localhost:8082
REDIS_HOST	Redis хост	localhost
REDIS_PORT	Redis порт	6379
PAYMENT_SERVICE_URL	URL платежного сервиса	http://localhost:8081
Решение проблем
Keycloak не запускается
bash
# Проверьте, что порт 8082 свободен
netstat -an | grep 8082

# Очистите тома Docker
docker-compose down -v
docker-compose up
Redis подключение ошибка
bash
# Проверьте статус Redis
docker-compose ps redis

# Просмотр логов Redis
docker-compose logs redis
Ошибки аутентификации OAuth2
Убедитесь, что Keycloak запущен на порту 8082

Проверьте в консоли Keycloak наличие клиента my-market-app

При пересборке может потребоваться очистить cookies браузера

Разработка
Профили
default - полная конфигурация (Keycloak, Redis, платежный сервис)

test - мок-конфигурация для тестов (без внешних зависимостей)

dev - локальная разработка (H2 in-memory, без кеша)

Локальный запуск без Docker
bash
# Запуск Redis
redis-server

# Запуск Keycloak (требуется отдельная установка)
# Или используйте docker-compose только для зависимостей
docker-compose up keycloak redis payment-service

# Запуск приложения
mvn spring-boot:run