import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';

import { MemberService } from '../../services/member.service';
import { OrgContextService } from '../../../../core/services/org-context.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Member } from '../../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../../core/models/api.model';
import { DataTableComponent, TableColumn, TableAction } from '../../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { MemberFormComponent } from '../member-form/member-form.component';

@Component({
  selector: 'app-member-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DataTableComponent, PageHeaderComponent],
  template: `
    <div class="page-container">
      <app-page-header
        title="Members"
        subtitle="People holding a membership in the organization"
        actionLabel="Add Member"
        actionIcon="person_add"
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
export class MemberListComponent implements OnInit {
  private memberService = inject(MemberService);
  private orgContext = inject(OrgContextService);
  private dialog = inject(MatDialog);
  private notifications = inject(NotificationService);

  private orgId: string | null = null;
  page = signal<Page<Member> | null>(null);
  loading = signal(false);
  pageParams = signal<PageParams>(DEFAULT_PAGE_PARAMS);

  readonly columns: TableColumn[] = [
    { key: 'person', header: 'Name', cell: m => `${m.person.firstName} ${m.person.lastName}` },
    { key: 'membershipType', header: 'Membership Type', cell: m => m.membershipType.name },
    { key: 'status', header: 'Status' },
    { key: 'joinDate', header: 'Join Date' },
    { key: 'expiryDate', header: 'Expiry Date' },
    { key: 'memberNumber', header: 'Member #' },
  ];

  readonly actions: TableAction[] = [
    { icon: 'edit', label: 'Edit', action: (m: Member) => this.openForm(m) },
    { icon: 'delete', label: 'Delete', action: (m: Member) => this.confirmDelete(m) },
  ];

  ngOnInit(): void {
    this.loadPage();
  }

  onPageChange(event: PageEvent): void {
    this.pageParams.set({ page: event.pageIndex, size: event.pageSize });
    this.loadPage();
  }

  openForm(member?: Member): void {
    if (!this.orgId) {
      this.notifications.error('No organization found — create one first, under Organizations.');
      return;
    }
    this.dialog
      .open(MemberFormComponent, {
        width: '540px',
        data: { orgId: this.orgId, member: member ?? null },
      })
      .afterClosed()
      .subscribe(saved => { if (saved) this.loadPage(); });
  }

  confirmDelete(member: Member): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Delete Member',
          message: `Delete ${member.person.firstName} ${member.person.lastName}'s membership? This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .subscribe(confirmed => { if (confirmed) this.deleteMember(member); });
  }

  private loadPage(): void {
    this.loading.set(true);
    this.orgContext.ensureOrgId().subscribe({
      next: orgId => {
        this.orgId = orgId;
        this.memberService.getPageForOrg(orgId, this.pageParams()).subscribe({
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

  private deleteMember(member: Member): void {
    this.memberService.remove(member.id).subscribe({
      next: () => {
        this.notifications.success('Member deleted.');
        this.loadPage();
      },
    });
  }
}
