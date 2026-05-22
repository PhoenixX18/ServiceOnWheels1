import { isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-profile',
  imports: [RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfilePage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly email = signal<string | null>(null);
  readonly role = signal<string | null>(null);
  readonly userName = signal<string>('User');
  readonly initials = signal<string>('U');
  readonly memberSince = signal<string>('');

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const claims = decodeJwtPayload(this.auth.getToken());
      const emailVal = claims?.sub ?? '';
      this.email.set(emailVal);
      this.role.set((claims?.['role'] as string | undefined) ?? null);

      const namePart = emailVal.split('@')[0] ?? 'User';
      const formatted = namePart.charAt(0).toUpperCase() + namePart.slice(1).replace(/[._]/g, ' ');
      this.userName.set(formatted);
      this.initials.set(formatted.substring(0, 2).toUpperCase());
      this.memberSince.set(new Date().getFullYear().toString());
    }
  }

  logout(): void {
    this.auth.logout();
    this.toast.success('Signed out', 'Come back soon!');
    void this.router.navigateByUrl('/login');
  }
}

function decodeJwtPayload(token: string | null): { sub?: string; role?: string } | null {
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    return JSON.parse(atob(padded)) as { sub?: string; role?: string };
  } catch { return null; }
}
