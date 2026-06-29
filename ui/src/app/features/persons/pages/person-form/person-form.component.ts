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

import { PersonService } from '../../services/person.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Person } from '../../../../core/models/domain.model';

@Component({
  selector: 'app-person-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit' : 'Add' }} Person</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="person-form">

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>First Name</mat-label>
            <input matInput formControlName="firstName" autocomplete="given-name" />
            @if (form.controls.firstName.invalid && form.controls.firstName.touched) {
              <mat-error>First name is required</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Last Name</mat-label>
            <input matInput formControlName="lastName" autocomplete="family-name" />
            @if (form.controls.lastName.invalid && form.controls.lastName.touched) {
              <mat-error>Last name is required</mat-error>
            }
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.hasError('required') && form.controls.email.touched) {
            <mat-error>Email is required</mat-error>
          }
          @if (form.controls.email.hasError('email')) {
            <mat-error>Enter a valid email address</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Phone</mat-label>
          <input matInput type="tel" formControlName="phone" autocomplete="tel" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Address</mat-label>
          <input matInput formControlName="addressLine1" autocomplete="street-address" />
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-2">
            <mat-label>City</mat-label>
            <input matInput formControlName="city" autocomplete="address-level2" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>State</mat-label>
            <input matInput formControlName="state" maxlength="2" autocomplete="address-level1" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>ZIP</mat-label>
            <input matInput formControlName="zip" autocomplete="postal-code" />
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
          {{ isEdit ? 'Save Changes' : 'Create' }}
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .person-form { display: flex; flex-direction: column; gap: 4px; padding-top: 8px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 12px; width: 100%; }
    .flex-1 { flex: 1; }
    .flex-2 { flex: 2; }
  `],
})
export class PersonFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private personService = inject(PersonService);
  private dialogRef = inject(MatDialogRef<PersonFormComponent>);
  private notifications = inject(NotificationService);
  person = inject<Person | null>(MAT_DIALOG_DATA);

  isEdit = !!this.person;
  saving = signal(false);

  form = this.fb.nonNullable.group({
    firstName:    ['', Validators.required],
    lastName:     ['', Validators.required],
    email:        ['', [Validators.required, Validators.email]],
    phone:        [''],
    addressLine1: [''],
    city:         [''],
    state:        [''],
    zip:          [''],
  });

  ngOnInit(): void {
    if (this.person) {
      this.form.patchValue(this.person);
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const payload = this.form.getRawValue();
    const op = this.isEdit
      ? this.personService.update(this.person!.id, payload)
      : this.personService.create(payload);

    op.subscribe({
      next: () => {
        this.notifications.success(`Person ${this.isEdit ? 'updated' : 'created'}.`);
        this.dialogRef.close(true);
      },
      error: () => this.saving.set(false),
    });
  }
}
