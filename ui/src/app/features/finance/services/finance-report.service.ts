import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';
import {
  FundOverviewRow,
  StatementOfActivities,
  StatementOfFinancialPosition,
} from '../../../core/models/domain.model';

@Injectable({ providedIn: 'root' })
export class FinanceReportService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  statementOfActivities(
    orgId: string,
    entryDateFrom: string,
    entryDateTo: string,
    fundId?: string,
  ): Observable<StatementOfActivities> {
    let p = new HttpParams().set('entryDateFrom', entryDateFrom).set('entryDateTo', entryDateTo);
    if (fundId) {
      p = p.set('fundId', fundId);
    }
    return this.http.get<StatementOfActivities>(
      `${this.env.apiUrl}/organizations/${orgId}/reports/statement-of-activities`,
      { params: p },
    );
  }

  statementOfFinancialPosition(orgId: string, asOf: string): Observable<StatementOfFinancialPosition> {
    const p = new HttpParams().set('asOf', asOf);
    return this.http.get<StatementOfFinancialPosition>(
      `${this.env.apiUrl}/organizations/${orgId}/reports/statement-of-financial-position`,
      { params: p },
    );
  }

  fundsOverview(orgId: string): Observable<FundOverviewRow[]> {
    return this.http.get<FundOverviewRow[]>(
      `${this.env.apiUrl}/organizations/${orgId}/reports/funds-overview`,
    );
  }
}
