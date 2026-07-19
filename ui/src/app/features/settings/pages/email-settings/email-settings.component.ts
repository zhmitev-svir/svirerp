import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { SettingsService } from '../../services/settings.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

type EmailMode = 'DISABLED' | 'LIVE' | 'TEST';

/**
 * Reads/writes the same email.* rows the generic Settings page can edit
 * (V39__create_email_settings.sql), with a radio group instead of that
 * page's plain text input since the three modes are mutually exclusive.
 * This is the single central switch EmailService.send() checks before
 * every outgoing email, app-wide.
 */
@Component({
  selector: 'app-email-settings',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    PageHeaderComponent,
    MatCardModule,
    MatRadioModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Email"
        subtitle="Central switch for all outgoing app email" />

      <mat-card class="setting-card">
        <mat-card-content>
          <mat-radio-group [(ngModel)]="modeDraft" class="mode-group">
            <mat-radio-button value="DISABLED">
              Disabled — no email leaves the app, for any reason
            </mat-radio-button>
            <mat-radio-button value="LIVE">
              Live — email sends normally to its real recipient
            </mat-radio-button>
            <mat-radio-button value="TEST">
              Test — every email is redirected to a single address below, unchanged
            </mat-radio-button>
          </mat-radio-group>

          @if (modeDraft === 'TEST') {
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Test address</mat-label>
              <input matInput type="email" [(ngModel)]="testAddressDraft" autocomplete="email" />
            </mat-form-field>
          }

          <button mat-flat-button color="primary" [disabled]="saving()" (click)="save()">
            @if (saving()) {
              <mat-progress-spinner diameter="20" mode="indeterminate" />
            } @else {
              Save
            }
          </button>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .setting-card { margin-bottom: 12px; }
    .mode-group { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; }
    .full-width { width: 100%; max-width: 400px; }
  `],
})
export class EmailSettingsComponent implements OnInit {
  private settingsService = inject(SettingsService);
  private notifications = inject(NotificationService);

  saving = signal(false);
  modeDraft: EmailMode = 'DISABLED';
  testAddressDraft = '';

  ngOnInit(): void {
    this.load();
  }

  save(): void {
    this.saving.set(true);
    this.settingsService.update('email.mode', this.modeDraft).subscribe({
      next: () => {
        this.settingsService.update('email.test-address', this.testAddressDraft).subscribe({
          next: () => {
            this.saving.set(false);
            this.notifications.success('Email settings saved.');
          },
          error: () => this.saving.set(false),
        });
      },
      error: () => this.saving.set(false),
    });
  }

  private load(): void {
    this.settingsService.list().subscribe(settings => {
      const emailSettings = settings.filter(s => s.key.startsWith('email.'));
      this.modeDraft = (emailSettings.find(s => s.key === 'email.mode')?.value as EmailMode) ?? 'DISABLED';
      this.testAddressDraft = emailSettings.find(s => s.key === 'email.test-address')?.value ?? '';
    });
  }
}
