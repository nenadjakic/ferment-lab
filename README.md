# FermentLab

A fermentation journal for home fermenters, modelled on the feature set of the
[Fermently](https://fermently-legal.vercel.app/) iOS app: batch tracking, scientific charts,
side by side batch comparison, smart notifications, a recipe library and over-fermentation detection.
*(Feature list paraphrased from the Fermently marketing site; content was rephrased for compliance
with licensing restrictions.)*

Kombucha, kimchi, kefir, sourdough, hot sauce, mead - every run gets a timeline, a journal, a
measurement history and a verdict.

## Features

| Feature | Where it lives |
|---|---|
| **Batch tracking** from day one to completion, with a stage timeline generated from the recipe | `BatchService`, `/batches` |
| **Scientific charts** for pH, temperature, Brix, gravity and more, with the healthy window for the style shaded in | `MeasurementService.buildSeries`, batch detail page |
| **Side by side comparison** of two to six batches, aligned by day offset so runs weeks apart overlay cleanly | `AnalyticsService.compare`, `/compare` |
| **Smart notifications** - milestone reminders, overdue stages, harvest windows, temperature excursions, stale logs, culture feeds | `AlertEvaluationService`, `scheduler` module, `/alerts` |
| **Over-fermentation detection** - a batch whose newest pH crosses the sour limit for its style is flagged everywhere it appears | `BatchExtension.deriveHealth`, `AlertEvaluationService` |
| **Recipes and guides** with ingredients, step by step method, in-app authoring and one-click "start this batch" | `RecipeService`, `/recipes` |
| **Culture library** with health, feed schedules and multi-generation family trees | `CultureService`, `/cultures` |

## Layout

```
backend/           Kotlin + Spring Boot, multi-module Gradle build
  data/            JPA entities, repositories, Flyway migrations
  common/          DTOs, API enums, entity -> DTO mappers, exceptions
  service/         Business logic
  app-rest/        REST API + OpenAPI + security config
  scheduler/       Alert evaluation jobs
frontend/          Angular standalone application, Tailwind CSS, ECharts
infrastructure/    Dockerfiles, nginx config, docker-compose stack
```

## Run everything

The Compose stack is a production deployment: schema and reference data only.

```bash
cd infrastructure
docker compose up --build
```

For a stack seeded with the demo library, add the dev overlay:

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

The demo library is 8 recipes, 9 cultures including a three generation SCOBY family tree, and 12
batches across the whole lifecycle with generated pH, temperature and Brix series. One batch is
deliberately pushed past its sour limit so the over-fermentation detection is visible on first load.
The 14 ferment styles are reference data and are created in every environment.

## Run the pieces separately

Backend (needs a Postgres on `localhost:5432`, JDK 21):

```bash
cd backend
./gradlew :app-rest:bootRun --args='--spring.profiles.active=dev'
./gradlew :scheduler:bootRun
```

Without a profile the API behaves like production and applies no seed data.

Frontend (Node 22):

```bash
cd frontend
npm install
npm start          # http://localhost:4200, proxies /api to localhost:8080
```

## Stack

- Kotlin 2.3, Spring Boot 4.0, Spring Data JPA, Flyway, PostgreSQL 17, Gradle 9.2, JDK 21
- Angular 20 standalone components with signals, Tailwind CSS 4, ECharts 5
- Docker Compose, nginx

## Conventions

Structure and style follow [nenadjakic/investiq](https://github.com/nenadjakic/investiq): a layered
multi-module Gradle backend (`data` / `common` / `service` / `app-rest` / `scheduler`), version
catalog in `gradle/libs.versions.toml`, timestamped Flyway migrations under
`data/src/main/resources/migrations`, DTO mapping through Kotlin extension functions in
`common/extension`, and an Angular frontend with `src/app/core` for infrastructure and one directory
per feature under `src/`.

Two deliberate deviations:

- **API types are hand written** in `frontend/src/app/core/api/models.ts` rather than generated from
  OpenAPI, so the frontend builds without a running backend. `/v3/api-docs` remains the source of
  truth to diff against.
- **Charts use ECharts directly** through a small `app-chart` wrapper rather than a wrapper library,
  which avoids a version-compatibility dependency and keeps the registered chart types explicit.

## Security

There is no authentication: every endpoint is `permitAll` and CORS is driven by
`FERMENTLAB_CORS_ALLOWED_ORIGINS`. That is deliberate for a single-user local stack, but the API must
not be exposed to a network as-is. See `infrastructure/README.md`.
