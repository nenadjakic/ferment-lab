import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { BatchService } from '../../app/core/api/batch.service';
import { CultureService } from '../../app/core/api/culture.service';
import { FermentTypeService } from '../../app/core/api/ferment-type.service';
import { RecipeService } from '../../app/core/api/recipe.service';
import { BatchAddRequest, Culture, FermentType, Recipe } from '../../app/core/api/models';
import { ToastService } from '../../shared/toast.service';

/**
 * Modal for starting a batch.
 *
 * Picking a recipe narrows the culture list to matching styles and pre-fills the ferment style, since
 * the backend rejects a recipe that does not match the style.
 */
@Component({
  selector: 'app-batch-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  templateUrl: './batch-form.html'
})
export class BatchFormComponent {
  /** Pre-selects a recipe, used when starting a batch straight from the recipe page. */
  readonly initialRecipeId = input<string | null>(null);

  readonly closed = output<void>();
  readonly created = output<string>();

  private readonly batchService = inject(BatchService);
  private readonly fermentTypeService = inject(FermentTypeService);
  private readonly recipeService = inject(RecipeService);
  private readonly cultureService = inject(CultureService);
  private readonly toastService = inject(ToastService);

  protected readonly fermentTypes = signal<FermentType[]>([]);
  protected readonly recipes = signal<Recipe[]>([]);
  protected readonly cultures = signal<Culture[]>([]);
  protected readonly saving = signal(false);

  protected name = '';
  protected fermentTypeId = '';
  protected recipeId = '';
  protected cultureId = '';
  protected startedOn = new Date().toISOString().slice(0, 10);
  protected container = '';
  protected batchSize: number | null = null;
  protected batchSizeUnit = 'L';
  protected location = '';
  protected notes = '';

  /**
   * Only recipes for the chosen style, so an invalid combination cannot be submitted.
   *
   * A method rather than a computed signal, because the selected style lives in an `ngModel` field
   * rather than a signal.
   */
  protected selectableRecipes(): Recipe[] {
    return this.fermentTypeId
      ? this.recipes().filter((recipe) => recipe.fermentType.id === this.fermentTypeId)
      : this.recipes();
  }

  /** Only live cultures for the chosen style. */
  protected selectableCultures(): Culture[] {
    return this.cultures().filter(
      (culture) => culture.active && (!this.fermentTypeId || culture.fermentType.id === this.fermentTypeId)
    );
  }

  constructor() {
    this.fermentTypeService.findAll().subscribe((types) => this.fermentTypes.set(types));
    this.cultureService.findAll(true).subscribe((cultures) => this.cultures.set(cultures));

    this.recipeService.findAll().subscribe((recipes) => {
      this.recipes.set(recipes);

      const preselected = this.initialRecipeId();
      if (preselected) {
        const recipe = recipes.find((candidate) => candidate.id === preselected);
        if (recipe) {
          this.fermentTypeId = recipe.fermentType.id;
          this.recipeId = recipe.id;
          this.name = recipe.name;
        }
      }
    });
  }

  /** Changing the style invalidates a recipe or culture picked for a different one. */
  protected onFermentTypeChange(): void {
    const recipe = this.recipes().find((candidate) => candidate.id === this.recipeId);
    if (recipe && recipe.fermentType.id !== this.fermentTypeId) {
      this.recipeId = '';
    }

    const culture = this.cultures().find((candidate) => candidate.id === this.cultureId);
    if (culture && culture.fermentType.id !== this.fermentTypeId) {
      this.cultureId = '';
    }
  }

  /** Choosing a recipe implies its style, and seeds the batch name on first pick. */
  protected onRecipeChange(): void {
    const recipe = this.recipes().find((candidate) => candidate.id === this.recipeId);
    if (!recipe) {
      return;
    }

    this.fermentTypeId = recipe.fermentType.id;
    if (!this.name.trim()) {
      this.name = recipe.name;
    }
    if (this.batchSize === null && recipe.yieldAmount) {
      this.batchSize = recipe.yieldAmount;
      this.batchSizeUnit = recipe.yieldUnit ?? 'L';
    }
  }

  protected submit(): void {
    if (!this.name.trim() || !this.fermentTypeId) {
      this.toastService.error('A name and a ferment style are required.');
      return;
    }

    const request: BatchAddRequest = {
      name: this.name.trim(),
      fermentTypeId: this.fermentTypeId,
      recipeId: this.recipeId || null,
      cultureId: this.cultureId || null,
      startedOn: this.startedOn || null,
      container: this.container.trim() || null,
      batchSize: this.batchSize,
      batchSizeUnit: this.batchSizeUnit || null,
      location: this.location.trim() || null,
      notes: this.notes.trim() || null
    };

    this.saving.set(true);
    this.batchService.add(request).subscribe({
      next: (detail) => {
        this.saving.set(false);
        this.toastService.success(`Batch ${detail.batch.code} started.`);
        this.created.emit(detail.batch.id);
      },
      error: () => this.saving.set(false)
    });
  }
}
