import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Batch } from '../../app/core/api/models';
import {
  BATCH_HEALTH_CLASS,
  BATCH_HEALTH_LABEL,
  METRIC_LABEL,
  accentBarClass
} from '../../shared/labels';

/**
 * Batch summary card, shared by the dashboard and the batch list.
 */
@Component({
  selector: 'app-batch-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DecimalPipe],
  template: `
    <a [routerLink]="['/batches', batch().id]"
       class="group relative block overflow-hidden rounded-xl border border-stone-800 bg-stone-900/60
              transition-colors hover:border-stone-700 hover:bg-stone-900">
      <span class="absolute inset-y-0 left-0 w-1" [class]="accent()" aria-hidden="true"></span>

      <div class="p-4 pl-5">
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <p class="truncate font-medium text-stone-100 group-hover:text-white">{{ batch().name }}</p>
            <p class="mt-0.5 text-xs text-stone-500">
              {{ batch().code }} · {{ batch().fermentType.name }}
            </p>
          </div>
          <span class="chip shrink-0" [class]="healthClass[batch().health]">
            {{ healthLabel[batch().health] }}
          </span>
        </div>

        @if (batch().healthReason) {
          <p class="mt-2 text-xs text-stone-400">{{ batch().healthReason }}</p>
        }

        <!-- Progress against the plan -->
        <div class="mt-3">
          <div class="flex items-baseline justify-between text-xs text-stone-400">
            <span>Day {{ batch().dayCount }} of {{ batch().plannedDurationDays }}</span>
            <span>{{ batch().progressPercent }}%</span>
          </div>
          <div class="mt-1 h-1.5 overflow-hidden rounded-full bg-stone-800"
               role="progressbar"
               [attr.aria-valuenow]="batch().progressPercent"
               aria-valuemin="0"
               aria-valuemax="100"
               [attr.aria-label]="'Progress of ' + batch().name">
            <div class="h-full rounded-full transition-all"
                 [class]="batch().overdue ? 'bg-red-500' : 'bg-amber-500'"
                 [style.width.%]="cappedProgress()"></div>
          </div>
        </div>

        <!-- Latest readings -->
        @if (readings().length > 0) {
          <dl class="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs">
            @for (reading of readings(); track reading.id) {
              <div class="flex items-baseline gap-1">
                <dt class="text-stone-500">{{ metricLabel[reading.metric] }}</dt>
                <dd class="font-medium text-stone-200">
                  {{ reading.value | number: '1.0-2' }}{{ reading.unit === '%' ? '' : ' ' }}{{ reading.unit }}
                </dd>
              </div>
            }
          </dl>
        }

        @if (batch().openAlertCount > 0) {
          <p class="mt-3 text-xs text-red-400">
            {{ batch().openAlertCount }} open alert{{ batch().openAlertCount === 1 ? '' : 's' }}
          </p>
        }
      </div>
    </a>
  `
})
export class BatchCardComponent {
  readonly batch = input.required<Batch>();

  protected readonly healthClass = BATCH_HEALTH_CLASS;
  protected readonly healthLabel = BATCH_HEALTH_LABEL;
  protected readonly metricLabel = METRIC_LABEL;

  protected readonly accent = computed(() => accentBarClass(this.batch().fermentType.accentColor));

  /** The bar never overflows its track, even when the batch is well past its plan. */
  protected readonly cappedProgress = computed(() => Math.min(100, Math.max(2, this.batch().progressPercent)));

  /** pH and temperature are the two readings worth showing on a card. */
  protected readonly readings = computed(() =>
    this.batch().latestMeasurements.filter((measurement) =>
      measurement.metric === 'PH' || measurement.metric === 'TEMPERATURE'
    )
  );
}
