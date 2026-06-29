import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';

import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { NavComponent } from '../nav/nav.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterOutlet,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    NavComponent,
  ],
  template: `
    <mat-sidenav-container class="shell-container">

      <mat-sidenav
        #sidenav
        [mode]="isMobile() ? 'over' : 'side'"
        [opened]="!isMobile()"
        class="shell-sidenav">

        <div class="brand-header">
          <mat-icon class="brand-icon">church</mat-icon>
          <span class="brand-name">SvirERP</span>
        </div>

        <app-nav (navigate)="isMobile() && sidenav.close()" />
      </mat-sidenav>

      <mat-sidenav-content class="shell-content">
        <mat-toolbar color="primary" class="shell-toolbar">
          <button mat-icon-button (click)="sidenav.toggle()" aria-label="Toggle navigation">
            <mat-icon>menu</mat-icon>
          </button>
          <span class="toolbar-app-name">SvirERP</span>
        </mat-toolbar>

        <main class="shell-main">
          <router-outlet />
        </main>
      </mat-sidenav-content>

    </mat-sidenav-container>
  `,
  styles: [`
    .shell-container { height: 100vh; }
    .shell-sidenav { width: 240px; border-right: 1px solid rgba(0,0,0,.12); }
    .brand-header {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 16px;
      border-bottom: 1px solid rgba(0,0,0,.08);
    }
    .brand-icon { color: #3f51b5; font-size: 28px; }
    .brand-name { font-size: 20px; font-weight: 500; }
    .shell-toolbar { position: sticky; top: 0; z-index: 10; }
    .toolbar-app-name { margin-left: 8px; font-size: 18px; }
    .shell-main { padding: 0; min-height: calc(100vh - 64px); }
  `],
})
export class ShellComponent {
  private breakpoint = inject(BreakpointObserver);

  isMobile = toSignal(
    this.breakpoint.observe(Breakpoints.Handset).pipe(map(r => r.matches)),
    { initialValue: false },
  );
}
