import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

/** Financial reports — Statement of Activities (the landing sub-tab), Financial Position, and
 *  Funds Overview, grouped under one Finance tab. */
@Component({
  selector: 'app-reports-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, MatTabsModule],
  template: `
    <nav mat-tab-nav-bar [tabPanel]="tabPanel" class="reports-tabs">
      <a mat-tab-link routerLink="activities" routerLinkActive #activitiesActive="routerLinkActive"
         [active]="activitiesActive.isActive">
        Statement of Activities
      </a>
      <a mat-tab-link routerLink="financial-position" routerLinkActive #positionActive="routerLinkActive"
         [active]="positionActive.isActive">
        Financial Position
      </a>
      <a mat-tab-link routerLink="funds-overview" routerLinkActive #fundsActive="routerLinkActive"
         [active]="fundsActive.isActive">
        Funds Overview
      </a>
    </nav>
    <mat-tab-nav-panel #tabPanel>
      <router-outlet />
    </mat-tab-nav-panel>
  `,
  styles: [`
    .reports-tabs { margin-bottom: 8px; }
  `],
})
export class ReportsShellComponent {}
