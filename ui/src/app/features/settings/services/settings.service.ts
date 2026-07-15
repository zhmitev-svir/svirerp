import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENVIRONMENT } from '../../../core/tokens/environment.token';
import { AppSetting } from '../../../core/models/api.model';

/** Not a ResourceService<T> subclass — this isn't a standard paginated CRUD
 * resource, just a flat list-and-update-by-key API. */
@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  list(): Observable<AppSetting[]> {
    return this.http.get<AppSetting[]>(`${this.env.apiUrl}/settings`);
  }

  update(key: string, value: string): Observable<AppSetting> {
    return this.http.put<AppSetting>(`${this.env.apiUrl}/settings/${key}`, { value });
  }
}
