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

import { OrganizationService } from '../../services/organization.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Organization } from '../../../../core/models/domain.model';

@Component({
  selector: 'app-organization-form',
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
    <h2 mat-dialog-title>{{ isEdit ? 'Edit' : 'Add' }} Organization</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="organization-form">

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" autocomplete="organization" />
          @if (form.controls.name.invalid && form.controls.name.touched) {
            <mat-error>Name is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Legal Name</mat-label>
          <input matInput formControlName="legalName" />
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Tax ID / EIN</mat-label>
            <input matInput formControlName="taxIdEin" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Nonprofit Type</mat-label>
            <input matInput formControlName="nonprofitType" placeholder="e.g. 501(c)(3)" />
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.hasError('email')) {
            <mat-error>Enter a valid email address</mat-error>
          }
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Phone</mat-label>
            <input matInput type="tel" formControlName="phone" autocomplete="tel" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Website</mat-label>
            <input matInput formControlName="website" autocomplete="url" />
          </mat-form-field>
        </div>

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
    .organization-form { display: flex; flex-direction: column; gap: 4px; padding-top: 8px; }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 12px; width: 100%; }
    .flex-1 { flex: 1; }
    .flex-2 { flex: 2; }
  `],
})
export class OrganizationFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private orgService = inject(OrganizationService);
  private dialogRef = inject(MatDialogRef<OrganizationFormComponent>);
  private notifications = inject(NotificationService);
  org = inject<Organization | null>(MAT_DIALOG_DATA);

  isEdit = !!this.org;
  saving = signal(false);

  form = this.fb.nonNullable.group({
    name:           ['', Validators.required],
    legalName:      [''],
    taxIdEin:       [''],
    nonprofitType:  [''],
    email:          ['', Validators.email],
    phone:          [''],
    website:        [''],
    addressLine1:   [''],
    city:           [''],
    state:          [''],
    zip:            [''],
  });

  ngOnInit(): void {
    if (this.org) {
      this.form.patchValue(this.org);
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
      ? this.orgService.update(this.org!.id, payload)
      : this.orgService.create(payload);

    op.subscribe({
      next: () => {
        this.notifications.success(`Organization ${this.isEdit ? 'updated' : 'created'}.`);
        this.dialogRef.close(true);
      },
      error: () => this.saving.set(false),
    });
  }
}
