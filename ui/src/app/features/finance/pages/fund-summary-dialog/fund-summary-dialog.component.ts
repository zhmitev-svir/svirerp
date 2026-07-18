import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { FundService } from '../../services/fund.service';
import { Fund, FundSummary } from '../../../../core/models/domain.model';

interface FundSummaryDialogData {
  fund: Fund;
}

@Component({
  selector: 'app-fund-summary-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatDialogModule, MatButtonModule, MatProgressSpinnerModule, DecimalPipe],
  template: `
    <h2 mat-dialog-title>{{ data.fund.fundName }} — Financial Status</h2>

    <mat-dialog-content>
      @if (loading()) {
        <div class="loading"><mat-progress-spinner diameter="32" mode="indeterminate" /></div>
      } @else if (summary(); as s) {
        <dl class="summary-grid">
          <dt>Opening Balance</dt>
          <dd>{{ s.openingBalance | number: '1.2-2' }}</dd>
          <dt>Total Income</dt>
          <dd class="positive">{{ s.totalIncome | number: '1.2-2' }}</dd>
          <dt>Total Expense</dt>
          <dd class="negative">{{ s.totalExpense | number: '1.2-2' }}</dd>
          <dt class="total">Balance</dt>
          <dd class="total">{{ s.balance | number: '1.2-2' }}</dd>
        </dl>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-flat-button color="primary" mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .loading { display: flex; justify-content: center; padding: 24px; }
    .summary-grid {
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 8px 24px;
      min-width: 320px;
      margin: 0;
    }
    .summary-grid dt { color: rgba(0,0,0,.6); }
    .summary-grid dd { margin: 0; text-align: right; font-variant-numeric: tabular-nums; }
    .positive { color: #2e7d32; }
    .negative { color: #c62828; }
    .total { font-weight: 600; border-top: 1px solid rgba(0,0,0,.12); padding-top: 8px; }
  `],
})
export class FundSummaryDialogComponent implements OnInit {
  private fundService = inject(FundService);
  data = inject<FundSummaryDialogData>(MAT_DIALOG_DATA);

  loading = signal(true);
  summary = signal<FundSummary | null>(null);

  ngOnInit(): void {
    this.fundService.summary(this.data.fund.id).subscribe({
      next: s => { this.summary.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
