import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceService } from '../../../core/services/resource.service';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';
import { MeetingMinutes } from '../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../core/models/api.model';

@Injectable({ providedIn: 'root' })
export class MeetingMinutesService extends ResourceService<MeetingMinutes> {
  private readonly orgScopedEnv = inject(ENVIRONMENT);

  constructor() {
    super('meeting-minutes');
  }

  /** List is org-scoped (unlike get/create/update/delete, which are flat). */
  getPageForOrg(orgId: string, params: PageParams = DEFAULT_PAGE_PARAMS): Observable<Page<MeetingMinutes>> {
    let p = new HttpParams().set('page', String(params.page)).set('size', String(params.size));
    if (params.sort) {
      p = p.set('sort', params.sort);
    }
    return this.http.get<Page<MeetingMinutes>>(
      `${this.orgScopedEnv.apiUrl}/organizations/${orgId}/meeting-minutes`,
      { params: p },
    );
  }
}
