import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';

import { MemberService } from '../../services/member.service';
import { MembershipTypeService } from '../../services/membership-type.service';
import { OrgContextService } from '../../../../core/services/org-context.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Member, MembershipType } from '../../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../../core/models/api.model';
import { DataTableComponent, TableColumn, TableAction } from '../../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { MemberFormComponent } from '../member-form/member-form.component';
import { MemberImportDialogComponent } from '../member-import-dialog/member-import-dialog.component';

@Component({
  selector: 'app-member-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DataTableComponent, PageHeaderComponent, MatButtonModule, MatIconModule, MatFormFieldModule, MatSelectModule, FormsModule],
  template: `
    <div class="page-container">
      <app-page-header
        title="Members"
        subtitle="People holding a membership in the organization"
        actionLabel="Add Member"
        actionIcon="person_add"
        (action)="openForm()">
        <ng-container extraActions>
          <button mat-stroked-button (click)="downloadTemplate()">
            <mat-icon>download</mat-icon>
            Download Template
          </button>
          <button mat-stroked-button (click)="openImportDialog()">
            <mat-icon>upload</mat-icon>
            Import Members
          </button>
          <button mat-stroked-button [disabled]="recomputingTiers()" (click)="recomputeTiers()">
            <mat-icon>refresh</mat-icon>
            Recompute Tiers
          </button>
        </ng-container>
      </app-page-header>

      <div class="filter-bar">
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Status</mat-label>
          <mat-select [(ngModel)]="statusFilter" (selectionChange)="onFilterChange()">
            <mat-option [value]="null">All</mat-option>
            @for (s of statuses; track s) {
              <mat-option [value]="s">{{ s }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Membership Type</mat-label>
          <mat-select [(ngModel)]="membershipTypeFilter" (selectionChange)="onFilterChange()">
            <mat-option [value]="null">All</mat-option>
            @for (t of membershipTypes(); track t.id) {
              <mat-option [value]="t.id">{{ t.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>

      <app-data-table
        [columns]="columns"
        [actions]="actions"
        [data]="page()"
        [loading]="loading()"
        [pageParams]="pageParams()"
        (pageChange)="onPageChange($event)"
        (sortChange)="onSortChange($event)" />
    </div>
  `,
  styles: [`
    .filter-bar { display: flex; gap: 12px; margin-bottom: 8px; }
    .filter-field { width: 220px; }
  `],
})
export class MemberListComponent implements OnInit {
  private memberService = inject(MemberService);
  private membershipTypeService = inject(MembershipTypeService);
  private orgContext = inject(OrgContextService);
  private dialog = inject(MatDialog);
  private notifications = inject(NotificationService);
  private router = inject(Router);

  private orgId: string | null = null;
  page = signal<Page<Member> | null>(null);
  loading = signal(false);
  pageParams = signal<PageParams>(DEFAULT_PAGE_PARAMS);
  recomputingTiers = signal(false);
  membershipTypes = signal<MembershipType[]>([]);

  readonly statuses = ['active', 'inactive', 'suspended', 'expired', 'pending'];
  statusFilter: string | null = null;
  membershipTypeFilter: string | null = null;

  readonly columns: TableColumn[] = [
    { key: 'person', header: 'Name', cell: m => `${m.person.firstName} ${m.person.lastName}` },
    { key: 'membershipType', header: 'Membership Type', cell: m => m.membershipType.name },
    { key: 'status', header: 'Status' },
    { key: 'joinDate', header: 'Join Date', sortable: true },
    { key: 'expiryDate', header: 'Expiry Date', sortable: true },
    { key: 'memberNumber', header: 'Member #' },
  ];

  readonly actions: TableAction[] = [
    { icon: 'open_in_new', label: 'View', action: (m: Member) => this.openDetail(m) },
    { icon: 'edit', label: 'Edit', action: (m: Member) => this.openForm(m) },
    { icon: 'delete', label: 'Delete', action: (m: Member) => this.confirmDelete(m) },
  ];

  ngOnInit(): void {
    this.loadPage();
    this.orgContext.ensureOrgId().subscribe(orgId => {
      this.membershipTypeService.getPageForOrg(orgId, { page: 0, size: 100 }).subscribe(page => {
        this.membershipTypes.set(page.content);
      });
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageParams.set({ ...this.pageParams(), page: event.pageIndex, size: event.pageSize });
    this.loadPage();
  }

  onFilterChange(): void {
    this.pageParams.set({ ...this.pageParams(), page: 0 });
    this.loadPage();
  }

  onSortChange(sort: string | null): void {
    this.pageParams.set({ ...this.pageParams(), page: 0, sort: sort ?? undefined });
    this.loadPage();
  }

  openDetail(member: Member): void {
    this.router.navigate(['/membership/members', member.id]);
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

  downloadTemplate(): void {
    if (!this.orgId) {
      this.notifications.error('No organization found — create one first, under Organizations.');
      return;
    }
    this.memberService.downloadImportTemplate(this.orgId).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'member-import-template.csv';
        anchor.click();
        URL.revokeObjectURL(url);
      },
    });
  }

  openImportDialog(): void {
    if (!this.orgId) {
      this.notifications.error('No organization found — create one first, under Organizations.');
      return;
    }
    this.dialog
      .open(MemberImportDialogComponent, { width: '600px', data: { orgId: this.orgId } })
      .afterClosed()
      .subscribe(imported => { if (imported) this.loadPage(); });
  }

  recomputeTiers(): void {
    if (!this.orgId) {
      this.notifications.error('No organization found — create one first, under Organizations.');
      return;
    }
    this.recomputingTiers.set(true);
    this.memberService.recomputeTiers(this.orgId).subscribe({
      next: result => {
        this.recomputingTiers.set(false);
        this.notifications.success(`Recomputed tiers for ${result.membersProcessed} member(s).`);
        this.loadPage();
      },
      error: () => {
        this.recomputingTiers.set(false);
        this.notifications.error('Could not recompute tiers.');
      },
    });
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
        this.memberService.getPageForOrg(orgId, this.pageParams(), this.statusFilter, this.membershipTypeFilter).subscribe({
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
