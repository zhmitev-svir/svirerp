import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/services/auth.service';

/**
 * Break-glass local admin login. Intentionally not linked from anywhere in
 * the nav or the main login page — this route is only meant to be known to
 * whoever holds the admin credentials, as a fallback if Google sign-in is
 * ever unavailable.
 */
@Component({
  selector: 'app-portal-access',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card mat-elevation-z2">
        <mat-card-header>
          <mat-card-title>Admin Access</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput formControlName="username" autocomplete="username" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="current-password" />
            </mat-form-field>

            @if (errorMessage()) {
              <p class="error">{{ errorMessage() }}</p>
            }

            <button mat-raised-button color="primary" type="submit"
                    class="full-width" [disabled]="form.invalid || submitting()">
              @if (submitting()) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                Sign in
              }
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
    }
    .login-card { width: 320px; padding: 8px; }
    .full-width { width: 100%; }
    .error { color: #b00020; margin: 0 0 12px; }
  `],
})
export class PortalAccessComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.form.getRawValue();
    // Spring's UsernamePasswordAuthenticationFilter reads credentials from
    // form-urlencoded request parameters, not a JSON body.
    const body = new HttpParams().set('username', username).set('password', password);

    this.http
      .post('/login/local', body, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      .subscribe({
        next: () => {
          this.authService.fetchMe().subscribe(() => this.router.navigateByUrl('/'));
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          this.errorMessage.set(
            err.status === 429
              ? 'Too many failed attempts — try again later.'
              : 'Invalid username or password.',
          );
        },
      });
  }
}
