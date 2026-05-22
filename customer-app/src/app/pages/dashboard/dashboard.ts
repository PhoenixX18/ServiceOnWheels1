import { DatePipe, isPlatformBrowser, SlicePipe } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ServiceRequestService } from '../../services/service-request.service';
import type { ServiceRequestResponse } from '../../models/service-request.models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe, SlicePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly api = inject(ServiceRequestService);

  readonly email = signal<string | null>(null);
  readonly role = signal<string | null>(null);
  readonly userName = signal<string>('there');
  readonly requests = signal<ServiceRequestResponse[]>([]);
  readonly loading = signal(true);
  readonly greeting = signal('Hello');

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const claims = decodeJwtPayload(this.auth.getToken());
      this.email.set(claims?.sub ?? null);
      this.role.set((claims?.['role'] as string | undefined) ?? null);

      // Derive first name from email
      const emailVal = claims?.sub ?? '';
      const namePart = emailVal.split('@')[0] ?? '';
      const formatted = namePart.charAt(0).toUpperCase() + namePart.slice(1).replace(/[._]/g, ' ');
      this.userName.set(formatted || 'there');

      // Set greeting by time
      const hour = new Date().getHours();
      if (hour < 12) this.greeting.set('Good morning');
      else if (hour < 17) this.greeting.set('Good afternoon');
      else this.greeting.set('Good evening');

      // Load requests for mini stats
      this.api.getMyRequests().subscribe({
        next: rows => {
          this.requests.set(rows);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    }
  }

  get totalRequests(): number { return this.requests().length; }
  get activeRequests(): number {
    return this.requests().filter(r => ['PENDING','ACCEPTED','IN_PROGRESS'].includes(r.status)).length;
  }
  get completedRequests(): number {
    return this.requests().filter(r => r.status === 'COMPLETED').length;
  }
  get recentRequests(): ServiceRequestResponse[] {
    return [...this.requests()]
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 3);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-pending';
      case 'ACCEPTED': return 'badge-accepted';
      case 'IN_PROGRESS': return 'badge-progress';
      case 'COMPLETED': return 'badge-completed';
      case 'CANCELLED': return 'badge-cancelled';
      case 'TOW_REQUIRED': return 'badge-tow';
      default: return 'badge-neutral';
    }
  }

  getDotClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'dot-pending';
      case 'ACCEPTED': return 'dot-accepted';
      case 'IN_PROGRESS': return 'dot-progress';
      case 'COMPLETED': return 'dot-completed';
      case 'CANCELLED': return 'dot-cancelled';
      default: return 'dot-cancelled';
    }
  }

  logout(): void {
    this.auth.logout();
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
  } catch {
    return null;
  }
}
