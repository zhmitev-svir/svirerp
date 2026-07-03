import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthenticatedUser } from '../models/auth.model';
import { ENVIRONMENT } from '../tokens/environment.token';

/**
 * Tracks client-side auth state. currentUser is populated by fetchMe(),
 * called once at app bootstrap (see app.config.ts) and again after either
 * login flow completes. There is no "check if logged in" endpoint distinct
 * from /api/auth/me — since /api/** requires authentication, a 401 from
 * that call already means "not logged in".
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  readonly currentUser = signal<AuthenticatedUser | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  fetchMe(): Observable<AuthenticatedUser> {
    return this.http
      .get<AuthenticatedUser>(`${this.env.apiUrl}/auth/me`)
      .pipe(tap(user => this.currentUser.set(user)));
  }

  logout(): Observable<void> {
    return this.http.post<void>('/logout', {}).pipe(tap(() => this.currentUser.set(null)));
  }
}
