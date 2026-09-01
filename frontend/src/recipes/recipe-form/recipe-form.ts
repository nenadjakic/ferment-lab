import { ChangeDetectionStrategy, Component, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FermentTypeService } from '../../app/core/api/ferment-type.service';
import { RecipeService } from '../../app/core/api/recipe.service';
import {
  Difficulty,
  FermentType,
  RecipeAddRequest,
  RecipeIngredientRequest,
  RecipeStepRequest,
  StagePhase
} from '../../app/core/api/models';
import { DIFFICULTIES, STAGE_PHASES, UNITS, humanise } from '../../shared/labels';
import { ToastService } from '../../shared/toast.service';

/**
 * Modal for creating a recipe, with its ingredient list and step by step method.
 *
 * Ordering follows the row order, and the recipe's total duration is summed from the step durations
 * by the backend, so it is shown here as a read-only running total rather than an input.
 */
@Component({
  selector: 'app-recipe-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  templateUrl: './recipe-form.html'
})
export class RecipeFormComponent {
  readonly closed = output<void>();
  readonly created = output<string>();

  private readonly recipeService = inject(RecipeService);
  private readonly fermentTypeService = inject(FermentTypeService);
  private readonly toastService = inject(ToastService);

  protected readonly fermentTypes = signal<FermentType[]>([]);
  protected readonly saving = signal(false);

  protected readonly difficulties = DIFFICULTIES;
  protected readonly phases = STAGE_PHASES;
  protected readonly units = UNITS;
  protected readonly humanise = humanise;

  protected name = '';
  protected fermentTypeId = '';
  protected difficulty: Difficulty = 'BEGINNER';
  protected summary = '';
  protected yieldAmount: number | null = null;
  protected yieldUnit = 'L';
  protected salinityPercent: number | null = null;
  protected hydrationPercent: number | null = null;
  protected source = '';
  protected featured = false;

  /**
   * Row models are mutated in place by `ngModel`, so the arrays are replaced rather than mutated
   * whenever a row is added or removed - that is what makes the template re-render.
   */
  protected readonly ingredients = signal<RecipeIngredientRequest[]>([
    RecipeFormComponent.blankIngredient(),
    RecipeFormComponent.blankIngredient(),
    RecipeFormComponent.blankIngredient()
  ]);

  protected readonly steps = signal<RecipeStepRequest[]>([RecipeFormComponent.blankStep()]);

  constructor() {
    this.fermentTypeService.findAll().subscribe((types) => this.fermentTypes.set(types));
  }

  protected addIngredient(): void {
    this.ingredients.update((rows) => [...rows, RecipeFormComponent.blankIngredient()]);
  }

  protected removeIngredient(index: number): void {
    this.ingredients.update((rows) => rows.filter((_, i) => i !== index));
  }

  protected addStep(): void {
    this.steps.update((rows) => [...rows, RecipeFormComponent.blankStep()]);
  }

  protected removeStep(index: number): void {
    this.steps.update((rows) => rows.filter((_, i) => i !== index));
  }

  /**
   * Running total of the step durations.
   *
   * A method rather than a `computed`, because the durations live on row objects edited through
   * `ngModel` and a computed signal would not see those writes.
   */
  protected totalDurationDays(): number {
    return this.steps().reduce((total, step) => total + (Number(step.durationDays) || 0), 0);
  }

  protected submit(): void {
    const steps = this.steps()
      .filter((step) => step.title.trim() && step.instruction.trim())
      .map((step) => ({
        title: step.title.trim(),
        instruction: step.instruction.trim(),
        phase: step.phase,
        durationDays: Number(step.durationDays) || 0
      }));

    if (!this.name.trim() || !this.fermentTypeId) {
      this.toastService.error('A name and a ferment style are required.');
      return;
    }
    if (steps.length === 0) {
      this.toastService.error('Add at least one step with a title and an instruction.');
      return;
    }

    const request: RecipeAddRequest = {
      name: this.name.trim(),
      fermentTypeId: this.fermentTypeId,
      difficulty: this.difficulty,
      summary: this.summary.trim() || null,
      yieldAmount: this.yieldAmount,
      yieldUnit: this.yieldAmount === null ? null : this.yieldUnit,
      salinityPercent: this.salinityPercent,
      hydrationPercent: this.hydrationPercent,
      source: this.source.trim() || null,
      featured: this.featured,

      // Blank rows are dropped rather than rejected, so the starter rows can just be left empty.
      ingredients: this.ingredients()
        .filter((ingredient) => ingredient.name.trim())
        .map((ingredient) => ({
          name: ingredient.name.trim(),
          quantity: ingredient.quantity,
          unit: ingredient.quantity === null ? null : ingredient.unit,
          note: ingredient.note?.trim() || null
        })),
      steps
    };

    this.saving.set(true);
    this.recipeService.add(request).subscribe({
      next: (detail) => {
        this.saving.set(false);
        this.toastService.success(`Recipe "${detail.recipe.name}" created.`);
        this.created.emit(detail.recipe.id);
      },
      error: () => this.saving.set(false)
    });
  }

  private static blankIngredient(): RecipeIngredientRequest {
    return { name: '', quantity: null, unit: 'g', note: null };
  }

  private static blankStep(): RecipeStepRequest {
    return { title: '', instruction: '', phase: 'PRIMARY' as StagePhase, durationDays: 0 };
  }
}
