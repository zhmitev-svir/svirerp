import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of, catchError, throwError } from 'rxjs';
import { ResourceService } from '../../../core/services/resource.service';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';
import { ChurchEvent } from '../../../core/models/domain.model';

@Injectable({ providedIn: 'root' })
export class ChurchEventService extends ResourceService<ChurchEvent> {
  private readonly rootEnv = inject(ENVIRONMENT);

  constructor() {
    super('church-events');
  }

  /** Null means "no church details recorded yet" — the backend 404s for that case, which isn't an error here. */
  getForEvent(calendarEventId: string): Observable<ChurchEvent | null> {
    return this.http.get<ChurchEvent>(`${this.rootEnv.apiUrl}/events/${calendarEventId}/church-details`).pipe(
      catchError((err: HttpErrorResponse) => err.status === 404 ? of(null) : throwError(() => err)),
    );
  }
}
