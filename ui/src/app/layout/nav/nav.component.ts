import { Component, output, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-nav',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, MatListModule, MatIconModule],
  template: `
    <mat-nav-list class="nav-list">
      @for (item of navItems; track item.route) {
        <a mat-list-item
           [routerLink]="item.route"
           routerLinkActive="nav-active"
           (click)="navigate.emit()">
          <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
          <span matListItemTitle>{{ item.label }}</span>
        </a>
      }
    </mat-nav-list>
  `,
  styles: [`
    .nav-list { padding-top: 8px; }
    .nav-active { background: rgba(63, 81, 181, .1) !important; border-radius: 4px; }
    .nav-active mat-icon { color: #3f51b5; }
  `],
})
export class NavComponent {
  navigate = output<void>();

  readonly navItems: NavItem[] = [
    { label: 'Dashboard',      icon: 'dashboard',           route: '/dashboard' },
    { label: 'Persons',        icon: 'people',              route: '/persons' },
    { label: 'Organizations',  icon: 'business',            route: '/organizations' },
    { label: 'Membership',     icon: 'card_membership',     route: '/membership' },
    { label: 'Governance',     icon: 'gavel',               route: '/governance' },
    { label: 'Volunteers',     icon: 'volunteer_activism',  route: '/volunteers' },
    { label: 'Events',         icon: 'event',               route: '/events' },
    { label: 'Finance',        icon: 'account_balance',     route: '/finance' },
  ];
}
