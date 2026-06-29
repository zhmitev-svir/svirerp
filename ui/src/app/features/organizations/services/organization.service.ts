import { Injectable } from '@angular/core';
import { Organization } from '../../../core/models/domain.model';
import { ResourceService } from '../../../core/services/resource.service';

@Injectable({ providedIn: 'root' })
export class OrganizationService extends ResourceService<Organization> {
  constructor() {
    super('organizations');
  }
}
