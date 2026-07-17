import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { VolunteerArea } from '../../../core/models/domain.model';
import { Page, PageParams } from '../../../core/models/api.model';
import { ResourceService } from '../../../core/services/resource.service';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class VolunteerAreaService extends ResourceService<VolunteerArea> {
  private orgScopedEnv = inject(ENVIRONMENT);

  constructor() {
    super('volunteer-areas');
  }

  getPageForOrg(orgId: string, params: PageParams): Observable<Page<VolunteerArea>> {
    let p = new HttpParams().set('page', String(params.page)).set('size', String(params.size));
    if (params.sort) p = p.set('sort', params.sort);
    return this.http.get<Page<VolunteerArea>>(
      `${this.orgScopedEnv.apiUrl}/organizations/${orgId}/volunteer-areas`,
      { params: p },
    );
  }
}
