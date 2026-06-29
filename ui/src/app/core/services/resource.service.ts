import { inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page, PageParams, DEFAULT_PAGE_PARAMS } from '../models/api.model';
import { ENVIRONMENT } from '../tokens/environment.token';

/**
 * Base class for all domain-specific API services.
 *
 * Subclasses call super(path) where path is the resource segment,
 * e.g. super('persons').  They inherit full CRUD without boilerplate.
 * Additional endpoints can be added by calling this.endpoint(...) in
 * the subclass and delegating to this.http.
 */
export abstract class ResourceService<T> {
  protected readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  constructor(protected readonly resourcePath: string) {}

  protected endpoint(suffix = ''): string {
    return `${this.env.apiUrl}/${this.resourcePath}${suffix}`;
  }

  getPage(params: PageParams = DEFAULT_PAGE_PARAMS): Observable<Page<T>> {
    let p = new HttpParams()
      .set('page', String(params.page))
      .set('size', String(params.size));
    if (params.sort) {
      p = p.set('sort', params.sort);
    }
    return this.http.get<Page<T>>(this.endpoint(), { params: p });
  }

  getById(id: string): Observable<T> {
    return this.http.get<T>(this.endpoint(`/${id}`));
  }

  create(body: Partial<T>): Observable<T> {
    return this.http.post<T>(this.endpoint(), body);
  }

  update(id: string, body: Partial<T>): Observable<T> {
    return this.http.put<T>(this.endpoint(`/${id}`), body);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(this.endpoint(`/${id}`));
  }
}
