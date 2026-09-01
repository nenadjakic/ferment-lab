import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { RecipeService } from '../../app/core/api/recipe.service';
import { RecipeDetail } from '../../app/core/api/models';
import { BatchFormComponent } from '../../batches/batch-form/batch-form';
import { humanise } from '../../shared/labels';

@Component({
  selector: 'app-recipe-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BatchFormComponent],
  templateUrl: './recipe-detail.html'
})
export class RecipeDetailComponent {
  /** Bound from the `:id` route parameter. */
  readonly id = input.required<string>();

  private readonly recipeService = inject(RecipeService);
  private readonly router = inject(Router);

  protected readonly detail = signal<RecipeDetail | null>(null);
  protected readonly loading = signal(true);
  protected readonly startOpen = signal(false);

  protected readonly humanise = humanise;

  constructor() {
    this.recipeService.findById(this.id()).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected onBatchCreated(batchId: string): void {
    this.startOpen.set(false);
    void this.router.navigate(['/batches', batchId]);
  }
}
