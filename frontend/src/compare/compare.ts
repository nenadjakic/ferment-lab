import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AnalyticsService } from '../app/core/api/analytics.service';
import { BatchService } from '../app/core/api/batch.service';
import { Batch, BatchComparison, MetricType } from '../app/core/api/models';
import { ChartComponent } from '../shared/chart/chart';
import {
  CHART_LINE_COLOR,
  CHART_PALETTE,
  CHART_TEXT_COLOR,
  EChartsOption
} from '../shared/chart/echarts.setup';
import { METRIC_LABEL, METRIC_TYPES, humanise } from '../shared/labels';
import { ToastService } from '../shared/toast.service';

/**
 * Side by side batch comparison.
 *
 * Batches are plotted against day offset rather than date, which is the whole point of the page:
 * a batch from March and one from August line up so the difference in the curve is the signal.
 */
@Component({
  selector: 'app-compare',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, DatePipe, ChartComponent],
  templateUrl: './compare.html'
})
export class CompareComponent {
  private static readonly MAX_BATCHES = 6;

  private readonly batchService = inject(BatchService);
  private readonly analyticsService = inject(AnalyticsService);
  private readonly toastService = inject(ToastService);

  protected readonly candidates = signal<Batch[]>([]);
  protected readonly selectedIds = signal<string[]>([]);
  protected readonly selectedMetrics = signal<MetricType[]>(['PH', 'TEMPERATURE']);
  protected readonly comparison = signal<BatchComparison | null>(null);
  protected readonly loading = signal(true);
  protected readonly comparing = signal(false);

  protected readonly metricTypes = METRIC_TYPES;
  protected readonly metricLabel = METRIC_LABEL;
  protected readonly humanise = humanise;

  protected readonly canCompare = computed(
    () => this.selectedIds().length >= 2 && this.selectedMetrics().length > 0
  );

  /**
   * One chart per metric, with a line per batch. Colours follow the selection order so a batch keeps
   * its colour across every chart on the page.
   */
  protected readonly charts = computed(() => {
    const comparison = this.comparison();
    if (!comparison) {
      return [];
    }

    return comparison.metrics
      .map((metric) => ({ metric, option: this.buildOption(comparison, metric) }))
      .filter((chart) => chart.option !== null) as { metric: MetricType; option: EChartsOption }[];
  });

  constructor() {
    // Comparison is most useful across finished batches, so search rather than only open ones.
    this.batchService.search({ size: 100 }).subscribe({
      next: (page) => {
        this.candidates.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected isSelected(id: string): boolean {
    return this.selectedIds().includes(id);
  }

  protected toggleBatch(id: string): void {
    const current = this.selectedIds();

    if (current.includes(id)) {
      this.selectedIds.set(current.filter((candidate) => candidate !== id));
      return;
    }

    if (current.length >= CompareComponent.MAX_BATCHES) {
      this.toastService.info(`Up to ${CompareComponent.MAX_BATCHES} batches at a time.`);
      return;
    }

    this.selectedIds.set([...current, id]);
  }

  protected isMetricSelected(metric: MetricType): boolean {
    return this.selectedMetrics().includes(metric);
  }

  protected toggleMetric(metric: MetricType): void {
    const current = this.selectedMetrics();
    this.selectedMetrics.set(
      current.includes(metric) ? current.filter((candidate) => candidate !== metric) : [...current, metric]
    );
  }

  protected compare(): void {
    if (!this.canCompare()) {
      this.toastService.error('Pick at least two batches and one metric.');
      return;
    }

    this.comparing.set(true);
    this.analyticsService.compare(this.selectedIds(), this.selectedMetrics()).subscribe({
      next: (comparison) => {
        this.comparison.set(comparison);
        this.comparing.set(false);
      },
      error: () => this.comparing.set(false)
    });
  }

  protected clear(): void {
    this.selectedIds.set([]);
    this.comparison.set(null);
  }

  /**
   * @returns the chart for [metric], or `null` when no selected batch has enough readings for it.
   */
  private buildOption(comparison: BatchComparison, metric: MetricType): EChartsOption | null {
    const lines = comparison.batches
      .map((batch, index) => ({
        batch,
        index,
        series: batch.series.find((candidate) => candidate.metric === metric)
      }))
      .filter((line) => (line.series?.points.length ?? 0) > 1);

    if (lines.length === 0) {
      return null;
    }

    const maxDay = Math.max(
      ...lines.flatMap((line) => line.series!.points.map((point) => point.dayOffset))
    );
    const dayAxis = Array.from({ length: maxDay + 1 }, (_, day) => day);

    return {
      color: CHART_PALETTE,
      grid: { left: 48, right: 16, top: 28, bottom: 56 },
      tooltip: { trigger: 'axis', confine: true },
      legend: {
        bottom: 0,
        textStyle: { color: CHART_TEXT_COLOR, fontSize: 11 },
        itemWidth: 12,
        itemHeight: 8
      },
      xAxis: {
        type: 'category',
        name: 'day',
        nameLocation: 'middle',
        nameGap: 24,
        nameTextStyle: { color: CHART_TEXT_COLOR, fontSize: 10 },
        data: dayAxis.map((day) => String(day)),
        axisLine: { lineStyle: { color: CHART_LINE_COLOR } },
        axisLabel: { color: CHART_TEXT_COLOR, fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        scale: true,
        name: lines[0].series!.unit,
        nameTextStyle: { color: CHART_TEXT_COLOR, fontSize: 10 },
        splitLine: { lineStyle: { color: CHART_LINE_COLOR } },
        axisLabel: { color: CHART_TEXT_COLOR, fontSize: 10 }
      },
      series: lines.map((line) => {
        // Align on the day axis; gaps stay null so ECharts breaks the line instead of inventing data.
        const byDay = new Map(line.series!.points.map((point) => [point.dayOffset, point.value]));

        return {
          type: 'line' as const,
          name: `${line.batch.code} ${line.batch.name}`,
          smooth: true,
          connectNulls: true,
          showSymbol: false,
          lineStyle: { width: 2 },
          data: dayAxis.map((day) => byDay.get(day) ?? null)
        };
      })
    };
  }
}
