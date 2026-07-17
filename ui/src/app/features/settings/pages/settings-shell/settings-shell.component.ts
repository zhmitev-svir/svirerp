import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-settings-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, MatTabsModule],
  template: `
    <nav mat-tab-nav-bar [tabPanel]="tabPanel" class="settings-tabs">
      <a mat-tab-link routerLink="general" routerLinkActive #generalActive="routerLinkActive"
         [active]="generalActive.isActive">
        General
      </a>
      <a mat-tab-link routerLink="organization" routerLinkActive #orgActive="routerLinkActive"
         [active]="orgActive.isActive">
        Organization
      </a>
      <a mat-tab-link routerLink="gmail" routerLinkActive #gmailActive="routerLinkActive"
         [active]="gmailActive.isActive">
        Gmail
      </a>
    </nav>
    <mat-tab-nav-panel #tabPanel>
      <router-outlet />
    </mat-tab-nav-panel>
  `,
  styles: [`
    .settings-tabs { margin-bottom: 8px; }
  `],
})
export class SettingsShellComponent {}
