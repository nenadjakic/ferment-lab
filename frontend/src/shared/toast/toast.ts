import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { ToastService } from '../toast.service';

@Component({
  selector: 'app-toast',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pointer-events-none fixed bottom-4 right-4 z-50 flex w-80 flex-col gap-2"
         aria-live="polite"
         aria-atomic="false">
      @for (toast of toasts(); track toast.id) {
        <div class="pointer-events-auto flex items-start gap-3 rounded-lg border px-4 py-3 text-sm shadow-lg"
             [class]="styleFor(toast.kind)">
          <span class="grow">{{ toast.message }}</span>
          <button type="button"
                  class="shrink-0 text-lg leading-none opacity-70 hover:opacity-100"
                  aria-label="Dismiss notification"
                  (click)="toastService.dismiss(toast.id)">&times;</button>
        </div>
      }
    </div>
  `
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);
  protected readonly toasts = this.toastService.toasts;

  protected styleFor(kind: string): string {
    switch (kind) {
      case 'success':
        return 'border-emerald-800 bg-emerald-950 text-emerald-100';
      case 'error':
        return 'border-red-800 bg-red-950 text-red-100';
      default:
        return 'border-stone-700 bg-stone-900 text-stone-100';
    }
  }
}
