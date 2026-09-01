import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { FermentTypeService } from '../app/core/api/ferment-type.service';
import { RecipeService } from '../app/core/api/recipe.service';
import { FermentType, Recipe } from '../app/core/api/models';
import { DIFFICULTY_CLASS, accentBarClass, humanise } from '../shared/labels';
import { RecipeFormComponent } from './recipe-form/recipe-form';

@Component({
  selector: 'app-recipes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, RecipeFormComponent],
  templateUrl: './recipes.html'
})
export class RecipesComponent {
  private readonly recipeService = inject(RecipeService);
  private readonly fermentTypeService = inject(FermentTypeService);
  private readonly router = inject(Router);

  protected readonly recipes = signal<Recipe[]>([]);
  protected readonly fermentTypes = signal<FermentType[]>([]);
  protected readonly loading = signal(true);
  protected readonly formOpen = signal(false);

  protected readonly fermentTypeId = signal('');
  protected readonly featuredOnly = signal(false);

  protected readonly humanise = humanise;
  protected readonly accentBarClass = accentBarClass;
  protected readonly difficultyClass = DIFFICULTY_CLASS;

  protected readonly visible = computed(() => {
    const styleId = this.fermentTypeId();
    const featured = this.featuredOnly();

    return this.recipes().filter(
      (recipe) => (!styleId || recipe.fermentType.id === styleId) && (!featured || recipe.featured)
    );
  });

  constructor() {
    this.fermentTypeService.findAll().subscribe((types) => this.fermentTypes.set(types));
    this.load();
  }

  protected onCreated(recipeId: string): void {
    this.formOpen.set(false);
    void this.router.navigate(['/recipes', recipeId]);
  }

  private load(): void {
    this.loading.set(true);
    this.recipeService.findAll().subscribe({
      next: (recipes) => {
        this.recipes.set(recipes);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
