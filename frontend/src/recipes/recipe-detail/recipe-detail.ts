import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { RecipeService } from '../../app/core/api/recipe.service';
import { RecipeDetail } from '../../app/core/api/models';
import { PrintService } from '../../app/core/print.service';
import { BatchFormComponent } from '../../batches/batch-form/batch-form';
import { RecipeFormComponent } from '../recipe-form/recipe-form';
import { humanise } from '../../shared/labels';
import { ToastService } from '../../shared/toast.service';

@Component({
  selector: 'app-recipe-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BatchFormComponent, RecipeFormComponent],
  templateUrl: './recipe-detail.html'
})
export class RecipeDetailComponent implements OnInit {
  /** Bound from the `:id` route parameter through `withComponentInputBinding()`. */
  readonly id = input.required<string>();

  private readonly recipeService = inject(RecipeService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);
  private readonly printService = inject(PrintService);

  protected readonly detail = signal<RecipeDetail | null>(null);
  protected readonly loading = signal(true);
  protected readonly startOpen = signal(false);
  protected readonly editOpen = signal(false);
  protected readonly busy = signal(false);

  protected readonly humanise = humanise;

  // Required route inputs are only populated after construction, so the fetch waits for `ngOnInit`.
  ngOnInit(): void {
    this.load();
  }

  protected onBatchCreated(batchId: string): void {
    this.startOpen.set(false);
    void this.router.navigate(['/batches', batchId]);
  }

  /** After an edit the detail is re-fetched so the page reflects the new steps and duration. */
  protected onEdited(): void {
    this.editOpen.set(false);
    this.load();
  }

  protected deleteRecipe(): void {
    this.busy.set(true);
    this.recipeService.delete(this.id()).subscribe({
      next: () => {
        this.toastService.success('Recipe deleted.');
        void this.router.navigate(['/recipes']);
      },
      error: () => this.busy.set(false)
    });
  }

  private load(): void {
    this.loading.set(true);
    this.recipeService.findById(this.id()).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

    protected print(): void {
      const data = this.detail();

      if (!data) {
        return;
      }

      const ingredients = data.ingredients
        .map((ingredient) => `
          <tr>
            <td>${this.escapeHtml(ingredient.name)}</td>
            <td>${ingredient.quantity ?? ''} ${this.escapeHtml(ingredient.unit ?? '')}</td>
            <td>${this.escapeHtml(ingredient.note ?? '')}</td>
          </tr>
        `)
        .join('');

      const steps = data.steps
        .map((step) => `
          <div class="card">
            <h3>
              ${step.position}. ${this.escapeHtml(step.title)}
            </h3>
            <div>
              <span class="pill">${this.escapeHtml(this.humanise(step.phase))}</span>
              ${step.durationDays > 0
                ? `<span class="pill">${step.durationDays} days</span>`
                : ''}
            </div>
            <p>${this.escapeHtml(step.instruction)}</p>
          </div>
        `)
        .join('');

      const body = `
        <div class="facts">
          <div class="fact">
            <div class="fact-label">Ferment</div>
            <div class="fact-value">${this.escapeHtml(data.recipe.fermentType.name)}</div>
          </div>

          <div class="fact">
            <div class="fact-label">Difficulty</div>
            <div class="fact-value">${this.escapeHtml(this.humanise(data.recipe.difficulty))}</div>
          </div>

          <div class="fact">
            <div class="fact-label">Duration</div>
            <div class="fact-value">${data.recipe.totalDurationDays} days</div>
          </div>

          <div class="fact">
            <div class="fact-label">Yield</div>
            <div class="fact-value">
              ${data.recipe.yieldAmount ?? '—'}
              ${this.escapeHtml(data.recipe.yieldUnit ?? '')}
            </div>
          </div>
        </div>

        ${data.recipe.summary ? `
          <div class="card">
            <strong>Summary</strong>
            <p>${this.escapeHtml(data.recipe.summary)}</p>
          </div>
        ` : ''}

        <h2>Recipe parameters</h2>

        <table>
          <tbody>
            <tr>
              <th>Salinity</th>
              <td>${data.recipe.salinityPercent != null ? data.recipe.salinityPercent + '%' : '—'}</td>
            </tr>
            <tr>
              <th>Hydration</th>
              <td>${data.recipe.hydrationPercent != null ? data.recipe.hydrationPercent + '%' : '—'}</td>
            </tr>
            <tr>
              <th>Source</th>
              <td>${this.escapeHtml(data.recipe.source ?? '—')}</td>
            </tr>
            <tr>
              <th>Featured</th>
              <td>${data.recipe.featured ? 'Yes' : 'No'}</td>
            </tr>
          </tbody>
        </table>

        <h2>Ingredients</h2>

        <table>
          <thead>
            <tr>
              <th>Ingredient</th>
              <th>Quantity</th>
              <th>Note</th>
            </tr>
          </thead>
          <tbody>
            ${ingredients || `
              <tr>
                <td colspan="3" class="muted">No ingredients.</td>
              </tr>
            `}
          </tbody>
        </table>

        <h2>Method</h2>

        ${steps || '<p class="muted">No steps.</p>'}
      `;

      this.printService.print(
        data.recipe.name,
        `${data.recipe.fermentType.name} · Recipe`,
        body
      );
    }

    private escapeHtml(value: unknown): string {
      return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
    }
}
