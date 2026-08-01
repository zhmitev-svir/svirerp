import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';

import { FinanceTransactionService } from '../../services/finance-transaction.service';
import { JournalEntry, JournalLine } from '../../../../core/models/domain.model';

interface JournalEntryDetailDialogData {
  entry: JournalEntry;
}

function partyName(entry: JournalEntry): string {
  if (entry.payer) return `${entry.payer.firstName} ${entry.payer.lastName}`;
  if (entry.vendor) return entry.vendor.name;
  return '—';
}

/**
 * Shows the real double-entry lines behind a Transactions-tab row — the list only shows the
 * entry-level totalDebit (the gross amount), which hides a processing-fee split (e.g. a Stripe
 * payment posts a net deposit line, a fee-expense line, and a gross revenue credit line, all
 * summing to the same totalDebit). This is where that split actually becomes visible.
 */
@Component({
  selector: 'app-journal-entry-detail-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule, MatButtonModule, MatProgressSpinnerModule, MatTableModule, DecimalPipe],
  template: `
    <h2 mat-dialog-title>Transaction Detail</h2>

    <mat-dialog-content>
      <dl class="header-grid">
        <dt>Date</dt>
        <dd>{{ data.entry.entryDate }}</dd>
        <dt>Description</dt>
        <dd>{{ data.entry.description || '—' }}</dd>
        <dt>Category</dt>
        <dd>{{ data.entry.categoryAccount?.accountName ?? '—' }}</dd>
        <dt>Project / Fund</dt>
        <dd>{{ data.entry.fund?.fundName ?? '—' }}</dd>
        <dt>Payer / Payee</dt>
        <dd>{{ partyName(data.entry) }}</dd>
        <dt>Method</dt>
        <dd>{{ data.entry.paymentMethod ?? '—' }}</dd>
        <dt>Status</dt>
        <dd>{{ data.entry.status }}</dd>
      </dl>

      @if (loading()) {
        <div class="loading"><mat-progress-spinner diameter="32" mode="indeterminate" /></div>
      } @else {
        <table mat-table [dataSource]="lines()" class="lines-table">
          <ng-container matColumnDef="account">
            <th mat-header-cell *matHeaderCellDef>Account</th>
            <td mat-cell *matCellDef="let line">{{ line.account.accountNumber }} — {{ line.account.accountName }}</td>
          </ng-container>
          <ng-container matColumnDef="debit">
            <th mat-header-cell *matHeaderCellDef>Debit</th>
            <td mat-cell *matCellDef="let line">
              {{ line.debitAmount > 0 ? (line.debitAmount | number: '1.2-2') : '—' }}
            </td>
          </ng-container>
          <ng-container matColumnDef="credit">
            <th mat-header-cell *matHeaderCellDef>Credit</th>
            <td mat-cell *matCellDef="let line">
              {{ line.creditAmount > 0 ? (line.creditAmount | number: '1.2-2') : '—' }}
            </td>
          </ng-container>
          <ng-container matColumnDef="memo">
            <th mat-header-cell *matHeaderCellDef>Memo</th>
            <td mat-cell *matCellDef="let line">{{ line.memo || '—' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="lineColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: lineColumns"></tr>
        </table>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-flat-button color="primary" mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .header-grid {
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 4px 16px;
      margin: 0 0 16px;
      min-width: 420px;
    }
    .header-grid dt { color: rgba(0,0,0,.6); }
    .header-grid dd { margin: 0; }
    .loading { display: flex; justify-content: center; padding: 24px; }
    .lines-table { width: 100%; }
  `],
})
export class JournalEntryDetailDialogComponent implements OnInit {
  private transactionService = inject(FinanceTransactionService);
  data = inject<JournalEntryDetailDialogData>(MAT_DIALOG_DATA);

  loading = signal(true);
  lines = signal<JournalLine[]>([]);
  readonly lineColumns = ['account', 'debit', 'credit', 'memo'];

  partyName = partyName;

  ngOnInit(): void {
    this.transactionService.getLines(this.data.entry.id).subscribe({
      next: lines => { this.lines.set(lines); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
