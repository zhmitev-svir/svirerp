import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';

import { VolunteerService } from '../../services/volunteer.service';
import { OrgContextService } from '../../../../core/services/org-context.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Person, Volunteer } from '../../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../../core/models/api.model';
import { DataTableComponent, TableColumn, TableAction } from '../../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { VolunteerFormComponent } from '../volunteer-form/volunteer-form.component';
import { PersonDetailsDialogComponent } from '../../../persons/pages/person-details/person-details-dialog.component';

@Component({
  selector: 'app-volunteer-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DataTableComponent, PageHeaderComponent],
  template: `
    <div class="page-container">
      <app-page-header
        title="Volunteers"
        subtitle="Volunteer directory, contacts and service areas"
        actionLabel="Add Volunteer"
        actionIcon="volunteer_activism"
        (action)="openForm()" />

      <app-data-table
        [columns]="columns"
        [actions]="actions"
        [data]="page()"
        [loading]="loading()"
        [pageParams]="pageParams()"
        (pageChange)="onPageChange($event)" />
    </div>
  `,
})
export class VolunteerListComponent implements OnInit {
  private volunteerService = inject(VolunteerService);
  private orgContext = inject(OrgContextService);
  private dialog = inject(MatDialog);
  private notifications = inject(NotificationService);

  private orgId: string | null = null;

  page = signal<Page<Volunteer> | null>(null);
  loading = signal(false);
  pageParams = signal<PageParams>(DEFAULT_PAGE_PARAMS);

  readonly columns: TableColumn[] = [
    {
      key: 'volunteer',
      header: 'Volunteer',
      cell: (v: Volunteer) => `${v.person.firstName} ${v.person.lastName}`,
      link: (v: Volunteer) => this.openPersonDetails(v.person),
    },
    { key: 'contact', header: 'Contact', cell: (v: Volunteer) => v.contactPerson ? `${v.contactPerson.firstName} ${v.contactPerson.lastName}` : '—' },
    { key: 'areas', header: 'Areas', cell: (v: Volunteer) => (v.areas ?? []).map(a => a.name).join(', ') || '—' },
    { key: 'onboardDate', header: 'Onboarded' },
    { key: 'isActive', header: 'Active', cell: (v: Volunteer) => v.isActive ? 'Yes' : 'No' },
  ];

  readonly actions: TableAction[] = [
    { icon: 'edit',   label: 'Edit',   action: (v: Volunteer) => this.openForm(v) },
    { icon: 'delete', label: 'Delete', action: (v: Volunteer) => this.confirmDelete(v) },
  ];

  ngOnInit(): void {
    this.orgContext.ensureOrgId().subscribe({
      next: orgId => {
        this.orgId = orgId;
        this.loadPage();
      },
      error: err => this.notifications.error(err.message ?? 'Could not load organization.'),
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageParams.set({ page: event.pageIndex, size: event.pageSize });
    this.loadPage();
  }

  openPersonDetails(person: Person): void {
    this.dialog.open(PersonDetailsDialogComponent, { width: '360px', data: person });
  }

  openForm(volunteer?: Volunteer): void {
    if (!this.orgId) return;
    this.dialog
      .open(VolunteerFormComponent, { width: '560px', data: { orgId: this.orgId, entity: volunteer ?? null } })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadPage(); });
  }

  confirmDelete(volunteer: Volunteer): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Delete Volunteer',
          message: `Delete ${volunteer.person.firstName} ${volunteer.person.lastName} from the volunteer roster? This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .subscribe(confirmed => { if (confirmed) this.deleteVolunteer(volunteer); });
  }

  private loadPage(): void {
    if (!this.orgId) return;
    this.loading.set(true);
    this.volunteerService.getPageForOrg(this.orgId, this.pageParams()).subscribe({
      next: data => { this.page.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  private deleteVolunteer(volunteer: Volunteer): void {
    this.volunteerService.remove(volunteer.id).subscribe({
      next: () => {
        this.notifications.success('Volunteer deleted.');
        this.loadPage();
      },
    });
  }
}
