import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { MeetingMinutesService } from '../../services/meeting-minutes.service';
import { OrgContextService } from '../../../../core/services/org-context.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { MeetingMinutes } from '../../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../../core/models/api.model';
import { DataTableComponent, TableColumn, TableAction } from '../../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { MeetingMinutesFormComponent } from '../meeting-minutes-form/meeting-minutes-form.component';

function excerpt(text: string | undefined, maxLength = 80): string {
  if (!text) return '—';
  return text.length > maxLength ? `${text.slice(0, maxLength)}…` : text;
}

@Component({
  selector: 'app-meeting-minutes-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DataTableComponent, PageHeaderComponent],
  template: `
    <div class="page-container">
      <app-page-header
        title="Meeting Minutes"
        subtitle="Board and trustee meeting records"
        actionLabel="Add Meeting Minutes"
        actionIcon="post_add"
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
export class MeetingMinutesListComponent implements OnInit {
  private meetingMinutesService = inject(MeetingMinutesService);
  private orgContext = inject(OrgContextService);
  private dialog = inject(MatDialog);
  private notifications = inject(NotificationService);
  private router = inject(Router);

  private orgId: string | null = null;
  page = signal<Page<MeetingMinutes> | null>(null);
  loading = signal(false);
  pageParams = signal<PageParams>(DEFAULT_PAGE_PARAMS);

  readonly columns: TableColumn[] = [
    { key: 'meetingDate', header: 'Date' },
    { key: 'title', header: 'Title' },
    { key: 'summary', header: 'Summary', cell: m => excerpt(m.summary) },
  ];

  readonly actions: TableAction[] = [
    { icon: 'open_in_new', label: 'View', action: (m: MeetingMinutes) => this.openDetail(m) },
    { icon: 'edit', label: 'Edit', action: (m: MeetingMinutes) => this.openForm(m) },
    { icon: 'delete', label: 'Delete', action: (m: MeetingMinutes) => this.confirmDelete(m) },
  ];

  ngOnInit(): void {
    this.loadPage();
  }

  onPageChange(event: PageEvent): void {
    this.pageParams.set({ page: event.pageIndex, size: event.pageSize });
    this.loadPage();
  }

  openDetail(minutes: MeetingMinutes): void {
    this.router.navigate(['/governance/meeting-minutes', minutes.id]);
  }

  openForm(minutes?: MeetingMinutes): void {
    if (!this.orgId) {
      this.notifications.error('No organization found — create one first, under Organizations.');
      return;
    }
    this.dialog
      .open(MeetingMinutesFormComponent, {
        width: '540px',
        data: { orgId: this.orgId, minutes: minutes ?? null },
      })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadPage(); });
  }

  confirmDelete(minutes: MeetingMinutes): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Delete Meeting Minutes',
          message: `Delete "${minutes.title}" and all of its action items? This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .subscribe(confirmed => { if (confirmed) this.deleteMinutes(minutes); });
  }

  private loadPage(): void {
    this.loading.set(true);
    this.orgContext.ensureOrgId().subscribe({
      next: orgId => {
        this.orgId = orgId;
        this.meetingMinutesService.getPageForOrg(orgId, this.pageParams()).subscribe({
          next: data => { this.page.set(data); this.loading.set(false); },
          error: () => this.loading.set(false),
        });
      },
      error: () => {
        this.loading.set(false);
        this.notifications.error('No organization found — create one first, under Organizations.');
      },
    });
  }

  private deleteMinutes(minutes: MeetingMinutes): void {
    this.meetingMinutesService.remove(minutes.id).subscribe({
      next: () => {
        this.notifications.success('Meeting minutes deleted.');
        this.loadPage();
      },
    });
  }
}
