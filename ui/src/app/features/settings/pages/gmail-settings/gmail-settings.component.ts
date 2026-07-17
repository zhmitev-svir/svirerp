import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

import { SettingsService } from '../../services/settings.service';
import { GmailSettingsService } from '../../services/gmail-settings.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { AppSetting } from '../../../../core/models/api.model';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

/**
 * Reads/writes the same gmail.* rows the generic Settings page can edit
 * (V31__create_gmail_settings.sql), but adds the pieces that page can't:
 * kicking off the Connect Gmail OAuth redirect, showing connection status
 * from gmail.sender-address, and a test-send action.
 */
@Component({
  selector: 'app-gmail-settings',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    PageHeaderComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Gmail"
        subtitle="Send app email through a connected Gmail account" />

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Credentials</h3>
          <p class="section-hint">
            Create an OAuth 2.0 Web application client in Google Cloud Console with redirect URI
            <code>{{ redirectUri }}</code> and the <code>gmail.send</code> scope, then paste its
            client ID and secret below.
          </p>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Client ID</mat-label>
            <input matInput [(ngModel)]="clientIdDraft" autocomplete="off" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Client Secret</mat-label>
            <input matInput type="password" [(ngModel)]="clientSecretDraft"
                   [placeholder]="clientSecretConfigured() ? '••••••••' : 'Not set'"
                   autocomplete="new-password" />
          </mat-form-field>

          <button mat-flat-button color="primary" [disabled]="savingCredentials()" (click)="saveCredentials()">
            @if (savingCredentials()) {
              <mat-progress-spinner diameter="20" mode="indeterminate" />
            } @else {
              Save Credentials
            }
          </button>
        </mat-card-content>
      </mat-card>

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Connection</h3>

          @if (senderAddress()) {
            <p class="connection-status connected">
              <mat-icon>check_circle</mat-icon>
              Connected as {{ senderAddress() }}
            </p>
          } @else {
            <p class="connection-status">
              <mat-icon>cancel</mat-icon>
              Not connected
            </p>
          }

          <button mat-stroked-button [disabled]="!credentialsConfigured() || connecting()" (click)="connect()">
            @if (connecting()) {
              <mat-progress-spinner diameter="20" mode="indeterminate" />
            } @else {
              {{ senderAddress() ? 'Reconnect Gmail' : 'Connect Gmail' }}
            }
          </button>
          @if (!credentialsConfigured()) {
            <p class="section-hint">Save a Client ID and Client Secret first.</p>
          }
        </mat-card-content>
      </mat-card>

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Test</h3>

          <div class="test-row">
            <mat-form-field appearance="outline" class="test-input">
              <mat-label>Send a test email to</mat-label>
              <input matInput type="email" [(ngModel)]="testRecipient" autocomplete="email" />
            </mat-form-field>

            <button mat-flat-button color="primary"
                    [disabled]="!senderAddress() || !testRecipient || sendingTest()"
                    (click)="sendTest()">
              @if (sendingTest()) {
                <mat-progress-spinner diameter="20" mode="indeterminate" />
              } @else {
                Send Test Email
              }
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .setting-card { margin-bottom: 12px; }
    .section-title { margin: 0 0 8px; }
    .section-hint { font-size: 0.85em; color: rgba(0,0,0,.6); }
    .full-width { width: 100%; }
    .connection-status { display: flex; align-items: center; gap: 8px; }
    .connection-status.connected { color: #2e7d32; }
    .test-row { display: flex; align-items: flex-start; gap: 12px; }
    .test-input { width: 320px; margin-bottom: -1.25em; }
  `],
})
export class GmailSettingsComponent implements OnInit {
  private settingsService = inject(SettingsService);
  private gmailSettingsService = inject(GmailSettingsService);
  private notifications = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly redirectUri = `${window.location.origin}/api/settings/gmail/callback`;

  private settings = signal<AppSetting[]>([]);
  clientIdDraft = '';
  clientSecretDraft = '';
  testRecipient = '';

  savingCredentials = signal(false);
  connecting = signal(false);
  sendingTest = signal(false);

  clientSecretConfigured = computed(() =>
    this.settings().find(s => s.key === 'gmail.oauth.client-secret')?.hasValue ?? false);

  credentialsConfigured = computed(() => {
    const clientId = this.settings().find(s => s.key === 'gmail.oauth.client-id');
    return !!clientId?.hasValue && this.clientSecretConfigured();
  });

  senderAddress = computed(() =>
    this.settings().find(s => s.key === 'gmail.sender-address')?.value ?? null);

  ngOnInit(): void {
    this.load();

    const params = this.route.snapshot.queryParamMap;
    if (params.get('gmail_connected')) {
      this.notifications.success('Gmail connected.');
      this.router.navigate([], { relativeTo: this.route, queryParams: {} });
    } else if (params.get('gmail_error')) {
      this.notifications.error(`Gmail connection failed: ${params.get('gmail_error')}`);
      this.router.navigate([], { relativeTo: this.route, queryParams: {} });
    }
  }

  saveCredentials(): void {
    this.savingCredentials.set(true);
    this.settingsService.update('gmail.oauth.client-id', this.clientIdDraft).subscribe({
      next: () => {
        const secret = this.clientSecretDraft;
        if (!secret) {
          this.savingCredentials.set(false);
          this.load();
          this.notifications.success('Gmail credentials saved.');
          return;
        }
        this.settingsService.update('gmail.oauth.client-secret', secret).subscribe({
          next: () => {
            this.savingCredentials.set(false);
            this.clientSecretDraft = '';
            this.load();
            this.notifications.success('Gmail credentials saved.');
          },
          error: () => this.savingCredentials.set(false),
        });
      },
      error: () => this.savingCredentials.set(false),
    });
  }

  connect(): void {
    this.connecting.set(true);
    this.gmailSettingsService.authorizeUrl().subscribe({
      next: ({ url }) => window.location.href = url,
      error: () => this.connecting.set(false),
    });
  }

  sendTest(): void {
    this.sendingTest.set(true);
    this.gmailSettingsService.testSend(this.testRecipient).subscribe({
      next: () => {
        this.sendingTest.set(false);
        this.notifications.success(`Test email sent to ${this.testRecipient}.`);
      },
      error: () => this.sendingTest.set(false),
    });
  }

  private load(): void {
    this.settingsService.list().subscribe(settings => {
      const gmailSettings = settings.filter(s => s.key.startsWith('gmail.'));
      this.settings.set(gmailSettings);
      this.clientIdDraft = gmailSettings.find(s => s.key === 'gmail.oauth.client-id')?.value ?? '';
    });
  }
}
