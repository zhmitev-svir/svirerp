import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { SettingsService } from '../../services/settings.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { AppSetting } from '../../../../core/models/api.model';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

/**
 * Reads/writes the same stripe.* rows the generic Settings page can edit
 * (V42__create_stripe_integration.sql) — a dedicated page mainly so the webhook URL and its
 * purpose are documented right next to where the secrets go. Checkout itself happens entirely on
 * WordPress / a mobile card-reader app; svirerp only needs these two values to verify and react to
 * completed payments.
 */
@Component({
  selector: 'app-stripe-settings',
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
  ],
  template: `
    <div class="page-container">
      <app-page-header
        title="Stripe"
        subtitle="Receive completed Stripe payments (membership dues, event tickets, church services, and more) into the Finance module" />

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Webhook Endpoint</h3>
          <p class="section-hint">
            In the Stripe Dashboard, add a webhook endpoint pointing at
            <code>{{ webhookUrl }}</code> subscribed to the <code>checkout.session.completed</code>
            and <code>payment_intent.succeeded</code> events, then paste its signing secret below.
          </p>
        </mat-card-content>
      </mat-card>

      <mat-card class="setting-card">
        <mat-card-content>
          <h3 class="section-title">Credentials</h3>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Secret Key</mat-label>
            <input matInput type="password" [(ngModel)]="secretKeyDraft"
                   [placeholder]="secretKeyConfigured() ? '••••••••' : 'sk_live_... or sk_test_...'"
                   autocomplete="new-password" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Webhook Signing Secret</mat-label>
            <input matInput type="password" [(ngModel)]="webhookSecretDraft"
                   [placeholder]="webhookSecretConfigured() ? '••••••••' : 'whsec_...'"
                   autocomplete="new-password" />
          </mat-form-field>

          <button mat-flat-button color="primary" [disabled]="saving()" (click)="save()">
            @if (saving()) {
              <mat-progress-spinner diameter="20" mode="indeterminate" />
            } @else {
              Save Credentials
            }
          </button>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .setting-card { margin-bottom: 12px; }
    .section-title { margin: 0 0 8px; }
    .section-hint { font-size: 0.85em; color: rgba(0,0,0,.6); }
    .full-width { width: 100%; }
  `],
})
export class StripeSettingsComponent implements OnInit {
  private settingsService = inject(SettingsService);
  private notifications = inject(NotificationService);

  readonly webhookUrl = `${window.location.origin}/api/webhooks/stripe`;

  private settings = signal<AppSetting[]>([]);
  secretKeyDraft = '';
  webhookSecretDraft = '';
  saving = signal(false);

  secretKeyConfigured = computed(() =>
    this.settings().find(s => s.key === 'stripe.secret-key')?.hasValue ?? false);

  webhookSecretConfigured = computed(() =>
    this.settings().find(s => s.key === 'stripe.webhook-signing-secret')?.hasValue ?? false);

  ngOnInit(): void {
    this.load();
  }

  save(): void {
    this.saving.set(true);
    const updates: Array<[string, string]> = [];
    if (this.secretKeyDraft) updates.push(['stripe.secret-key', this.secretKeyDraft]);
    if (this.webhookSecretDraft) updates.push(['stripe.webhook-signing-secret', this.webhookSecretDraft]);

    if (!updates.length) {
      this.saving.set(false);
      return;
    }

    this.saveSequentially(updates, 0);
  }

  private saveSequentially(updates: Array<[string, string]>, index: number): void {
    if (index >= updates.length) {
      this.saving.set(false);
      this.secretKeyDraft = '';
      this.webhookSecretDraft = '';
      this.load();
      this.notifications.success('Stripe credentials saved.');
      return;
    }
    const [key, value] = updates[index];
    this.settingsService.update(key, value).subscribe({
      next: () => this.saveSequentially(updates, index + 1),
      error: () => this.saving.set(false),
    });
  }

  private load(): void {
    this.settingsService.list().subscribe(settings => {
      this.settings.set(settings.filter(s => s.key.startsWith('stripe.')));
    });
  }
}
