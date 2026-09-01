/**
 * Wire types for the FermentLab API.
 *
 * These mirror the Kotlin DTOs in `backend/common`. They are hand written rather than generated so
 * the frontend can be built without a running backend; when the API changes, the OpenAPI document at
 * `/v3/api-docs` is the source of truth to diff against.
 */

export type FermentCategory =
  | 'BEVERAGE'
  | 'VEGETABLE'
  | 'DAIRY'
  | 'DOUGH'
  | 'CONDIMENT'
  | 'LEGUME'
  | 'MEAT';

export type CultureKind =
  | 'SCOBY'
  | 'SOURDOUGH_STARTER'
  | 'KEFIR_GRAINS'
  | 'VINEGAR_MOTHER'
  | 'YOGHURT_CULTURE'
  | 'KOJI'
  | 'BRINE_BACKSLOP'
  | 'WILD';

export type CultureHealth = 'THRIVING' | 'HEALTHY' | 'SLUGGISH' | 'DORMANT' | 'LOST';

export type BatchStatus = 'PLANNED' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'DISCARDED' | 'ARCHIVED';

export type BatchOutcome = 'EXCELLENT' | 'GOOD' | 'ACCEPTABLE' | 'POOR' | 'FAILED';

export type BatchHealth =
  | 'PLANNED'
  | 'ON_TRACK'
  | 'READY'
  | 'ATTENTION'
  | 'OVER_FERMENTED'
  | 'CLOSED';

export type StagePhase =
  | 'PREP'
  | 'PRIMARY'
  | 'SECONDARY'
  | 'CONDITIONING'
  | 'COLD_STORAGE'
  | 'CURING'
  | 'CUSTOM';

export type StageStatus = 'PENDING' | 'ACTIVE' | 'OVERDUE' | 'COMPLETED';

export type MetricType =
  | 'PH'
  | 'TEMPERATURE'
  | 'BRIX'
  | 'SPECIFIC_GRAVITY'
  | 'SALINITY'
  | 'TITRATABLE_ACIDITY'
  | 'ABV'
  | 'WEIGHT'
  | 'HYDRATION';

export type BatchEventType =
  | 'OBSERVATION'
  | 'TASTING'
  | 'FEEDING'
  | 'BURPING'
  | 'STIRRING'
  | 'STAGE_STARTED'
  | 'STAGE_COMPLETED'
  | 'HARVEST'
  | 'ISSUE';

export type AlertType =
  | 'MILESTONE_DUE'
  | 'STAGE_OVERDUE'
  | 'HARVEST_WINDOW'
  | 'OVER_FERMENTATION_RISK'
  | 'TEMPERATURE_OUT_OF_RANGE'
  | 'MEASUREMENT_STALE'
  | 'CULTURE_FEED_DUE';

export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL';

export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export interface FermentTypeSimple {
  id: string;
  code: string;
  name: string;
  category: FermentCategory;
  accentColor?: string;
}

export interface FermentType extends FermentTypeSimple {
  description?: string;
  defaultDurationDays: number;
  targetPhMin?: number;
  targetPhMax?: number;
  overFermentationPh?: number;
  targetTempMinC?: number;
  targetTempMaxC?: number;
}

export interface NameValue {
  name: string;
  value: number;
}

export interface Measurement {
  id: string;
  batchId: string;
  metric: MetricType;
  value: number;
  unit: string;
  recordedAt: string;
  dayOffset: number;
  note?: string;
}

export interface MetricPoint {
  recordedAt: string;
  dayOffset: number;
  value: number;
}

export interface MetricSeries {
  metric: MetricType;
  unit: string;
  targetMin?: number;
  targetMax?: number;
  dangerThreshold?: number;
  points: MetricPoint[];
}

export interface BatchStage {
  id: string;
  position: number;
  name: string;
  phase: StagePhase;
  instruction?: string;
  plannedDurationDays: number;
  startedAt?: string;
  completedAt?: string;
  note?: string;
  status: StageStatus;
  dueAt?: string;
  daysRemaining?: number;
}

export interface BatchEvent {
  id: string;
  batchId: string;
  batchCode: string;
  eventType: BatchEventType;
  occurredAt: string;
  title: string;
  description?: string;
}

export interface Alert {
  id: string;
  batchId?: string;
  batchCode?: string;
  batchName?: string;
  cultureId?: string;
  cultureName?: string;
  alertType: AlertType;
  severity: AlertSeverity;
  message: string;
  dueAt: string;
  acknowledgedAt?: string;
  createdAt: string;
}

export interface Batch {
  id: string;
  code: string;
  name: string;
  fermentType: FermentTypeSimple;
  recipeId?: string;
  recipeName?: string;
  cultureId?: string;
  cultureName?: string;
  status: BatchStatus;
  startedOn: string;
  expectedEndOn: string;
  endedOn?: string;
  container?: string;
  batchSize?: number;
  batchSizeUnit?: string;
  location?: string;
  rating?: number;
  outcome?: BatchOutcome;
  dayCount: number;
  plannedDurationDays: number;
  progressPercent: number;
  overdue: boolean;
  health: BatchHealth;
  healthReason?: string;
  latestMeasurements: Measurement[];
  openAlertCount: number;
}

export interface BatchDetail {
  batch: Batch;
  notes?: string;
  stages: BatchStage[];
  events: BatchEvent[];
  series: MetricSeries[];
  alerts: Alert[];
  measurementCount: number;
}

export interface Culture {
  id: string;
  name: string;
  fermentType: FermentTypeSimple;
  kind: CultureKind;
  parentId?: string;
  parentName?: string;
  generation: number;
  acquiredOn: string;
  health: CultureHealth;
  feedIntervalDays?: number;
  lastFedOn?: string;
  nextFeedDueOn?: string;
  feedDue: boolean;
  flavorProfile?: string;
  notes?: string;
  active: boolean;
  childCount: number;
}

export interface CultureTreeNode {
  id: string;
  name: string;
  kind: CultureKind;
  health: CultureHealth;
  generation: number;
  acquiredOn: string;
  flavorProfile?: string;
  children: CultureTreeNode[];
}

export interface CultureFeeding {
  id: string;
  cultureId: string;
  fedAt: string;
  amount?: number;
  unit?: string;
  note?: string;
}

export interface RecipeIngredient {
  id: string;
  position: number;
  name: string;
  quantity?: number;
  unit?: string;
  note?: string;
}

export interface RecipeStep {
  id: string;
  position: number;
  title: string;
  instruction: string;
  phase: StagePhase;
  durationDays: number;
}

export interface Recipe {
  id: string;
  name: string;
  fermentType: FermentTypeSimple;
  difficulty: Difficulty;
  summary?: string;
  totalDurationDays: number;
  yieldAmount?: number;
  yieldUnit?: string;
  salinityPercent?: number;
  hydrationPercent?: number;
  source?: string;
  featured: boolean;
  stepCount: number;
  ingredientCount: number;
}

export interface RecipeDetail {
  recipe: Recipe;
  ingredients: RecipeIngredient[];
  steps: RecipeStep[];
}

export interface Milestone {
  batchId: string;
  batchCode: string;
  batchName: string;
  stageId?: string;
  title: string;
  dueAt: string;
  daysRemaining: number;
}

export interface DashboardSummary {
  activeBatchCount: number;
  plannedBatchCount: number;
  completedBatchCount: number;
  activeCultureCount: number;
  openAlertCount: number;
  averageRating?: number;
  batchesPerFermentType: NameValue[];
  activeBatches: Batch[];
  attentionBatches: Batch[];
  upcomingMilestones: Milestone[];
  recentEvents: BatchEvent[];
  openAlerts: Alert[];
}

export interface ComparedBatch {
  batchId: string;
  code: string;
  name: string;
  fermentTypeName: string;
  accentColor?: string;
  startedOn: string;
  dayCount: number;
  rating?: number;
  outcome?: BatchOutcome;
  series: MetricSeries[];
}

export interface BatchComparison {
  metrics: MetricType[];
  batches: ComparedBatch[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

// ---------------------------------------------------------------------------------------------
// Request payloads
// ---------------------------------------------------------------------------------------------

export interface BatchAddRequest {
  name: string;
  fermentTypeId: string;
  recipeId?: string | null;
  cultureId?: string | null;
  startedOn?: string | null;
  expectedEndOn?: string | null;
  container?: string | null;
  batchSize?: number | null;
  batchSizeUnit?: string | null;
  location?: string | null;
  notes?: string | null;
}

export interface BatchEditRequest {
  name: string;
  expectedEndOn: string;
  cultureId?: string | null;
  container?: string | null;
  batchSize?: number | null;
  batchSizeUnit?: string | null;
  location?: string | null;
  rating?: number | null;
  notes?: string | null;
}

export interface BatchCompleteRequest {
  outcome: BatchOutcome;
  rating?: number | null;
  endedOn?: string | null;
  notes?: string | null;
}

export interface BatchStageAddRequest {
  name: string;
  phase: StagePhase;
  instruction?: string | null;
  plannedDurationDays: number;
}

export interface BatchEventAddRequest {
  eventType: BatchEventType;
  title: string;
  description?: string | null;
  occurredAt?: string | null;
}

export interface MeasurementAddRequest {
  metric: MetricType;
  value: number;
  unit?: string | null;
  recordedAt?: string | null;
  note?: string | null;
}

export interface CultureAddRequest {
  name: string;
  fermentTypeId: string;
  kind: CultureKind;
  parentCultureId?: string | null;
  acquiredOn?: string | null;
  health: CultureHealth;
  feedIntervalDays?: number | null;
  flavorProfile?: string | null;
  notes?: string | null;
}

export interface CultureUpdateRequest {
  name: string;
  health: CultureHealth;
  feedIntervalDays?: number | null;
  flavorProfile?: string | null;
  notes?: string | null;
  active: boolean;
}

export interface CultureSplitRequest {
  name: string;
  notes?: string | null;
}

export interface CultureFeedingRequest {
  fedAt?: string | null;
  amount?: number | null;
  unit?: string | null;
  note?: string | null;
}

export interface RecipeIngredientRequest {
  name: string;
  quantity?: number | null;
  unit?: string | null;
  note?: string | null;
}

export interface RecipeStepRequest {
  title: string;
  instruction: string;
  phase: StagePhase;
  durationDays: number;
}

/**
 * Payload for creating a recipe.
 *
 * Ordering of [ingredients] and [steps] is taken from the array order, and the recipe's total
 * duration is derived server side from the step durations, so neither is sent explicitly.
 */
export interface RecipeAddRequest {
  name: string;
  fermentTypeId: string;
  difficulty: Difficulty;
  summary?: string | null;
  yieldAmount?: number | null;
  yieldUnit?: string | null;
  salinityPercent?: number | null;
  hydrationPercent?: number | null;
  source?: string | null;
  featured: boolean;
  ingredients: RecipeIngredientRequest[];
  steps: RecipeStepRequest[];
}

/**
 * Error body returned by every failing endpoint.
 */
export interface ErrorInfo {
  status: number;
  error: string;
  message: string;
  path?: string;
  fieldErrors?: Record<string, string>;
  timestamp: string;
}
