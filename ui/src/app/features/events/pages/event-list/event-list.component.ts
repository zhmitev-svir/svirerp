import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-event-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatIconModule, PageHeaderComponent],
  template: `
    <div class="page-container">
      <app-page-header
        title="Events"
        subtitle="Calendar events and registrations" />

      <mat-card class="coming-soon-card">
        <mat-card-content class="coming-soon-content">
          <mat-icon class="coming-soon-icon">event</mat-icon>
          <p class="mat-body-1">
            Event management will be available in a future iteration.
            Planned: calendar view, church events, registration management and resource allocation.
          </p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .coming-soon-card { margin-top: 16px; }
    .coming-soon-content { display: flex; align-items: center; gap: 16px; padding: 24px !important; }
    .coming-soon-icon { font-size: 48px; width: 48px; height: 48px; color: rgba(0,0,0,.3); }
  `],
})
export class EventListComponent {}
