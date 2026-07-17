import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Volunteer } from '../../../core/models/domain.model';
import { Page, PageParams } from '../../../core/models/api.model';
import { ResourceService } from '../../../core/services/resource.service';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class VolunteerService extends ResourceService<Volunteer> {
  private orgScopedEnv = inject(ENVIRONMENT);

  constructor() {
    super('volunteers');
  }

  getPageForOrg(orgId: string, params: PageParams): Observable<Page<Volunteer>> {
    let p = new HttpParams().set('page', String(params.page)).set('size', String(params.size));
    if (params.sort) p = p.set('sort', params.sort);
    return this.http.get<Page<Volunteer>>(
      `${this.orgScopedEnv.apiUrl}/organizations/${orgId}/volunteers`,
      { params: p },
    );
  }
}
