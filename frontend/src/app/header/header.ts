import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AlertService } from '../core/api/alert.service';

interface NavItem {
  readonly path: string;
  readonly label: string;
}

@Component({
  selector: 'app-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html'
})
export class HeaderComponent {
  private readonly alertService = inject(AlertService);

  protected readonly navItems: readonly NavItem[] = [
    { path: '/dashboard', label: 'Dashboard' },
    { path: '/batches', label: 'Batches' },
    { path: '/compare', label: 'Compare' },
    { path: '/cultures', label: 'Cultures' },
    { path: '/recipes', label: 'Recipes' }
  ];

  protected readonly openAlertCount = signal(0);
  protected readonly menuOpen = signal(false);

  constructor() {
    // The count is a nice-to-have in the chrome, so a failure here must not surface an error.
    this.alertService.findAll().subscribe({
      next: (alerts) => this.openAlertCount.set(alerts.length),
      error: () => this.openAlertCount.set(0)
    });
  }

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
}
