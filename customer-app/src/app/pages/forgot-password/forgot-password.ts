import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPasswordPage {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly emailSent = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  onSubmit(): void {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const { email } = this.form.getRawValue();
    this.auth.forgotPassword(email).subscribe({
      next: (res: any) => {
        this.submitting.set(false);
        this.emailSent.set(true);
        const msg = res?.message ?? 'Reset instructions have been sent if the email exists.';
        this.toast.success('Email sent', msg);
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        const msg = err instanceof HttpErrorResponse
          ? (err.error as { message?: string })?.message ?? 'Request failed'
          : 'Something went wrong';
        this.errorMessage.set(msg);
        this.toast.error('Request failed', msg);
      },
    });
  }
}
