import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard'
  },
  {
    path: 'dashboard',
    title: 'Dashboard · FermentLab',
    loadComponent: () => import('../dashboard/dashboard').then((m) => m.DashboardComponent)
  },
  {
    path: 'batches',
    title: 'Batches · FermentLab',
    loadComponent: () => import('../batches/batches').then((m) => m.BatchesComponent)
  },
  {
    path: 'batches/:id',
    title: 'Batch · FermentLab',
    loadComponent: () => import('../batch-detail/batch-detail').then((m) => m.BatchDetailComponent)
  },
  {
    path: 'compare',
    title: 'Compare · FermentLab',
    loadComponent: () => import('../compare/compare').then((m) => m.CompareComponent)
  },
  {
    path: 'cultures',
    title: 'Cultures · FermentLab',
    loadComponent: () => import('../cultures/cultures').then((m) => m.CulturesComponent)
  },
  {
    path: 'recipes',
    title: 'Recipes · FermentLab',
    loadComponent: () => import('../recipes/recipes').then((m) => m.RecipesComponent)
  },
  {
    path: 'recipes/:id',
    title: 'Recipe · FermentLab',
    loadComponent: () => import('../recipes/recipe-detail/recipe-detail').then((m) => m.RecipeDetailComponent)
  },
  {
    path: 'alerts',
    title: 'Alerts · FermentLab',
    loadComponent: () => import('../alerts/alerts').then((m) => m.AlertsComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
