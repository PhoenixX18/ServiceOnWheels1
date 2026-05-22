import { Component, inject } from '@angular/core';
import { ToastService, Toast } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-container" aria-live="polite" aria-atomic="true">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="toast toast-{{ toast.type }}"
          [class.removing]="toast.removing"
          role="alert"
        >
          <!-- Progress bar -->
          <div
            class="toast-progress"
            [style.animation-duration]="(toast.duration ?? 4000) + 'ms'"
          ></div>

          <!-- Icon -->
          <div class="toast-icon">
            @switch (toast.type) {
              @case ('success') {
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="var(--status-completed)" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
                </svg>
              }
              @case ('error') {
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="var(--status-cancelled)" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              }
              @case ('warning') {
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="var(--status-pending)" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                </svg>
              }
              @default {
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="var(--status-accepted)" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
              }
            }
          </div>

          <!-- Body -->
          <div class="toast-body">
            <div class="toast-title">{{ toast.title }}</div>
            @if (toast.message) {
              <div class="toast-msg">{{ toast.message }}</div>
            }
          </div>

          <!-- Close -->
          <button class="toast-close" (click)="toastService.dismiss(toast.id)" aria-label="Dismiss">
            <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);
}
