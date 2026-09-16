# Smart Backend

Spring Boot + PostgreSQL + Ollama. Реализует конструктор модулей, историю диалогов, JSON/Markdown parsing, динамический SQL и динамические REST endpoints.

## Запуск

1. Запустить инфраструктуру:

```bash
docker compose up -d
```

2. Проверить:

```bash
docker ps
```

Должны работать `smart-postgres` и `smart-ollama`.

3. Скачать модель:

```bash
docker exec -it smart-ollama ollama pull gemma3:4b
```

4. Запустить приложение:

```bash
mvn spring-boot:run
```

## Примеры API

Создать обсуждение:

```http
POST /api/discussions
Content-Type: application/json

{
  "name": "Users API",
  "systemPrompt": "Return valid JSON in Markdown.",
  "model": "gemma3:4b"
}
```

Сообщение нейросети:

```http
POST /api/discussions/1/messages
Content-Type: application/json

{
  "message": "Create a SELECT for a users table with id and email."
}
```

Распарсить последний ответ:

```http
POST /api/discussions/1/parse-last
```

Создать динамический API:

```http
POST /api/features/discussion/1
Content-Type: application/json

{
  "name": "User list",
  "type": "API",
  "description": "Returns users",
  "method": "GET",
  "path": "/dynamic/users",
  "code": "SELECT id, email FROM users",
  "settings": {}
}
```

После сохранения endpoint доступен:

```http
GET /dynamic/users
```

## Проверка

```bash
mvn clean test
mvn clean package
```
