import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { OrganizationService } from '../../features/organizations/services/organization.service';

/**
 * Single-org convenience: this app has no org picker/switcher, so org-scoped
 * features just need whichever organization already exists.
 */
@Injectable({ providedIn: 'root' })
export class OrgContextService {
  private orgService = inject(OrganizationService);

  ensureOrgId(): Observable<string> {
    return this.orgService.getPage({ page: 0, size: 1 }).pipe(
      map(page => {
        if (!page.content.length) {
          throw new Error('No organization exists yet — create one under Organizations first.');
        }
        return page.content[0].id;
      }),
    );
  }
}
