import { ChangeDetectionStrategy, Component, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { BatchService } from '../app/core/api/batch.service';
import { FermentTypeService } from '../app/core/api/ferment-type.service';
import { Batch, BatchStatus, FermentType, Page } from '../app/core/api/models';
import { BATCH_STATUSES, humanise } from '../shared/labels';
import { BatchCardComponent } from './batch-card/batch-card';
import { BatchFormComponent } from './batch-form/batch-form';

@Component({
  selector: 'app-batches',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, BatchCardComponent, BatchFormComponent],
  templateUrl: './batches.html'
})
export class BatchesComponent {
  private readonly batchService = inject(BatchService);
  private readonly fermentTypeService = inject(FermentTypeService);
  private readonly router = inject(Router);

  protected readonly page = signal<Page<Batch> | null>(null);
  protected readonly fermentTypes = signal<FermentType[]>([]);
  protected readonly loading = signal(true);
  protected readonly formOpen = signal(false);

  protected readonly search = signal('');
  protected readonly fermentTypeId = signal('');
  protected readonly status = signal<BatchStatus | ''>('');
  protected readonly pageIndex = signal(0);

  protected readonly statuses = BATCH_STATUSES;
  protected readonly humanise = humanise;

  constructor() {
    this.fermentTypeService.findAll().subscribe((types) => this.fermentTypes.set(types));

    // Any filter change reloads; the page index is reset by the filter setters.
    effect(() => {
      const filter = {
        search: this.search(),
        fermentTypeId: this.fermentTypeId(),
        status: this.status(),
        page: this.pageIndex()
      };

      this.loading.set(true);
      this.batchService
        .search({
          search: filter.search || undefined,
          fermentTypeId: filter.fermentTypeId || undefined,
          status: filter.status || undefined,
          page: filter.page,
          size: 12
        })
        .subscribe({
          next: (page) => {
            this.page.set(page);
            this.loading.set(false);
          },
          error: () => this.loading.set(false)
        });
    });
  }

  protected onSearchChange(value: string): void {
    this.pageIndex.set(0);
    this.search.set(value);
  }

  protected onFermentTypeChange(value: string): void {
    this.pageIndex.set(0);
    this.fermentTypeId.set(value);
  }

  protected onStatusChange(value: string): void {
    this.pageIndex.set(0);
    this.status.set(value as BatchStatus | '');
  }

  protected clearFilters(): void {
    this.pageIndex.set(0);
    this.search.set('');
    this.fermentTypeId.set('');
    this.status.set('');
  }

  protected goToPage(index: number): void {
    this.pageIndex.set(index);
  }

  protected onCreated(batchId: string): void {
    this.formOpen.set(false);
    void this.router.navigate(['/batches', batchId]);
  }
}
