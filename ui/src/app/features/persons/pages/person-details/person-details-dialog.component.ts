import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { Person } from '../../../../core/models/domain.model';

@Component({
  selector: 'app-person-details-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ person.firstName }} {{ person.lastName }}</h2>

    <mat-dialog-content class="details">
      <div class="detail-row">
        <mat-icon>phone</mat-icon>
        <span>{{ person.phone || 'Not provided' }}</span>
      </div>
      <div class="detail-row">
        <mat-icon>email</mat-icon>
        <span>{{ person.email || 'Not provided' }}</span>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .details { display: flex; flex-direction: column; gap: 12px; padding-top: 8px; min-width: 280px; }
    .detail-row { display: flex; align-items: center; gap: 12px; }
    .detail-row mat-icon { color: rgba(0,0,0,.54); }
  `],
})
export class PersonDetailsDialogComponent {
  person = inject<Person>(MAT_DIALOG_DATA);
}
