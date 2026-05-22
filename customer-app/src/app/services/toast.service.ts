import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
  removing?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  show(type: ToastType, title: string, message?: string, duration = 4000): void {
    const id = Math.random().toString(36).slice(2);
    const toast: Toast = { id, type, title, message, duration };
    this.toasts.update(t => [...t, toast]);

    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
  }

  success(title: string, message?: string, duration?: number): void {
    this.show('success', title, message, duration);
  }

  error(title: string, message?: string, duration?: number): void {
    this.show('error', title, message, duration ?? 6000);
  }

  warning(title: string, message?: string, duration?: number): void {
    this.show('warning', title, message, duration);
  }

  info(title: string, message?: string, duration?: number): void {
    this.show('info', title, message, duration);
  }

  dismiss(id: string): void {
    // Mark as removing for animation
    this.toasts.update(t => t.map(toast =>
      toast.id === id ? { ...toast, removing: true } : toast
    ));
    // Remove after animation
    setTimeout(() => {
      this.toasts.update(t => t.filter(toast => toast.id !== id));
    }, 350);
  }
}
