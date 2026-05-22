import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AUTH_TOKEN_STORAGE_KEY } from '../models/auth-storage';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  
  // Expose reactive signal for navbar
  readonly isAuthenticated = signal<boolean>(this.hasToken());

  private readonly apiBase = 'http://localhost:8081';

  register(body: RegisterRequest): Observable<AuthResponse> {
    const payload: RegisterRequest = {
      ...body,
      role: body.role ?? 'USER',
    };
    return this.http
      .post<AuthResponse>(`${this.apiBase}/api/auth/register`, payload)
      .pipe(tap((res) => this.saveToken(res.token)));
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/api/auth/login`, body)
      .pipe(tap((res) => this.saveToken(res.token)));
  }

  logout(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
    this.isAuthenticated.set(false);
  }

  saveToken(token: string): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
    this.isAuthenticated.set(true);
  }

  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    return localStorage.getItem(AUTH_TOKEN_STORAGE_KEY);
  }

  private hasToken(): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return false;
    }
    return !!localStorage.getItem(AUTH_TOKEN_STORAGE_KEY);
  }

  isLoggedIn(): boolean {
    return this.isAuthenticated();
  }
}
