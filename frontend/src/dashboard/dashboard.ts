import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AlertService } from '../app/core/api/alert.service';
import { AnalyticsService } from '../app/core/api/analytics.service';
import { DashboardSummary } from '../app/core/api/models';
import { BatchCardComponent } from '../batches/batch-card/batch-card';
import { ChartComponent } from '../shared/chart/chart';
import { ALERT_ICON, EVENT_ICON, SEVERITY_CLASS, humanise, relativeDays } from '../shared/labels';
import { CHART_LINE_COLOR, CHART_PALETTE, CHART_TEXT_COLOR, EChartsOption } from '../shared/chart/echarts.setup';
import { ToastService } from '../shared/toast.service';

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, DecimalPipe, ChartComponent, BatchCardComponent],
  templateUrl: './dashboard.html'
})
export class DashboardComponent {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly alertService = inject(AlertService);
  private readonly toastService = inject(ToastService);

  protected readonly summary = signal<DashboardSummary | null>(null);
  protected readonly loading = signal(true);

  protected readonly severityClass = SEVERITY_CLASS;
  protected readonly alertIcon = ALERT_ICON;
  protected readonly eventIcon = EVENT_ICON;
  protected readonly humanise = humanise;
  protected readonly relativeDays = relativeDays;

  /**
   * The next six milestones. Anything further out is noise on a dashboard.
   */
  protected readonly nextMilestones = computed(() => this.summary()?.upcomingMilestones.slice(0, 6) ?? []);

  /**
   * Donut of how the batch history splits across ferment styles.
   */
  protected readonly distributionOption = computed<EChartsOption>(() => {
    const data = this.summary()?.batchesPerFermentType ?? [];

    return {
      color: CHART_PALETTE,
      tooltip: { trigger: 'item', confine: true },
      legend: {
        bottom: 0,
        textStyle: { color: CHART_TEXT_COLOR, fontSize: 11 },
        itemWidth: 10,
        itemHeight: 10
      },
      series: [
        {
          type: 'pie',
          radius: ['48%', '72%'],
          center: ['50%', '43%'],
          avoidLabelOverlap: true,
          itemStyle: { borderColor: CHART_LINE_COLOR, borderWidth: 2 },
          label: { show: false },
          data: data.map((entry) => ({ name: entry.name, value: entry.value }))
        }
      ]
    };
  });

  constructor() {
    this.load();
  }

  protected acknowledge(id: string): void {
    this.alertService.acknowledge(id).subscribe(() => {
      this.toastService.success('Alert dismissed.');
      this.load();
    });
  }

  private load(): void {
    this.loading.set(true);
    this.analyticsService.dashboard().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
