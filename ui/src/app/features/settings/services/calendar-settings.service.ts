import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class CalendarSettingsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  authorizeUrl(): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(`${this.env.apiUrl}/settings/calendar/authorize-url`);
  }

  testConnection(): Observable<Record<string, string>> {
    return this.http.post<Record<string, string>>(`${this.env.apiUrl}/settings/calendar/test-connection`, {});
  }
}
