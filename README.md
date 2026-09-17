# Проект «Умный бэкенд»

Бэкенд-конструктор на Spring Boot, который позволяет вести диалог с локальной LLM через Ollama, сохранять контекст диалога в PostgreSQL, получать от нейросети описание Feature в JSON, сохранять Feature в БД и динамически создавать HTTP API для выполнения SQL-запросов.

В проекте реализован рабочий сценарий:

```text
Пользователь
    ↓
POST /api/discussions/{id}/generate-feature
    ↓
Ollama / gemma3:4b
    ↓
JSON-ответ модели
    ↓
MarkdownJsonParser + Jackson ObjectMapper
    ↓
Feature
    ↓
PostgreSQL
    ↓
RequestMappingHandlerMapping
    ↓
GET /dynamic/...
    ↓
NamedParameterJdbcTemplate
    ↓
SQL
    ↓
JSON-ответ
```

## Стек

* Java 21
* Spring Boot 3.3.2
* Spring Web
* Spring Data JPA / Hibernate
* PostgreSQL 17
* Ollama
* модель `gemma3:4b`
* `RestTemplate`
* `NamedParameterJdbcTemplate`
* Jackson `ObjectMapper`
* `flexmark-java`
* Maven
* Docker Compose
* JUnit 5 / Mockito

## Структура проекта

```text
src/
├── main/
│   ├── java/ru/yandex/practicum/smart/
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── dao/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── exceptions/
│   │   ├── ollama/
│   │   └── SmartApp.java
│   │
│   └── resources/
│       ├── application.properties
│       └── init.sql
│
└── test/
    └── java/
```

Контроллеры содержат только HTTP-слой, основная логика находится в сервисах, работа с БД вынесена в репозитории и `NamedParameterJdbcTemplate`.

## Инфраструктура

Для проекта используются два контейнера:

1. PostgreSQL
2. Ollama

Запуск:

```bash
docker compose up -d
```

Проверка:

```bash
docker ps
```

В результате должны быть запущены контейнеры PostgreSQL и Ollama.

Проверить Ollama:

```bash
docker exec -it smart-ollama ollama help
```

Проверить установленную модель:

```bash
docker exec -it smart-ollama ollama list
```

В проекте используется:

```text
gemma3:4b
```

Модели Ollama сохраняются во внешнем volume, подключённом к:

```text
/root/.ollama
```

## Настройка приложения

Основные параметры находятся в:

```text
src/main/resources/application.properties
```

Используются переменные окружения с fallback-значениями:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/smart}
spring.datasource.username=${DB_USERNAME:smart}
spring.datasource.password=${DB_PASSWORD:smart}

ollama.url=${OLLAMA_URL:http://localhost:11434}
ollama.model=${OLLAMA_MODEL:gemma3:4b}
```

Структура БД создаётся из:

```text
src/main/resources/init.sql
```

Hibernate не создаёт схему автоматически:

```properties
spring.jpa.hibernate.ddl-auto=none
```

## Сборка

Проверка тестов:

```bash
mvn clean test
```

Сборка:

```bash
mvn clean package -DskipTests
```

Запуск готового приложения:

```bash
java -jar target/smart-0.0.1-SNAPSHOT.jar
```

Приложение доступно на:

```text
http://localhost:8080
```

---

# API конструктора

## Создание обсуждения

### POST `/api/discussions`

Пример:

```json
{
  "name": "Test discussion",
  "systemPrompt": "You are a helpful assistant",
  "model": "gemma3:4b"
}
```

Пример запроса:

```powershell
$body = @{
    name = "Test discussion"
    systemPrompt = "You are a helpful assistant"
    model = "gemma3:4b"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/discussions" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

Ожидается `201 Created`.

---

## Получение списка обсуждений

### GET `/api/discussions`

```powershell
curl.exe -i "http://localhost:8080/api/discussions"
```

---

## Получение обсуждения

### GET `/api/discussions/{id}`

Например:

```powershell
curl.exe -i "http://localhost:8080/api/discussions/1"
```

В ответе должны присутствовать данные обсуждения и сохранённые сообщения.

---

## Обычный чат с Ollama

### POST `/api/discussions/{id}/chat`

Тело запроса:

```json
{
  "message": "Hello! Answer in one short sentence."
}
```

Пример:

```powershell
curl.exe -i `
    -X POST `
    "http://localhost:8080/api/discussions/1/chat" `
    -H "Content-Type: application/json" `
    --data-binary '{"message":"Hello! Answer in one short sentence."}'
```

Ответ Ollama сохраняется в БД как сообщение `assistant`.

История сообщений также сохраняется в БД и передаётся модели целиком при следующем запросе.

### Проверка контекста

После первого сообщения можно отправить:

```json
{
  "message": "What was my previous message?"
}
```

Модель должна учитывать предыдущее сообщение из истории диалога.

---

# Генерация Feature через нейросеть

Основная демонстрационная возможность проекта:

### POST `/api/discussions/{id}/generate-feature`

Endpoint получает пользовательское описание задачи, передаёт его локальной модели Ollama и ожидает JSON-описание Feature.

Пример запроса:

```json
{
  "message": "Создай GET API по адресу /dynamic/all-discussions, который возвращает id, name и model всех discussions."
}
```

Для PowerShell рекомендуется передавать JSON через файл, чтобы избежать проблем с экранированием кавычек.

Создать файл:

```powershell
[System.IO.File]::WriteAllText(
    "body.json",
    '{"message":"Создай GET API по адресу /dynamic/all-discussions, который возвращает id, name и model всех discussions."}',
    [System.Text.UTF8Encoding]::new($false)
)
```

Проверить:

```powershell
Get-Content .\body.json -Raw
```

Отправить:

```powershell
curl.exe -i `
    -X POST `
    "http://localhost:8080/api/discussions/1/generate-feature" `
    -H "Content-Type: application/json" `
    --data-binary "@body.json"
```

Ожидается:

```text
HTTP/1.1 201
```

Пример результата:

```json
{
  "id": 4,
  "name": "Get All Discussions",
  "type": "SQL",
  "description": "Retrieves all discussion details (id, name, model).",
  "method": "GET",
  "path": "/dynamic/all-discussions",
  "code": "SELECT id, name, model FROM discussions;",
  "enabled": true,
  "settings": {}
}
```

Конкретное содержимое ответа зависит от генерации модели, но Feature должна содержать как минимум:

* имя;
* тип;
* HTTP method;
* path;
* SQL-код.

---

# Обработка JSON-ответа нейросети

Для генерации Feature используется JSON-режим Ollama.

Ответ модели может содержать Markdown-обёртку:

````text
```json
{
  ...
}
```
````

Поэтому ответ обрабатывается через `flexmark-java`, после чего JSON разбирается с помощью Jackson `ObjectMapper`.

При ошибке парсинга:

1. ошибка сохраняется в историю;
2. предыдущий ответ модели сохраняется в историю;
3. модели отправляется запрос на исправление;
4. выполняется повторная попытка;
5. предусмотрено до трёх попыток.

Таким образом, модель получает полный контекст переписки и может исправить свой предыдущий ответ.

---

# Динамический API

Feature типа `SQL` автоматически регистрирует HTTP endpoint через Spring `RequestMappingHandlerMapping`.

Например, после генерации Feature:

```text
GET /dynamic/all-discussions
```

регистрируется при запуске приложения.

SQL из Feature:

```sql
SELECT id, name, model FROM discussions;
```

выполняется через:

```text
NamedParameterJdbcTemplate
```

## Проверка

```powershell
curl.exe -i "http://localhost:8080/dynamic/all-discussions"
```

Ожидается:

```text
HTTP/1.1 200
Content-Type: application/json
```

и результат:

```json
[
  {
    "id": 1,
    "name": "Test discussion",
    "model": "gemma3:4b"
  },
  {
    "id": 2,
    "name": "Test discussion",
    "model": "gemma3:4b"
  }
]
```

SQL выполняется динамически на основании сохранённой Feature, а не отдельного Java-контроллера для конкретного запроса.

---

# Проверка сохранения сценария после перезапуска

Это важный тест проекта.

После создания Feature и успешного вызова динамического API остановить приложение:

```text
Ctrl+C
```

Запустить снова:

```bash
java -jar target/smart-0.0.1-SNAPSHOT.jar
```

При старте приложение загружает сохранённые включённые Feature из БД и регистрирует их динамически.

В логе ожидаются сообщения вида:

```text
Found saved features: ...
Dynamic endpoint registered: GET /dynamic/discussions
Dynamic endpoint registered: GET /dynamic/all-discussions
```

После запуска снова выполнить:

```powershell
curl.exe -i "http://localhost:8080/dynamic/all-discussions"
```

Ожидается тот же `200 OK` и тот же результат SQL.

Таким образом проверяется, что сценарий не хранится только в памяти приложения и восстанавливается из PostgreSQL.

---

# Сущности конструктора

В проекте используются сущности:

```text
Discussion
    |
    +--- ChatMessage
    |
    +--- Feature
             |
             +--- FeatureSetting
```

Связи между сущностями реализованы средствами JPA.

Основные таблицы:

```text
discussions
chat_messages
features
feature_settings
```

Содержимое диалога сохраняется в `chat_messages`.

Описание динамических возможностей сохраняется в `features`.

Параметры Feature сохраняются в `feature_settings`.

---

# API работы с Feature

Для ручного создания Feature доступен API конструктора:

### POST `/api/features`

Пример:

```json
{
  "name": "Test SQL Feature",
  "type": "SQL",
  "description": "Returns discussions",
  "method": "GET",
  "path": "/dynamic/discussions",
  "code": "SELECT id, name, model FROM discussions ORDER BY id",
  "settings": {}
}
```

### GET `/api/features`

```powershell
curl.exe -i "http://localhost:8080/api/features"
```

### GET `/api/features/{id}`

```powershell
curl.exe -i "http://localhost:8080/api/features/4"
```

При создании SQL Feature динамический endpoint регистрируется автоматически.

---

# Тесты

Запуск всех unit-тестов:

```bash
mvn clean test
```

В проекте проверяются, в частности:

### `MarkdownJsonParserTest`

Проверяет разбор:

* обычного JSON;
* JSON внутри Markdown code fence;
* некорректного JSON;
* отсутствующего JSON.

### `DynamicSqlServiceTest`

Проверяет выполнение:

* `SELECT`;
* `UPDATE`/`DELETE`;
* передачу параметров в `NamedParameterJdbcTemplate`.

### `DiscussionControllerTest`

Проверяет HTTP-слой конструктора обсуждений и основные ответы API.

---


Рекомендуемый порядок проверки:

### 1. Инфраструктура

```bash
docker compose up -d
docker ps
```

Убедиться, что работают PostgreSQL и Ollama.

### 2. Модель Ollama

```bash
docker exec -it smart-ollama ollama list
```

Проверить наличие:

```text
gemma3:4b
```

### 3. Сборка

```bash
mvn clean test
```

Затем:

```bash
mvn clean package -DskipTests
```

### 4. Запуск приложения

```bash
java -jar target/smart-0.0.1-SNAPSHOT.jar
```

### 5. Создание Discussion

```text
POST /api/discussions
```

### 6. Проверка обычного чата

```text
POST /api/discussions/1/chat
```

Проверить, что сообщения сохраняются и следующий запрос учитывает историю.

### 7. Генерация Feature

```text
POST /api/discussions/1/generate-feature
```

с телом:

```json
{
  "message": "Создай GET API по адресу /dynamic/all-discussions, который возвращает id, name и model всех discussions."
}
```

Ожидается `201 Created`.

### 8. Проверка динамического endpoint

```text
GET /dynamic/all-discussions
```

Ожидается `200 OK` и массив данных из PostgreSQL.

### 9. Проверка persistence

Перезапустить приложение и снова выполнить:

```text
GET /dynamic/all-discussions
```

Endpoint должен зарегистрироваться заново автоматически и вернуть данные.

## Ограничения текущей реализации

Основной рабочий демонстрационный сценарий проекта — **динамический SQL API**.

Конструктор рассчитан на дальнейшее расширение другими типами динамических Feature, в частности HTTP-запросами к внешним сервисам и дополнительными сценариями работы с БД.

Для проверки текущей реализации достаточно сценария:

```text
Discussion
    ↓
Ollama
    ↓
Feature
    ↓
PostgreSQL
    ↓
Dynamic API
    ↓
SQL
```

## Результат

Проект демонстрирует гибридный подход к разработке: стандартный Spring Boot API используется как конструктор, а локальная нейросеть генерирует формализованное описание Feature. Сгенерированные сценарии сохраняются в PostgreSQL и могут автоматически восстанавливаться после перезапуска приложения.
