# Infrastructure

Docker Compose stack for FermentLab: Postgres, the REST API, the scheduler and the Angular frontend
behind nginx.

## Run

This stack is a **production** deployment. It creates the schema and the reference data, and nothing
else - no demo batches, no sample recipes.

```bash
cd infrastructure
docker compose up --build
```

To bring up the same stack **seeded with the demo library** instead:

```bash
cd infrastructure
docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up --build
```

| Service    | URL                                         |
|------------|---------------------------------------------|
| Frontend   | http://localhost:4200                       |
| REST API   | http://localhost:8080                       |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Scheduler  | http://localhost:8081                       |
| Postgres   | localhost:5432                              |

The frontend proxies `/api/*` to the API container, so the browser only ever talks to
`localhost:4200` in the containerised setup.

## Configuration

All values live in `.env`. The committed file holds local development defaults only - change
`POSTGRES_PASSWORD` before running this anywhere that is not your laptop.

| Variable                | Meaning                                                |
|-------------------------|--------------------------------------------------------|
| `POSTGRES_*`            | Database name, credentials and host port               |
| `APP_REST_PORT`         | Host port for the REST API                             |
| `SCHEDULER_PORT`        | Host port for the scheduler's actuator endpoints        |
| `FRONTEND_PORT`         | Host port for nginx                                    |
| `CORS_ALLOWED_ORIGINS`  | Comma separated origins allowed to call the API         |
| `SCHEDULER_CRON`        | Cron expression for the alert evaluation job            |

## Schema and seed data

The API owns the Flyway migrations. Compose starts the scheduler only after the API reports healthy,
so migrations always run exactly once.

Migrations live in two locations:

| Location                                              | Contents                                                                     | Applied in     |
|-------------------------------------------------------|------------------------------------------------------------------------------|----------------|
| `backend/data/src/main/resources/migrations`           | Extensions, tables, indexes, and the `ferment_types` reference rows           | every profile  |
| `backend/data/src/main/resources/migrations-seed`      | Recipe library, demo cultures, batches, measurements and journal entries      | `dev`, `test`  |

`ferment_types` is reference data rather than seed data: the target pH and temperature windows stored
there drive the analytics and the over-fermentation detection, so the application is not functional
without it.

Which locations get applied is decided purely by the active Spring profile
(`SPRING_PROFILES_ACTIVE`), set to `prod` in `docker-compose.yaml` and to `dev` by
`docker-compose.dev.yaml`. Under `prod`, Flyway is also configured with `clean-disabled=true` and
`baseline-on-migrate=false`.

Because every seed migration carries a version later than every baseline one, moving an existing
database from `prod` to `dev` applies the seed migrations in order - Flyway never has to run them out
of order.

To reset everything including data:

```bash
docker compose down -v
```

## Security note

The API is currently open: there is no authentication in front of it, and CORS is driven purely by
`CORS_ALLOWED_ORIGINS`. That is fine for a single-user local stack, but it must not be exposed to a
network as-is. Adding authentication means replacing `permitAll` in
`backend/app-rest/.../config/SecurityConfig.kt`.
