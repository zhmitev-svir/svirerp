import {
  Component, inject, OnInit,
  signal, ChangeDetectionStrategy,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { EventRegistrationService } from '../../services/event-registration.service';
import { PersonService } from '../../../persons/services/person.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { EventRegistration, Person } from '../../../../core/models/domain.model';

interface EventRegistrationDialogData {
  eventId: string;
  entity: EventRegistration | null;
}

const STATUSES = ['registered', 'attended', 'cancelled', 'waitlisted', 'no_show'] as const;

@Component({
  selector: 'app-event-registration-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit' : 'Add' }} Registration</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="registration-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Person</mat-label>
          <mat-select formControlName="personId">
            @for (person of persons(); track person.id) {
              <mat-option [value]="person.id">{{ person.firstName }} {{ person.lastName }} ({{ person.email }})</mat-option>
            }
          </mat-select>
          @if (form.controls.personId.invalid && form.controls.personId.touched) {
            <mat-error>Person is required</mat-error>
          }
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              @for (s of statuses; track s) {
                <mat-option [value]="s">{{ s }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Fee Paid</mat-label>
            <input matInput type="number" step="0.01" min="0" formControlName="feePaid" />
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Ticket Number</mat-label>
          <input matInput formControlName="ticketNumber" />
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
    .registration-form { display: flex; flex-direction: column; gap: 4px; padding-top: 8px; min-width: 380px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 12px; width: 100%; }
    .flex-1 { flex: 1; }
  `],
})
export class EventRegistrationFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private registrationService = inject(EventRegistrationService);
  private personService = inject(PersonService);
  private dialogRef = inject(MatDialogRef<EventRegistrationFormComponent>);
  private notifications = inject(NotificationService);
  private data = inject<EventRegistrationDialogData>(MAT_DIALOG_DATA);

  private eventId = this.data.eventId;
  private entity = this.data.entity;
  isEdit = !!this.entity;
  saving = signal(false);

  readonly statuses = STATUSES;
  persons = signal<Person[]>([]);

  form = this.fb.nonNullable.group({
    personId: ['', Validators.required],
    status: ['registered'],
    feePaid: this.fb.control<number | null>(null),
    ticketNumber: [''],
    notes: [''],
  });

  ngOnInit(): void {
    this.personService.getPage({ page: 0, size: 200 }).subscribe(page => this.persons.set(page.content));

    if (this.entity) {
      this.form.patchValue({
        personId: this.entity.person.id,
        status: this.entity.status,
        feePaid: this.entity.feePaid ?? null,
        ticketNumber: this.entity.ticketNumber ?? '',
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
      person: { id: value.personId },
      status: value.status,
      feePaid: value.feePaid ?? 0,
      ticketNumber: value.ticketNumber || null,
      notes: value.notes || null,
    } as unknown as Partial<EventRegistration>;

    const op = this.isEdit
      ? this.registrationService.update(this.entity!.id, payload)
      : this.registrationService.create(payload);

    op.subscribe({
      next: () => {
        this.notifications.success(`Registration ${this.isEdit ? 'updated' : 'added'}.`);
        this.dialogRef.close(true);
      },
      error: () => this.saving.set(false),
    });
  }
}
