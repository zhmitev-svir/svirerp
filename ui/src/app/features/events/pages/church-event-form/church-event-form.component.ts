import {
  Component, inject, OnInit,
  signal, ChangeDetectionStrategy,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ChurchEventService } from '../../services/church-event.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { ChurchEvent } from '../../../../core/models/domain.model';

interface ChurchEventDialogData {
  calendarEventId: string;
  entity: ChurchEvent | null;
}

@Component({
  selector: 'app-church-event-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit' : 'Add' }} Church Service Details</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="church-form">
        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Service Type</mat-label>
            <input matInput formControlName="serviceType" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Liturgical Season</mat-label>
            <input matInput formControlName="liturgicalSeason" />
          </mat-form-field>
        </div>

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Officiant</mat-label>
            <input matInput formControlName="officiant" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Sermon Title</mat-label>
            <input matInput formControlName="sermonTitle" />
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Scripture Readings</mat-label>
          <textarea matInput formControlName="scriptureReadings" rows="2"></textarea>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Music Selections</mat-label>
          <textarea matInput formControlName="musicSelections" rows="2"></textarea>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Special Instructions</mat-label>
          <textarea matInput formControlName="specialInstructions" rows="2"></textarea>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Offering Collected</mat-label>
            <input matInput type="number" step="0.01" min="0" formControlName="offeringCollected" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Attendance Count</mat-label>
            <input matInput type="number" min="0" formControlName="attendanceCount" />
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button color="primary" [disabled]="saving()" (click)="save()">
        @if (saving()) {
          <mat-progress-spinner diameter="20" mode="indeterminate" />
        } @else {
          {{ isEdit ? 'Save Changes' : 'Add' }}
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .church-form { display: flex; flex-direction: column; gap: 4px; padding-top: 8px; min-width: 420px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 12px; width: 100%; }
    .flex-1 { flex: 1; }
  `],
})
export class ChurchEventFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private churchEventService = inject(ChurchEventService);
  private dialogRef = inject(MatDialogRef<ChurchEventFormComponent>);
  private notifications = inject(NotificationService);
  private data = inject<ChurchEventDialogData>(MAT_DIALOG_DATA);

  private calendarEventId = this.data.calendarEventId;
  private entity = this.data.entity;
  isEdit = !!this.entity;
  saving = signal(false);

  form = this.fb.nonNullable.group({
    serviceType: [''],
    liturgicalSeason: [''],
    officiant: [''],
    sermonTitle: [''],
    scriptureReadings: [''],
    musicSelections: [''],
    specialInstructions: [''],
    offeringCollected: this.fb.control<number | null>(null),
    attendanceCount: this.fb.control<number | null>(null),
  });

  ngOnInit(): void {
    if (this.entity) {
      this.form.patchValue({
        serviceType: this.entity.serviceType ?? '',
        liturgicalSeason: this.entity.liturgicalSeason ?? '',
        officiant: this.entity.officiant ?? '',
        sermonTitle: this.entity.sermonTitle ?? '',
        scriptureReadings: this.entity.scriptureReadings ?? '',
        musicSelections: this.entity.musicSelections ?? '',
        specialInstructions: this.entity.specialInstructions ?? '',
        offeringCollected: this.entity.offeringCollected ?? null,
        attendanceCount: this.entity.attendanceCount ?? null,
      });
    }
  }

  save(): void {
    this.saving.set(true);
    const value = this.form.getRawValue();
    const payload = {
      calendarEvent: { id: this.calendarEventId },
      serviceType: value.serviceType || null,
      liturgicalSeason: value.liturgicalSeason || null,
      officiant: value.officiant || null,
      sermonTitle: value.sermonTitle || null,
      scriptureReadings: value.scriptureReadings || null,
      musicSelections: value.musicSelections || null,
      specialInstructions: value.specialInstructions || null,
      offeringCollected: value.offeringCollected ?? 0,
      attendanceCount: value.attendanceCount ?? 0,
    } as unknown as Partial<ChurchEvent>;

    const op = this.isEdit
      ? this.churchEventService.update(this.entity!.id, payload)
      : this.churchEventService.create(payload);

    op.subscribe({
      next: () => {
        this.notifications.success(`Church service details ${this.isEdit ? 'updated' : 'added'}.`);
        this.dialogRef.close(true);
      },
      error: () => this.saving.set(false),
    });
  }
}
