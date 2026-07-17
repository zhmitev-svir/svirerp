import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-page-header',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatIconModule],
  template: `
    <div class="page-header">
      <div class="page-header__text">
        <h1 class="mat-headline-5 page-title">{{ title() }}</h1>
        @if (subtitle()) {
          <p class="mat-body-2 page-subtitle">{{ subtitle() }}</p>
        }
      </div>
      <div class="page-header__actions">
        <ng-content select="[extraActions]" />
        @if (actionLabel()) {
          <button mat-flat-button color="primary" (click)="action.emit()">
            @if (actionIcon()) {
              <mat-icon>{{ actionIcon() }}</mat-icon>
            }
            {{ actionLabel() }}
          </button>
        }
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
      flex-wrap: wrap;
      gap: 16px;
    }
    .page-header__text { display: flex; flex-direction: column; gap: 2px; }
    .page-header__actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .page-title { margin: 0; }
    .page-subtitle { margin: 0; color: rgba(0,0,0,.54); }
  `],
})
export class PageHeaderComponent {
  title = input.required<string>();
  subtitle = input<string>('');
  actionLabel = input<string>('');
  actionIcon = input<string>('');

  action = output<void>();
}
