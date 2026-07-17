import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class GmailSettingsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  authorizeUrl(): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(`${this.env.apiUrl}/settings/gmail/authorize-url`);
  }

  testSend(to: string): Observable<void> {
    return this.http.post<void>(`${this.env.apiUrl}/settings/gmail/test-send`, { to });
  }
}
