import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { ENVIRONMENT } from '../tokens/environment.token';
import { Organization } from '../models/domain.model';
import { Page } from '../models/api.model';

/**
 * This is a single-org app (see Organization's own doc comment: "Root anchor
 * for the entire ERP... scoped to one organisation") — there's no org
 * picker/switcher UI, so org-scoped features just resolve whichever single
 * organization exists. Calls straight to HttpClient rather than going
 * through OrganizationService to avoid core/ depending on a feature module.
 */
@Injectable({ providedIn: 'root' })
export class OrgContextService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly orgId = signal<string | null>(null);

  ensureOrgId(): Observable<string> {
    const cached = this.orgId();
    if (cached) {
      return of(cached);
    }

    const params = new HttpParams().set('page', '0').set('size', '1');
    return this.http.get<Page<Organization>>(`${this.env.apiUrl}/organizations`, { params }).pipe(
      switchMap(page =>
        page.content.length
          ? of(page.content[0].id)
          : throwError(() => new Error('No organization exists yet — create one first.')),
      ),
      tap(id => this.orgId.set(id)),
    );
  }
}
