import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'svirerp.nav-collapsed';

/** Whether the left-hand nav is showing icons only — shared between ShellComponent (owns the
 *  sidenav width/toggle button) and NavComponent (hides item labels), and remembered for the
 *  browser session (sessionStorage, not localStorage — a fresh browser session starts expanded). */
@Injectable({ providedIn: 'root' })
export class NavCollapseService {
  private readonly _collapsed = signal(this.readInitial());
  readonly collapsed = this._collapsed.asReadonly();

  toggle(): void {
    this.set(!this._collapsed());
  }

  set(value: boolean): void {
    this._collapsed.set(value);
    try {
      sessionStorage.setItem(STORAGE_KEY, String(value));
    } catch {
      // sessionStorage can throw in some private-browsing contexts — losing the preference for
      // this session is harmless, not worth surfacing to the user.
    }
  }

  private readInitial(): boolean {
    try {
      return sessionStorage.getItem(STORAGE_KEY) === 'true';
    } catch {
      return false;
    }
  }
}
