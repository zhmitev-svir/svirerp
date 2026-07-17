import {
  Component, inject, OnInit,
  signal, ChangeDetectionStrategy,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { EventResourceService } from '../../services/event-resource.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { EventResource } from '../../../../core/models/domain.model';

interface EventResourceDialogData {
  eventId: string;
  entity: EventResource | null;
}

@Component({
  selector: 'app-event-resource-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit' : 'Add' }} Resource</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="resource-form">
        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Type</mat-label>
            <input matInput formControlName="resourceType" placeholder="e.g. equipment, room, staff" />
            @if (form.controls.resourceType.invalid && form.controls.resourceType.touched) {
              <mat-error>Type is required</mat-error>
            }
          </mat-form-field>
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Name</mat-label>
            <input matInput formControlName="resourceName" />
            @if (form.controls.resourceName.invalid && form.controls.resourceName.touched) {
              <mat-error>Name is required</mat-error>
            }
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Assigned To</mat-label>
          <input matInput formControlName="assignedTo" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Notes</mat-label>
          <textarea matInput formControlName="notes" rows="2"></textarea>
        </mat-form-field>
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
    .resource-form { display: flex; flex-direction: column; gap: 4px; padding-top: 8px; min-width: 380px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 12px; width: 100%; }
    .flex-1 { flex: 1; }
  `],
})
export class EventResourceFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private resourceService = inject(EventResourceService);
  private dialogRef = inject(MatDialogRef<EventResourceFormComponent>);
  private notifications = inject(NotificationService);
  private data = inject<EventResourceDialogData>(MAT_DIALOG_DATA);

  private eventId = this.data.eventId;
  private entity = this.data.entity;
  isEdit = !!this.entity;
  saving = signal(false);

  form = this.fb.nonNullable.group({
    resourceType: ['', Validators.required],
    resourceName: ['', Validators.required],
    assignedTo: [''],
    notes: [''],
  });

  ngOnInit(): void {
    if (this.entity) {
      this.form.patchValue({
        resourceType: this.entity.resourceType,
        resourceName: this.entity.resourceName,
        assignedTo: this.entity.assignedTo ?? '',
        notes: this.entity.notes ?? '',
      });
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const value = this.form.getRawValue();
    const payload = {
      event: { id: this.eventId },
      resourceType: value.resourceType,
      resourceName: value.resourceName,
      assignedTo: value.assignedTo || null,
      notes: value.notes || null,
    } as unknown as Partial<EventResource>;

    const op = this.isEdit
      ? this.resourceService.update(this.entity!.id, payload)
      : this.resourceService.create(payload);

    op.subscribe({
      next: () => {
        this.notifications.success(`Resource ${this.isEdit ? 'updated' : 'added'}.`);
        this.dialogRef.close(true);
      },
      error: () => this.saving.set(false),
    });
  }
}
