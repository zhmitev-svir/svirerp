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
import { CalendarSettingsService } from '../../services/calendar-settings.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { AppSetting } from '../../../../core/models/api.model';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

/**
 * Reads/writes the calendar.* AppSetting rows (V35__add_calendar_sync_to_events.sql),
 * plus the pieces the generic Settings page can't do: kicking off the Connect
 * Google Calendar OAuth redirect, connection status, and a read-only "can we
 * actually see these calendars" test. A fully independent connection from
 * Gmail's — see gmail-settings.component.ts for the sibling pattern this mirrors.
 */
@Component({
  selector: 'app-calendar-settings',
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
        title="Calendar"
        subtitle="Push events to the official and internal Google Calendars" />

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Credentials</h3>
          <p class="section-hint">
            Create an OAuth 2.0 Web application client in Google Cloud Console with redirect URI
            <code>{{ redirectUri }}</code> and the <code>calendar.events</code> scope, then paste its
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

          @if (connected()) {
            <p class="connection-status connected">
              <mat-icon>check_circle</mat-icon>
              Connected
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
              {{ connected() ? 'Reconnect Google Calendar' : 'Connect Google Calendar' }}
            }
          </button>
          @if (!credentialsConfigured()) {
            <p class="section-hint">Save a Client ID and Client Secret first.</p>
          }
        </mat-card-content>
      </mat-card>

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Calendars</h3>
          <p class="section-hint">
            The Google Calendar ID for each calendar (found under that calendar's Settings and
            Sharing page in Google Calendar) — the connected account must have write access to both.
          </p>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Official Calendar ID</mat-label>
            <input matInput [(ngModel)]="officialIdDraft" autocomplete="off" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Internal Calendar ID</mat-label>
            <input matInput [(ngModel)]="internalIdDraft" autocomplete="off" />
          </mat-form-field>

          <button mat-flat-button color="primary" [disabled]="savingCalendarIds()" (click)="saveCalendarIds()">
            @if (savingCalendarIds()) {
              <mat-progress-spinner diameter="20" mode="indeterminate" />
            } @else {
              Save Calendar IDs
            }
          </button>
        </mat-card-content>
      </mat-card>

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Test</h3>

          <button mat-flat-button color="primary" [disabled]="!connected() || testing()" (click)="testConnection()">
            @if (testing()) {
              <mat-progress-spinner diameter="20" mode="indeterminate" />
            } @else {
              Test Connection
            }
          </button>

          @if (testResults(); as results) {
            <div class="test-results">
              @for (entry of objectEntries(results); track entry[0]) {
                <p>{{ entry[0] }}: {{ entry[1] }}</p>
              }
            </div>
          }
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
    .test-results { margin-top: 12px; }
    .test-results p { margin: 0 0 4px; font-size: 0.9em; }
  `],
})
export class CalendarSettingsComponent implements OnInit {
  private settingsService = inject(SettingsService);
  private calendarSettingsService = inject(CalendarSettingsService);
  private notifications = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly redirectUri = `${window.location.origin}/api/settings/calendar/callback`;
  readonly objectEntries = Object.entries;

  private settings = signal<AppSetting[]>([]);
  clientIdDraft = '';
  clientSecretDraft = '';
  officialIdDraft = '';
  internalIdDraft = '';

  savingCredentials = signal(false);
  savingCalendarIds = signal(false);
  connecting = signal(false);
  testing = signal(false);
  testResults = signal<Record<string, string> | null>(null);

  clientSecretConfigured = computed(() =>
    this.settings().find(s => s.key === 'calendar.oauth.client-secret')?.hasValue ?? false);

  credentialsConfigured = computed(() => {
    const clientId = this.settings().find(s => s.key === 'calendar.oauth.client-id');
    return !!clientId?.hasValue && this.clientSecretConfigured();
  });

  connected = computed(() =>
    this.settings().find(s => s.key === 'calendar.oauth.refresh-token')?.hasValue ?? false);

  ngOnInit(): void {
    this.load();

    const params = this.route.snapshot.queryParamMap;
    if (params.get('calendar_connected')) {
      this.notifications.success('Google Calendar connected.');
      this.router.navigate([], { relativeTo: this.route, queryParams: {} });
    } else if (params.get('calendar_error')) {
      this.notifications.error(`Calendar connection failed: ${params.get('calendar_error')}`);
      this.router.navigate([], { relativeTo: this.route, queryParams: {} });
    }
  }

  saveCredentials(): void {
    this.savingCredentials.set(true);
    this.settingsService.update('calendar.oauth.client-id', this.clientIdDraft).subscribe({
      next: () => {
        const secret = this.clientSecretDraft;
        if (!secret) {
          this.savingCredentials.set(false);
          this.load();
          this.notifications.success('Calendar credentials saved.');
          return;
        }
        this.settingsService.update('calendar.oauth.client-secret', secret).subscribe({
          next: () => {
            this.savingCredentials.set(false);
            this.clientSecretDraft = '';
            this.load();
            this.notifications.success('Calendar credentials saved.');
          },
          error: () => this.savingCredentials.set(false),
        });
      },
      error: () => this.savingCredentials.set(false),
    });
  }

  saveCalendarIds(): void {
    this.savingCalendarIds.set(true);
    this.settingsService.update('calendar.official.id', this.officialIdDraft).subscribe({
      next: () => {
        this.settingsService.update('calendar.internal.id', this.internalIdDraft).subscribe({
          next: () => {
            this.savingCalendarIds.set(false);
            this.load();
            this.notifications.success('Calendar IDs saved.');
          },
          error: () => this.savingCalendarIds.set(false),
        });
      },
      error: () => this.savingCalendarIds.set(false),
    });
  }

  connect(): void {
    this.connecting.set(true);
    this.calendarSettingsService.authorizeUrl().subscribe({
      next: ({ url }) => window.location.href = url,
      error: () => this.connecting.set(false),
    });
  }

  testConnection(): void {
    this.testing.set(true);
    this.testResults.set(null);
    this.calendarSettingsService.testConnection().subscribe({
      next: results => {
        this.testing.set(false);
        this.testResults.set(results);
      },
      error: () => this.testing.set(false),
    });
  }

  private load(): void {
    this.settingsService.list().subscribe(settings => {
      const calendarSettings = settings.filter(s => s.key.startsWith('calendar.'));
      this.settings.set(calendarSettings);
      this.clientIdDraft = calendarSettings.find(s => s.key === 'calendar.oauth.client-id')?.value ?? '';
      this.officialIdDraft = calendarSettings.find(s => s.key === 'calendar.official.id')?.value ?? '';
      this.internalIdDraft = calendarSettings.find(s => s.key === 'calendar.internal.id')?.value ?? '';
    });
  }
}
