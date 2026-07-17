import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceService } from '../../../core/services/resource.service';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';
import { EventResource } from '../../../core/models/domain.model';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../../../core/models/api.model';

@Injectable({ providedIn: 'root' })
export class EventResourceService extends ResourceService<EventResource> {
  private readonly rootEnv = inject(ENVIRONMENT);

  constructor() {
    super('event-resources');
  }

  getForEvent(eventId: string, params: PageParams = DEFAULT_PAGE_PARAMS): Observable<Page<EventResource>> {
    let p = new HttpParams().set('page', String(params.page)).set('size', String(params.size));
    if (params.sort) {
      p = p.set('sort', params.sort);
    }
    return this.http.get<Page<EventResource>>(`${this.rootEnv.apiUrl}/events/${eventId}/resources`, { params: p });
  }
}
