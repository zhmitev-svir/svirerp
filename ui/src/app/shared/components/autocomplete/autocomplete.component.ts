import {
  Component, DestroyRef, ChangeDetectionStrategy, OnInit,
  inject, input, signal, output, effect,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ControlValueAccessor, FormControl, NgControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Observable, of, timer } from 'rxjs';
import { debounce, switchMap } from 'rxjs/operators';

/**
 * Generic server-search typeahead field — drop-in replacement for a `mat-select` bound to a
 * fully-preloaded list (the pattern copied into most forms today, see PersonService#search /
 * VolunteerFormComponent for the first real usage). Implements ControlValueAccessor so it binds
 * via `formControlName` exactly like `mat-select` does — including showing its own validation
 * error, via the `NgControl self`-injection pattern (not the `NG_VALUE_ACCESSOR` provider token,
 * which would create a circular-DI error the moment a component both provides that token *and*
 * injects `NgControl` for itself — this is the same pattern Angular Material's own `mat-select`
 * uses internally).
 *
 * The caller owns what "search" and "display" mean — `searchFn` is wired to a backend endpoint
 * (e.g. `personService.search('firstName', q)`), `displayFn` formats a result row, and `valueFn`
 * (defaults to `.id`) is what actually gets written into the bound form control.
 *
 * `initialLabel` exists because ControlValueAccessor#writeValue only ever receives the raw id, not
 * the full object — there's no reverse lookup here. Callers that already hold the full entity
 * (edit-mode prefill, or a just-created record from a quick-add dialog) pass its display text via
 * `[initialLabel]` so the field shows something meaningful instead of a blank box.
 */
@Component({
  selector: 'app-autocomplete',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatAutocompleteModule],
  template: `
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>{{ label() }}</mat-label>
      <input matInput
             [formControl]="searchControl"
             [matAutocomplete]="auto"
             [placeholder]="placeholder()"
             (blur)="onTouched()" />
      <mat-autocomplete #auto="matAutocomplete" (optionSelected)="onOptionSelected($event)">
        @for (item of results(); track $index) {
          <mat-option [value]="item">{{ displayFn()(item) }}</mat-option>
        }
      </mat-autocomplete>
      @if (ngControl?.invalid && ngControl?.touched) {
        <mat-error>{{ errorText() }}</mat-error>
      }
    </mat-form-field>
  `,
  styles: [`
    .full-width { width: 100%; }
  `],
})
export class AutocompleteComponent<T> implements ControlValueAccessor, OnInit {
  private destroyRef = inject(DestroyRef);
  // Not private: read from the template to show this control's own validation error (see class docs).
  protected ngControl = inject(NgControl, { optional: true, self: true });

  label = input.required<string>();
  searchFn = input.required<(query: string) => Observable<T[]>>();
  displayFn = input.required<(item: T) => string>();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  valueFn = input<(item: T) => string>((item: any) => item?.id);
  placeholder = input<string>('');
  minLength = input<number>(4);
  debounceMs = input<number>(300);
  initialLabel = input<string>('');
  errorText = input<string>('Required');

  selectionChange = output<T | null>();

  searchControl = new FormControl('', { nonNullable: true });
  results = signal<T[]>([]);

  private onChange: (value: string | null) => void = () => {};
  onTouched: () => void = () => {};

  constructor() {
    // Manually wire ourselves as the accessor instead of the NG_VALUE_ACCESSOR provider token —
    // see class docs for why (avoids a circular-DI error from also injecting NgControl above).
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }

    // Reflects an externally-supplied display label (edit-mode prefill, quick-create result) into
    // the visible text — writeValue() alone can't do this, see class docs.
    effect(() => {
      const label = this.initialLabel();
      if (label) {
        this.searchControl.setValue(label, { emitEvent: false });
      }
    });
  }

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(
        debounce(() => timer(this.debounceMs())),
        switchMap(text => {
          if (!text || text.length < this.minLength()) {
            return of([] as T[]);
          }
          return this.searchFn()(text);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(items => this.results.set(items));
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const item = event.option.value as T;
    this.searchControl.setValue(this.displayFn()(item), { emitEvent: false });
    this.onChange(this.valueFn()(item));
    this.selectionChange.emit(item);
  }

  writeValue(value: string | null): void {
    // Only clear on an empty/reset incoming value — a real id with nothing to display for it just
    // leaves whatever text is already showing (trusting [initialLabel] to have set it).
    if (!value) {
      this.searchControl.setValue('', { emitEvent: false });
    }
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.searchControl.disable({ emitEvent: false });
    } else {
      this.searchControl.enable({ emitEvent: false });
    }
  }
}
