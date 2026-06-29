import { Injectable } from '@angular/core';
import { Person } from '../../../core/models/domain.model';
import { ResourceService } from '../../../core/services/resource.service';

@Injectable({ providedIn: 'root' })
export class PersonService extends ResourceService<Person> {
  constructor() {
    super('persons');
  }
}
