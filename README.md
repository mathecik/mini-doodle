# Mini Doodle

A small meeting scheduling service written in Java and Spring Boot.
Users can open time slots in their calendar, turn them into meetings,
and check when they are free or busy.

## How to run

You need Docker (with Docker Compose) installed.

Run the command below to start the application together with its database:

```bash
docker compose up --build
```

To check the health, run this command in another terminal tab:

```bash
curl localhost:8080/actuator/health
```

It should return `{"status":"UP"}`.

To stop the application, press Ctrl+C, or run `docker compose down`.
