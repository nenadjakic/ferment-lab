-- Reference data: styles of ferment together with their healthy target windows.
CREATE TABLE ferment_types
(
    id                    uuid          NOT NULL,
    code                  varchar(40)   NOT NULL,
    name                  varchar(100)  NOT NULL,
    category              varchar(30)   NOT NULL,
    description           varchar(1000),
    default_duration_days integer       NOT NULL,
    target_ph_min         numeric(4, 2),
    target_ph_max         numeric(4, 2),
    over_fermentation_ph  numeric(4, 2),
    target_temp_min_c     numeric(5, 2),
    target_temp_max_c     numeric(5, 2),
    accent_color          varchar(16),
    CONSTRAINT pk_ferment_types PRIMARY KEY (id),
    CONSTRAINT uq_ferment_types_code UNIQUE (code),
    CONSTRAINT ck_ferment_types_category
        CHECK (category IN ('BEVERAGE', 'VEGETABLE', 'DAIRY', 'DOUGH', 'CONDIMENT', 'LEGUME', 'MEAT')),
    CONSTRAINT ck_ferment_types_duration CHECK (default_duration_days > 0)
);

-- The culture library. parent_culture_id turns the table into a family tree.
CREATE TABLE cultures
(
    id                 uuid         NOT NULL,
    name               varchar(120) NOT NULL,
    ferment_type_id    uuid         NOT NULL,
    kind               varchar(30)  NOT NULL,
    parent_culture_id  uuid,
    generation         integer      NOT NULL DEFAULT 1,
    acquired_on        date         NOT NULL,
    health             varchar(20)  NOT NULL DEFAULT 'HEALTHY',
    feed_interval_days integer,
    last_fed_on        date,
    flavor_profile     varchar(255),
    notes              varchar(2000),
    active             boolean      NOT NULL DEFAULT true,
    created_at         timestamptz  NOT NULL DEFAULT now(),
    updated_at         timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_cultures PRIMARY KEY (id),
    CONSTRAINT fk_cultures_ferment_type FOREIGN KEY (ferment_type_id) REFERENCES ferment_types (id),
    CONSTRAINT fk_cultures_parent FOREIGN KEY (parent_culture_id) REFERENCES cultures (id),
    CONSTRAINT ck_cultures_kind
        CHECK (kind IN ('SCOBY', 'SOURDOUGH_STARTER', 'KEFIR_GRAINS', 'VINEGAR_MOTHER',
                        'YOGHURT_CULTURE', 'KOJI', 'BRINE_BACKSLOP', 'WILD')),
    CONSTRAINT ck_cultures_health
        CHECK (health IN ('THRIVING', 'HEALTHY', 'SLUGGISH', 'DORMANT', 'LOST')),
    CONSTRAINT ck_cultures_generation CHECK (generation >= 1),
    CONSTRAINT ck_cultures_feed_interval CHECK (feed_interval_days IS NULL OR feed_interval_days > 0)
);

CREATE TABLE culture_feedings
(
    id         uuid        NOT NULL,
    culture_id uuid        NOT NULL,
    fed_at     timestamptz NOT NULL DEFAULT now(),
    amount     numeric(10, 3),
    unit       varchar(10),
    note       varchar(500),
    CONSTRAINT pk_culture_feedings PRIMARY KEY (id),
    CONSTRAINT fk_culture_feedings_culture FOREIGN KEY (culture_id) REFERENCES cultures (id) ON DELETE CASCADE
);

CREATE TABLE recipes
(
    id                  uuid         NOT NULL,
    name                varchar(150) NOT NULL,
    ferment_type_id     uuid         NOT NULL,
    difficulty          varchar(20)  NOT NULL DEFAULT 'BEGINNER',
    summary             varchar(1000),
    total_duration_days integer      NOT NULL,
    yield_amount        numeric(8, 3),
    yield_unit          varchar(10),
    salinity_percent    numeric(5, 2),
    hydration_percent   numeric(5, 2),
    source              varchar(150),
    featured            boolean      NOT NULL DEFAULT false,
    created_at          timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_recipes PRIMARY KEY (id),
    CONSTRAINT fk_recipes_ferment_type FOREIGN KEY (ferment_type_id) REFERENCES ferment_types (id),
    CONSTRAINT ck_recipes_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_recipes_duration CHECK (total_duration_days > 0)
);

CREATE TABLE recipe_ingredients
(
    id        uuid         NOT NULL,
    recipe_id uuid         NOT NULL,
    ordinal   integer      NOT NULL,
    name      varchar(120) NOT NULL,
    quantity  numeric(10, 3),
    unit      varchar(15),
    note      varchar(255),
    CONSTRAINT pk_recipe_ingredients PRIMARY KEY (id),
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
    CONSTRAINT uq_recipe_ingredients_ordinal UNIQUE (recipe_id, ordinal)
);

CREATE TABLE recipe_steps
(
    id            uuid          NOT NULL,
    recipe_id     uuid          NOT NULL,
    ordinal       integer       NOT NULL,
    title         varchar(150)  NOT NULL,
    instruction   varchar(2000) NOT NULL,
    phase         varchar(20)   NOT NULL DEFAULT 'PRIMARY',
    duration_days integer       NOT NULL DEFAULT 0,
    CONSTRAINT pk_recipe_steps PRIMARY KEY (id),
    CONSTRAINT fk_recipe_steps_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
    CONSTRAINT uq_recipe_steps_ordinal UNIQUE (recipe_id, ordinal),
    CONSTRAINT ck_recipe_steps_phase
        CHECK (phase IN ('PREP', 'PRIMARY', 'SECONDARY', 'CONDITIONING', 'COLD_STORAGE', 'CURING', 'CUSTOM')),
    CONSTRAINT ck_recipe_steps_duration CHECK (duration_days >= 0)
);

CREATE TABLE batches
(
    id              uuid         NOT NULL,
    code            varchar(20)  NOT NULL,
    name            varchar(150) NOT NULL,
    ferment_type_id uuid         NOT NULL,
    recipe_id       uuid,
    culture_id      uuid,
    status          varchar(20)  NOT NULL DEFAULT 'ACTIVE',
    started_on      date         NOT NULL,
    expected_end_on date         NOT NULL,
    ended_on        date,
    container       varchar(100),
    batch_size      numeric(8, 3),
    batch_size_unit varchar(10),
    location        varchar(100),
    rating          integer,
    outcome         varchar(20),
    notes           varchar(2000),
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_batches PRIMARY KEY (id),
    CONSTRAINT uq_batches_code UNIQUE (code),
    CONSTRAINT fk_batches_ferment_type FOREIGN KEY (ferment_type_id) REFERENCES ferment_types (id),
    CONSTRAINT fk_batches_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE SET NULL,
    CONSTRAINT fk_batches_culture FOREIGN KEY (culture_id) REFERENCES cultures (id) ON DELETE SET NULL,
    CONSTRAINT ck_batches_status
        CHECK (status IN ('PLANNED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'DISCARDED', 'ARCHIVED')),
    CONSTRAINT ck_batches_outcome
        CHECK (outcome IS NULL OR outcome IN ('EXCELLENT', 'GOOD', 'ACCEPTABLE', 'POOR', 'FAILED')),
    CONSTRAINT ck_batches_rating CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5)),
    CONSTRAINT ck_batches_dates CHECK (expected_end_on >= started_on)
);

CREATE TABLE batch_stages
(
    id                    uuid         NOT NULL,
    batch_id              uuid         NOT NULL,
    ordinal               integer      NOT NULL,
    name                  varchar(150) NOT NULL,
    phase                 varchar(20)  NOT NULL DEFAULT 'PRIMARY',
    instruction           varchar(2000),
    planned_duration_days integer      NOT NULL DEFAULT 0,
    started_at            timestamptz,
    completed_at          timestamptz,
    note                  varchar(1000),
    CONSTRAINT pk_batch_stages PRIMARY KEY (id),
    CONSTRAINT fk_batch_stages_batch FOREIGN KEY (batch_id) REFERENCES batches (id) ON DELETE CASCADE,
    CONSTRAINT uq_batch_stages_ordinal UNIQUE (batch_id, ordinal),
    CONSTRAINT ck_batch_stages_phase
        CHECK (phase IN ('PREP', 'PRIMARY', 'SECONDARY', 'CONDITIONING', 'COLD_STORAGE', 'CURING', 'CUSTOM')),
    CONSTRAINT ck_batch_stages_duration CHECK (planned_duration_days >= 0)
);

CREATE TABLE measurements
(
    id           uuid           NOT NULL,
    batch_id     uuid           NOT NULL,
    metric       varchar(30)    NOT NULL,
    metric_value numeric(12, 4) NOT NULL,
    unit         varchar(15)    NOT NULL,
    recorded_at  timestamptz    NOT NULL DEFAULT now(),
    note         varchar(500),
    CONSTRAINT pk_measurements PRIMARY KEY (id),
    CONSTRAINT fk_measurements_batch FOREIGN KEY (batch_id) REFERENCES batches (id) ON DELETE CASCADE,
    CONSTRAINT ck_measurements_metric
        CHECK (metric IN ('PH', 'TEMPERATURE', 'BRIX', 'SPECIFIC_GRAVITY', 'SALINITY',
                          'TITRATABLE_ACIDITY', 'ABV', 'WEIGHT', 'HYDRATION'))
);

CREATE TABLE batch_events
(
    id          uuid         NOT NULL,
    batch_id    uuid         NOT NULL,
    event_type  varchar(30)  NOT NULL,
    occurred_at timestamptz  NOT NULL DEFAULT now(),
    title       varchar(150) NOT NULL,
    description varchar(2000),
    CONSTRAINT pk_batch_events PRIMARY KEY (id),
    CONSTRAINT fk_batch_events_batch FOREIGN KEY (batch_id) REFERENCES batches (id) ON DELETE CASCADE,
    CONSTRAINT ck_batch_events_type
        CHECK (event_type IN ('OBSERVATION', 'TASTING', 'FEEDING', 'BURPING', 'STIRRING',
                              'STAGE_STARTED', 'STAGE_COMPLETED', 'HARVEST', 'ISSUE'))
);

CREATE TABLE alerts
(
    id              uuid         NOT NULL,
    batch_id        uuid,
    culture_id      uuid,
    alert_type      varchar(40)  NOT NULL,
    severity        varchar(10)  NOT NULL DEFAULT 'INFO',
    message         varchar(500) NOT NULL,
    due_at          timestamptz  NOT NULL DEFAULT now(),
    dedupe_key      varchar(200) NOT NULL,
    acknowledged_at timestamptz,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_alerts PRIMARY KEY (id),
    CONSTRAINT uq_alerts_dedupe_key UNIQUE (dedupe_key),
    CONSTRAINT fk_alerts_batch FOREIGN KEY (batch_id) REFERENCES batches (id) ON DELETE CASCADE,
    CONSTRAINT fk_alerts_culture FOREIGN KEY (culture_id) REFERENCES cultures (id) ON DELETE CASCADE,
    CONSTRAINT ck_alerts_type
        CHECK (alert_type IN ('MILESTONE_DUE', 'STAGE_OVERDUE', 'HARVEST_WINDOW', 'OVER_FERMENTATION_RISK',
                              'TEMPERATURE_OUT_OF_RANGE', 'MEASUREMENT_STALE', 'CULTURE_FEED_DUE')),
    CONSTRAINT ck_alerts_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    CONSTRAINT ck_alerts_subject CHECK (batch_id IS NOT NULL OR culture_id IS NOT NULL)
);
