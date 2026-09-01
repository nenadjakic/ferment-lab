import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { BatchService } from '../app/core/api/batch.service';
import {
  BatchDetail,
  BatchEventType,
  BatchOutcome,
  BatchStatus,
  MeasurementAddRequest,
  MetricSeries,
  MetricType
} from '../app/core/api/models';
import { ChartComponent } from '../shared/chart/chart';
import {
  CHART_LINE_COLOR,
  CHART_PALETTE,
  CHART_TEXT_COLOR,
  EChartsOption
} from '../shared/chart/echarts.setup';
import {
  ALERT_ICON,
  BATCH_EVENT_TYPES,
  BATCH_HEALTH_CLASS,
  BATCH_HEALTH_LABEL,
  BATCH_STATUS_CLASS,
  EVENT_ICON,
  METRIC_DEFAULT_UNIT,
  METRIC_LABEL,
  METRIC_TYPES,
  SEVERITY_CLASS,
  STAGE_STATUS_CLASS,
  humanise
} from '../shared/labels';
import { ToastService } from '../shared/toast.service';

@Component({
  selector: 'app-batch-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, DecimalPipe, RouterLink, ChartComponent],
  templateUrl: './batch-detail.html'
})
export class BatchDetailComponent implements OnInit {
  /** Bound from the `:id` route parameter through `withComponentInputBinding()`. */
  readonly id = input.required<string>();

  private readonly batchService = inject(BatchService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly detail = signal<BatchDetail | null>(null);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);

  protected readonly metricTypes = METRIC_TYPES;
  protected readonly eventTypes = BATCH_EVENT_TYPES;
  protected readonly metricLabel = METRIC_LABEL;
  protected readonly metricUnit = METRIC_DEFAULT_UNIT;
  protected readonly healthClass = BATCH_HEALTH_CLASS;
  protected readonly healthLabel = BATCH_HEALTH_LABEL;
  protected readonly statusClass = BATCH_STATUS_CLASS;
  protected readonly stageStatusClass = STAGE_STATUS_CLASS;
  protected readonly severityClass = SEVERITY_CLASS;
  protected readonly eventIcon = EVENT_ICON;
  protected readonly alertIcon = ALERT_ICON;
  protected readonly humanise = humanise;

  // Add-reading form
  protected measurementMetric: MetricType = 'PH';
  protected measurementValue: number | null = null;
  protected measurementNote = '';

  // Add-journal-entry form
  protected eventType: BatchEventType = 'OBSERVATION';
  protected eventTitle = '';
  protected eventDescription = '';

  // Complete-batch form
  protected readonly completeOpen = signal(false);
  protected completeOutcome: BatchOutcome = 'GOOD';
  protected completeRating: number | null = null;
  protected completeNotes = '';

  /**
   * One chart per metric, so a pH line is never squashed by a temperature axis.
   *
   * Series that carry a healthy window get a shaded band, and pH gets a dashed line at the sour limit
   * so the over-fermentation threshold is visible rather than implied.
   */
  protected readonly chartOptions = computed(() =>
    (this.detail()?.series ?? [])
      .filter((series) => series.points.length > 1)
      .map((series) => ({ series, option: this.buildOption(series) }))
  );

  // Required route inputs are only populated after construction, so the first load waits for
  // `ngOnInit`. `id` is a signal input, so re-navigating between batches reloads the page in place.
  ngOnInit(): void {
    this.load();
  }

  protected reload(): void {
    this.load();
  }

  protected addMeasurement(): void {
    if (this.measurementValue === null || Number.isNaN(this.measurementValue)) {
      this.toastService.error('Enter a value to log.');
      return;
    }

    const request: MeasurementAddRequest = {
      metric: this.measurementMetric,
      value: this.measurementValue,
      note: this.measurementNote.trim() || null
    };

    this.busy.set(true);
    this.batchService.addMeasurement(this.id(), request).subscribe({
      next: () => {
        this.measurementValue = null;
        this.measurementNote = '';
        this.toastService.success('Reading logged.');
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected deleteMeasurement(measurementId: string): void {
    this.busy.set(true);
    this.batchService.deleteMeasurement(this.id(), measurementId).subscribe({
      next: () => {
        this.toastService.success('Reading removed.');
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected addEvent(): void {
    if (!this.eventTitle.trim()) {
      this.toastService.error('A title is required.');
      return;
    }

    this.busy.set(true);
    this.batchService
      .addEvent(this.id(), {
        eventType: this.eventType,
        title: this.eventTitle.trim(),
        description: this.eventDescription.trim() || null
      })
      .subscribe({
        next: () => {
          this.eventTitle = '';
          this.eventDescription = '';
          this.toastService.success('Journal entry added.');
          this.load();
        },
        error: () => this.busy.set(false)
      });
  }

  protected startStage(stageId: string): void {
    this.busy.set(true);
    this.batchService.startStage(this.id(), stageId).subscribe({
      next: () => {
        this.toastService.success('Stage started.');
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected completeStage(stageId: string): void {
    this.busy.set(true);
    this.batchService.completeStage(this.id(), stageId).subscribe({
      next: () => {
        this.toastService.success('Stage completed.');
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected changeStatus(status: BatchStatus): void {
    this.busy.set(true);
    this.batchService.changeStatus(this.id(), status).subscribe({
      next: () => {
        this.toastService.success(`Batch ${humanise(status).toLowerCase()}.`);
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected complete(): void {
    this.busy.set(true);
    this.batchService
      .complete(this.id(), {
        outcome: this.completeOutcome,
        rating: this.completeRating,
        notes: this.completeNotes.trim() || null
      })
      .subscribe({
        next: () => {
          this.completeOpen.set(false);
          this.toastService.success('Batch completed.');
          this.load();
        },
        error: () => this.busy.set(false)
      });
  }

  protected deleteBatch(): void {
    this.busy.set(true);
    this.batchService.delete(this.id()).subscribe({
      next: () => {
        this.toastService.success('Batch deleted.');
        void this.router.navigate(['/batches']);
      },
      error: () => this.busy.set(false)
    });
  }

  private load(): void {
    this.loading.set(true);
    this.batchService.findById(this.id()).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.loading.set(false);
        this.busy.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.busy.set(false);
      }
    });
  }

  private buildOption(series: MetricSeries): EChartsOption {
    const hasWindow = series.targetMin !== undefined && series.targetMax !== undefined;

    return {
      color: [CHART_PALETTE[0]],
      grid: { left: 48, right: 16, top: 20, bottom: 32 },
      tooltip: { trigger: 'axis', confine: true },
      xAxis: {
        type: 'category',
        data: series.points.map((point) => `Day ${point.dayOffset}`),
        axisLine: { lineStyle: { color: CHART_LINE_COLOR } },
        axisLabel: { color: CHART_TEXT_COLOR, fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        scale: true,
        name: series.unit,
        nameTextStyle: { color: CHART_TEXT_COLOR, fontSize: 10 },
        splitLine: { lineStyle: { color: CHART_LINE_COLOR } },
        axisLabel: { color: CHART_TEXT_COLOR, fontSize: 10 }
      },
      series: [
        {
          type: 'line',
          name: METRIC_LABEL[series.metric],
          smooth: true,
          showSymbol: series.points.length < 40,
          symbolSize: 5,
          lineStyle: { width: 2 },
          data: series.points.map((point) => point.value),

          // Shaded healthy window for the style.
          markArea: hasWindow
            ? {
                silent: true,
                itemStyle: { color: 'rgba(52, 211, 153, 0.08)' },
                data: [[{ yAxis: series.targetMin }, { yAxis: series.targetMax }]]
              }
            : undefined,

          // Dashed sour limit: crossing it is what the over-fermentation detection reacts to.
          markLine: series.dangerThreshold
            ? {
                silent: true,
                symbol: 'none',
                lineStyle: { color: '#f87171', type: 'dashed' },
                label: {
                  formatter: `sour limit ${series.dangerThreshold}`,
                  color: '#f87171',
                  fontSize: 10,
                  position: 'insideEndTop'
                },
                data: [{ yAxis: series.dangerThreshold }]
              }
            : undefined
        }
      ]
    };
  }
}
