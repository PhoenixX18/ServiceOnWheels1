import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

const passwordsMatchValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password')?.value as string | undefined;
  const confirm = group.get('confirmPassword')?.value as string | undefined;
  if (!password || !confirm) {
    return null;
  }
  return password === confirm ? null : { passwordMismatch: true };
};

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPasswordPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly tokenError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly resetSuccess = signal(false);
  readonly showPassword = signal(false);
  readonly showConfirmPassword = signal(false);

  private token = '';

  readonly form = this.fb.nonNullable.group(
    {
      password: ['', [
        Validators.required, 
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/)
      ]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatchValidator },
  );

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.loading.set(false);
      this.tokenError.set('No reset token provided. Please use the link from your email.');
      return;
    }

    this.auth.validateResetToken(this.token).subscribe({
      next: () => {
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.loading.set(false);
        const msg = err instanceof HttpErrorResponse
          ? (err.error as { message?: string })?.message ?? 'Invalid or expired reset link.'
          : 'Something went wrong';
        this.tokenError.set(msg);
      },
    });
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword.update(v => !v);
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const { password } = this.form.getRawValue();
    this.auth.resetPassword(this.token, password).subscribe({
      next: () => {
        this.submitting.set(false);
        this.resetSuccess.set(true);
        this.toast.success('Password reset!', 'Please sign in with your new password.');
        setTimeout(() => void this.router.navigateByUrl('/login'), 3000);
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        const msg = err instanceof HttpErrorResponse
          ? (err.error as { message?: string })?.message ?? 'Reset failed'
          : 'Something went wrong';
        this.errorMessage.set(msg);
        this.toast.error('Reset failed', msg);
      },
    });
  }
}
