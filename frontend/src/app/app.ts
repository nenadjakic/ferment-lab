import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ToastComponent } from '../shared/toast/toast';
import { HeaderComponent } from './header/header';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, HeaderComponent, ToastComponent],
  template: `
    <div class="flex min-h-full flex-col">
      <app-header />

      <main class="mx-auto w-full max-w-7xl grow px-4 py-6 sm:px-6">
        <router-outlet />
      </main>

      <footer class="border-t border-stone-800 px-4 py-4 text-center text-xs text-stone-500 sm:px-6">
        FermentLab &middot; fermentation journal for home fermenters
      </footer>
    </div>

    <app-toast />
  `
})
export class App {}
