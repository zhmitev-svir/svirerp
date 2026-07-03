import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="login-container">
      <mat-card class="login-card mat-elevation-z2">
        <mat-card-header>
          <mat-card-title>SVIR ERP</mat-card-title>
          <mat-card-subtitle>Sign in to continue</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <!--
            A plain anchor, not a routerLink or click handler — this must be a
            real browser navigation so Google's consent screen and the
            session-cookie-setting redirect back both work correctly.
          -->
          <a mat-raised-button color="primary" href="/oauth2/authorization/google" class="google-btn">
            <mat-icon>login</mat-icon>
            Sign in with Google
          </a>
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
    .login-card { width: 320px; text-align: center; padding: 8px; }
    .google-btn { width: 100%; margin-top: 16px; }
  `],
})
export class LoginComponent {}
