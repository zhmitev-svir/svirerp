import { Component, input, output, computed, ChangeDetectionStrategy } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../core/models/api.model';

export interface TableColumn {
  key: string;
  header: string;
  /** Optional transform applied when rendering the cell value. */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  cell?: (row: any) => string;
}

export interface TableAction {
  icon: string;
  label: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  action: (row: any) => void;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  disabled?: (row: any) => boolean;
}

/**
 * Generic paginated data table backed by Angular Material.
 *
 * Usage:
 *   <app-data-table
 *     [columns]="columns"
 *     [actions]="actions"
 *     [data]="page()"
 *     [loading]="loading()"
 *     [pageParams]="pageParams()"
 *     (pageChange)="onPage($event)" />
 */
@Component({
  selector: 'app-data-table',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  template: `
    <div class="table-wrapper">
      @if (loading()) {
        <mat-progress-bar mode="indeterminate" class="loading-bar" />
      }

      <mat-table [dataSource]="rows()" class="data-table mat-elevation-z1">

        @for (col of columns(); track col.key) {
          <ng-container [matColumnDef]="col.key">
            <mat-header-cell *matHeaderCellDef>{{ col.header }}</mat-header-cell>
            <mat-cell *matCellDef="let row">
              {{ col.cell ? col.cell(row) : (row[col.key] ?? '') }}
            </mat-cell>
          </ng-container>
        }

        <ng-container matColumnDef="_actions">
          <mat-header-cell *matHeaderCellDef class="actions-cell">Actions</mat-header-cell>
          <mat-cell *matCellDef="let row" class="actions-cell">
            @for (act of actions(); track act.icon) {
              <button mat-icon-button
                      [matTooltip]="act.label"
                      [disabled]="act.disabled ? act.disabled(row) : false"
                      (click)="act.action(row)">
                <mat-icon>{{ act.icon }}</mat-icon>
              </button>
            }
          </mat-cell>
        </ng-container>

        <mat-header-row *matHeaderRowDef="displayedColumns()" />
        <mat-row *matRowDef="let row; columns: displayedColumns()" />

        <tr class="mat-row no-data-row" *matNoDataRow>
          <td [attr.colspan]="displayedColumns().length" class="no-data-cell">
            {{ emptyMessage() }}
          </td>
        </tr>
      </mat-table>

      <mat-paginator
        [length]="data()?.totalElements ?? 0"
        [pageSize]="pageParams().size"
        [pageIndex]="pageParams().page"
        [pageSizeOptions]="[10, 20, 50]"
        showFirstLastButtons
        (page)="pageChange.emit($event)" />
    </div>
  `,
  styles: [`
    .table-wrapper { position: relative; }
    .loading-bar { position: absolute; top: 0; left: 0; right: 0; z-index: 1; }
    .data-table { width: 100%; margin-top: 4px; }
    .actions-cell { justify-content: flex-end; min-width: 100px; }
    .no-data-row { display: block; }
    .no-data-cell { text-align: center; padding: 32px; color: rgba(0,0,0,.54); display: block; }
  `],
})
export class DataTableComponent {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data = input<Page<any> | null>(null);
  columns = input<TableColumn[]>([]);
  actions = input<TableAction[]>([]);
  loading = input<boolean>(false);
  emptyMessage = input<string>('No records found.');
  pageParams = input<PageParams>(DEFAULT_PAGE_PARAMS);

  pageChange = output<PageEvent>();

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  rows = computed<any[]>(() => this.data()?.content ?? []);
  displayedColumns = computed(() => [
    ...this.columns().map(c => c.key),
    '_actions',
  ]);
}
