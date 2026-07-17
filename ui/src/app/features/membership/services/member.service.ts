import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceService } from '../../../core/services/resource.service';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';
import { Member } from '../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS, MemberImportResult } from '../../../core/models/api.model';

@Injectable({ providedIn: 'root' })
export class MemberService extends ResourceService<Member> {
  private readonly orgScopedEnv = inject(ENVIRONMENT);

  constructor() {
    super('members');
  }

  /** List is org-scoped (unlike get/create/update/delete, which are flat). */
  getPageForOrg(orgId: string, params: PageParams = DEFAULT_PAGE_PARAMS, status?: string | null): Observable<Page<Member>> {
    let p = new HttpParams().set('page', String(params.page)).set('size', String(params.size));
    if (params.sort) {
      p = p.set('sort', params.sort);
    }
    if (status) {
      p = p.set('status', status);
    }
    return this.http.get<Page<Member>>(
      `${this.orgScopedEnv.apiUrl}/organizations/${orgId}/members`,
      { params: p },
    );
  }

  downloadImportTemplate(orgId: string): Observable<Blob> {
    return this.http.get(
      `${this.orgScopedEnv.apiUrl}/organizations/${orgId}/members/import-template`,
      { responseType: 'blob' },
    );
  }

  importMembers(orgId: string, file: File): Observable<MemberImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MemberImportResult>(
      `${this.orgScopedEnv.apiUrl}/organizations/${orgId}/members/import`,
      formData,
    );
  }
}
