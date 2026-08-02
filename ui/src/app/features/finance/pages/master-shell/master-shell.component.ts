import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

/** Master/reference data — Projects & Funds and Categories, grouped under one Finance tab so the
 *  top-level tab strip doesn't grow every time a new reference-data screen is added. */
@Component({
  selector: 'app-master-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, MatTabsModule],
  template: `
    <nav mat-tab-nav-bar [tabPanel]="tabPanel" class="master-tabs">
      <a mat-tab-link routerLink="projects" routerLinkActive #projectsActive="routerLinkActive"
         [active]="projectsActive.isActive">
        Projects &amp; Funds
      </a>
      <a mat-tab-link routerLink="categories" routerLinkActive #categoriesActive="routerLinkActive"
         [active]="categoriesActive.isActive">
        Categories
      </a>
    </nav>
    <mat-tab-nav-panel #tabPanel>
      <router-outlet />
    </mat-tab-nav-panel>
  `,
  styles: [`
    .master-tabs { margin-bottom: 8px; }
  `],
})
export class MasterShellComponent {}
