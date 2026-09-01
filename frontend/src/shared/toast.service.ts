import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

/**
 * Transient feedback messages. Toasts auto-dismiss after a few seconds and can be dismissed early.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private static readonly DISMISS_AFTER_MS = 4500;

  private nextId = 1;
  private readonly items = signal<Toast[]>([]);

  readonly toasts = this.items.asReadonly();

  success(message: string): void {
    this.push('success', message);
  }

  error(message: string): void {
    this.push('error', message);
  }

  info(message: string): void {
    this.push('info', message);
  }

  dismiss(id: number): void {
    this.items.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }

  private push(kind: ToastKind, message: string): void {
    const id = this.nextId++;
    this.items.update((toasts) => [...toasts, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), ToastService.DISMISS_AFTER_MS);
  }
}
