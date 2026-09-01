import {
  AlertSeverity,
  AlertType,
  BatchEventType,
  BatchHealth,
  BatchStatus,
  CultureHealth,
  CultureKind,
  Difficulty,
  MetricType,
  StagePhase,
  StageStatus
} from '../app/core/api/models';

/**
 * Display labels and colour tokens shared across the feature pages, kept in one place so a status
 * always looks the same wherever it appears.
 */

export function humanise(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((part, index) => (index === 0 ? part.charAt(0).toUpperCase() + part.slice(1) : part))
    .join(' ');
}

export const BATCH_HEALTH_LABEL: Record<BatchHealth, string> = {
  PLANNED: 'Planned',
  ON_TRACK: 'On track',
  READY: 'Ready',
  ATTENTION: 'Needs attention',
  OVER_FERMENTED: 'Over-fermented',
  CLOSED: 'Closed'
};

export const BATCH_HEALTH_CLASS: Record<BatchHealth, string> = {
  PLANNED: 'bg-stone-800 text-stone-300',
  ON_TRACK: 'bg-emerald-950 text-emerald-300',
  READY: 'bg-amber-950 text-amber-300',
  ATTENTION: 'bg-orange-950 text-orange-300',
  OVER_FERMENTED: 'bg-red-950 text-red-300',
  CLOSED: 'bg-stone-800 text-stone-400'
};

export const BATCH_STATUS_CLASS: Record<BatchStatus, string> = {
  PLANNED: 'bg-sky-950 text-sky-300',
  ACTIVE: 'bg-emerald-950 text-emerald-300',
  PAUSED: 'bg-amber-950 text-amber-300',
  COMPLETED: 'bg-stone-800 text-stone-300',
  DISCARDED: 'bg-red-950 text-red-300',
  ARCHIVED: 'bg-stone-800 text-stone-400'
};

export const SEVERITY_CLASS: Record<AlertSeverity, string> = {
  INFO: 'bg-stone-800 text-stone-300',
  WARNING: 'bg-amber-950 text-amber-300',
  CRITICAL: 'bg-red-950 text-red-300'
};

export const CULTURE_HEALTH_CLASS: Record<CultureHealth, string> = {
  THRIVING: 'bg-emerald-950 text-emerald-300',
  HEALTHY: 'bg-sky-950 text-sky-300',
  SLUGGISH: 'bg-amber-950 text-amber-300',
  DORMANT: 'bg-stone-800 text-stone-300',
  LOST: 'bg-red-950 text-red-300'
};

export const STAGE_STATUS_CLASS: Record<StageStatus, string> = {
  PENDING: 'bg-stone-800 text-stone-400',
  ACTIVE: 'bg-emerald-950 text-emerald-300',
  OVERDUE: 'bg-red-950 text-red-300',
  COMPLETED: 'bg-stone-800 text-stone-300'
};

export const METRIC_LABEL: Record<MetricType, string> = {
  PH: 'pH',
  TEMPERATURE: 'Temperature',
  BRIX: 'Brix',
  SPECIFIC_GRAVITY: 'Specific gravity',
  SALINITY: 'Salinity',
  TITRATABLE_ACIDITY: 'Titratable acidity',
  ABV: 'ABV',
  WEIGHT: 'Weight',
  HYDRATION: 'Hydration'
};

export const METRIC_DEFAULT_UNIT: Record<MetricType, string> = {
  PH: 'pH',
  TEMPERATURE: '°C',
  BRIX: '°Bx',
  SPECIFIC_GRAVITY: 'SG',
  SALINITY: '%',
  TITRATABLE_ACIDITY: 'g/L',
  ABV: '%',
  WEIGHT: 'g',
  HYDRATION: '%'
};

export const METRIC_TYPES: MetricType[] = [
  'PH',
  'TEMPERATURE',
  'BRIX',
  'SPECIFIC_GRAVITY',
  'SALINITY',
  'TITRATABLE_ACIDITY',
  'ABV',
  'WEIGHT',
  'HYDRATION'
];

export const STAGE_PHASES: StagePhase[] = [
  'PREP',
  'PRIMARY',
  'SECONDARY',
  'CONDITIONING',
  'COLD_STORAGE',
  'CURING',
  'CUSTOM'
];

export const BATCH_EVENT_TYPES: BatchEventType[] = [
  'OBSERVATION',
  'TASTING',
  'FEEDING',
  'BURPING',
  'STIRRING',
  'HARVEST',
  'ISSUE'
];

export const CULTURE_KINDS: CultureKind[] = [
  'SCOBY',
  'SOURDOUGH_STARTER',
  'KEFIR_GRAINS',
  'VINEGAR_MOTHER',
  'YOGHURT_CULTURE',
  'KOJI',
  'BRINE_BACKSLOP',
  'WILD'
];

export const CULTURE_HEALTHS: CultureHealth[] = ['THRIVING', 'HEALTHY', 'SLUGGISH', 'DORMANT', 'LOST'];

export const DIFFICULTIES: Difficulty[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];

export const DIFFICULTY_CLASS: Record<Difficulty, string> = {
  BEGINNER: 'bg-emerald-950 text-emerald-300',
  INTERMEDIATE: 'bg-amber-950 text-amber-300',
  ADVANCED: 'bg-rose-950 text-rose-300'
};

/** Units offered for ingredient quantities and recipe yields. */
export const UNITS: string[] = ['g', 'kg', 'ml', 'L', 'tsp', 'tbsp', 'pc'];

export const BATCH_STATUSES: BatchStatus[] = [
  'PLANNED',
  'ACTIVE',
  'PAUSED',
  'COMPLETED',
  'DISCARDED',
  'ARCHIVED'
];

export const EVENT_ICON: Record<BatchEventType, string> = {
  OBSERVATION: '👁',
  TASTING: '👅',
  FEEDING: '🥄',
  BURPING: '💨',
  STIRRING: '🌀',
  STAGE_STARTED: '▶',
  STAGE_COMPLETED: '✔',
  HARVEST: '🫙',
  ISSUE: '⚠'
};

export const ALERT_ICON: Record<AlertType, string> = {
  MILESTONE_DUE: '⏱',
  STAGE_OVERDUE: '⏰',
  HARVEST_WINDOW: '🫙',
  OVER_FERMENTATION_RISK: '🔥',
  TEMPERATURE_OUT_OF_RANGE: '🌡',
  MEASUREMENT_STALE: '📉',
  CULTURE_FEED_DUE: '🥄'
};

/**
 * Tailwind background tokens for the ferment style accent colours stored in the database.
 */
export function accentBarClass(accent?: string): string {
  const map: Record<string, string> = {
    amber: 'bg-amber-500',
    sky: 'bg-sky-500',
    indigo: 'bg-indigo-500',
    violet: 'bg-violet-500',
    orange: 'bg-orange-500',
    rose: 'bg-rose-500',
    lime: 'bg-lime-500',
    emerald: 'bg-emerald-500',
    red: 'bg-red-500',
    yellow: 'bg-yellow-500',
    teal: 'bg-teal-500',
    green: 'bg-green-500',
    stone: 'bg-stone-500',
    fuchsia: 'bg-fuchsia-500'
  };

  return map[accent ?? ''] ?? 'bg-stone-500';
}

/**
 * "in 3 days" / "2 days ago" / "today", from a whole number of days.
 */
export function relativeDays(days: number): string {
  if (days === 0) {
    return 'today';
  }
  if (days === 1) {
    return 'tomorrow';
  }
  if (days === -1) {
    return 'yesterday';
  }

  return days > 0 ? `in ${days} days` : `${Math.abs(days)} days ago`;
}
