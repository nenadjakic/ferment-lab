CREATE INDEX ix_cultures_ferment_type ON cultures (ferment_type_id);
CREATE INDEX ix_cultures_parent ON cultures (parent_culture_id);
CREATE INDEX ix_culture_feedings_culture_fed_at ON culture_feedings (culture_id, fed_at DESC);

CREATE INDEX ix_recipes_ferment_type ON recipes (ferment_type_id);
CREATE INDEX ix_recipe_ingredients_recipe ON recipe_ingredients (recipe_id, ordinal);
CREATE INDEX ix_recipe_steps_recipe ON recipe_steps (recipe_id, ordinal);

CREATE INDEX ix_batches_ferment_type ON batches (ferment_type_id);
CREATE INDEX ix_batches_status_started ON batches (status, started_on DESC);
CREATE INDEX ix_batches_culture ON batches (culture_id);

CREATE INDEX ix_batch_stages_batch ON batch_stages (batch_id, ordinal);
CREATE INDEX ix_batch_events_batch_occurred ON batch_events (batch_id, occurred_at DESC);

-- Charts always read a single batch/metric series ordered by time.
CREATE INDEX ix_measurements_batch_metric_recorded ON measurements (batch_id, metric, recorded_at);

CREATE INDEX ix_alerts_open ON alerts (due_at) WHERE acknowledged_at IS NULL;
