import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';

import { OrganizationService } from '../../services/organization.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Organization } from '../../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../../core/models/api.model';
import { DataTableComponent, TableColumn, TableAction } from '../../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { OrganizationFormComponent } from '../organization-form/organization-form.component';

@Component({
  selector: 'app-organization-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DataTableComponent, PageHeaderComponent],
  template: `
    <div class="page-container">
      <app-page-header
        title="Organizations"
        subtitle="Church and organization profiles"
        actionLabel="Add Organization"
        actionIcon="add_business"
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
export class OrganizationListComponent implements OnInit {
  private orgService = inject(OrganizationService);
  private dialog = inject(MatDialog);
  private notifications = inject(NotificationService);

  page = signal<Page<Organization> | null>(null);
  loading = signal(false);
  pageParams = signal<PageParams>(DEFAULT_PAGE_PARAMS);

  readonly columns: TableColumn[] = [
    { key: 'name',      header: 'Name' },
    { key: 'taxIdEin',  header: 'Tax ID' },
    { key: 'email',     header: 'Email' },
    { key: 'phone',     header: 'Phone' },
    { key: 'city',      header: 'City' },
  ];

  readonly actions: TableAction[] = [
    { icon: 'edit',   label: 'Edit',   action: (o: Organization) => this.openForm(o) },
    { icon: 'delete', label: 'Delete', action: (o: Organization) => this.confirmDelete(o) },
  ];

  ngOnInit(): void {
    this.loadPage();
  }

  onPageChange(event: PageEvent): void {
    this.pageParams.set({ page: event.pageIndex, size: event.pageSize });
    this.loadPage();
  }

  openForm(org?: Organization): void {
    this.dialog
      .open(OrganizationFormComponent, { width: '540px', data: org ?? null })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadPage(); });
  }

  confirmDelete(org: Organization): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Delete Organization',
          message: `Delete "${org.name}"? This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .subscribe(confirmed => { if (confirmed) this.deleteOrg(org); });
  }

  private loadPage(): void {
    this.loading.set(true);
    this.orgService.getPage(this.pageParams()).subscribe({
      next: data => { this.page.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  private deleteOrg(org: Organization): void {
    this.orgService.remove(org.id).subscribe({
      next: () => {
        this.notifications.success('Organization deleted.');
        this.loadPage();
      },
    });
  }
}
