import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

/** Stripe integration — Payments (the landing sub-tab) and Mappings, grouped under one Finance tab. */
@Component({
  selector: 'app-stripe-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, MatTabsModule],
  template: `
    <nav mat-tab-nav-bar [tabPanel]="tabPanel" class="stripe-tabs">
      <a mat-tab-link routerLink="payments" routerLinkActive #paymentsActive="routerLinkActive"
         [active]="paymentsActive.isActive">
        Payments
      </a>
      <a mat-tab-link routerLink="mappings" routerLinkActive #mappingsActive="routerLinkActive"
         [active]="mappingsActive.isActive">
        Mappings
      </a>
    </nav>
    <mat-tab-nav-panel #tabPanel>
      <router-outlet />
    </mat-tab-nav-panel>
  `,
  styles: [`
    .stripe-tabs { margin-bottom: 8px; }
  `],
})
export class StripeShellComponent {}
