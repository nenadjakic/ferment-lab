import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AlertService } from '../app/core/api/alert.service';
import { Alert } from '../app/core/api/models';
import { ALERT_ICON, SEVERITY_CLASS, humanise } from '../shared/labels';
import { ToastService } from '../shared/toast.service';

@Component({
  selector: 'app-alerts',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  templateUrl: './alerts.html'
})
export class AlertsComponent {
  private readonly alertService = inject(AlertService);
  private readonly toastService = inject(ToastService);

  protected readonly alerts = signal<Alert[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly showAcknowledged = signal(false);

  protected readonly severityClass = SEVERITY_CLASS;
  protected readonly alertIcon = ALERT_ICON;
  protected readonly humanise = humanise;

  protected readonly openCount = computed(
    () => this.alerts().filter((alert) => !alert.acknowledgedAt).length
  );

  constructor() {
    this.load();
  }

  protected toggleAcknowledged(): void {
    this.showAcknowledged.update((show) => !show);
    this.load();
  }

  protected acknowledge(alert: Alert): void {
    this.busy.set(true);
    this.alertService.acknowledge(alert.id).subscribe({
      next: () => {
        this.toastService.success('Alert dismissed.');
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected acknowledgeAll(): void {
    this.busy.set(true);
    this.alertService.acknowledgeAll().subscribe({
      next: (result) => {
        this.toastService.success(`${result.acknowledged} alert(s) dismissed.`);
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  protected remove(alert: Alert): void {
    this.busy.set(true);
    this.alertService.delete(alert.id).subscribe({
      next: () => {
        this.toastService.success('Alert deleted.');
        this.load();
      },
      error: () => this.busy.set(false)
    });
  }

  private load(): void {
    this.loading.set(true);
    this.alertService.findAll(this.showAcknowledged()).subscribe({
      next: (alerts) => {
        this.alerts.set(alerts);
        this.loading.set(false);
        this.busy.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.busy.set(false);
      }
    });
  }
}
