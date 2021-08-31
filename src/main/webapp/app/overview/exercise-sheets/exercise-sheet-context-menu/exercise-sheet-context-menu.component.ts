import { Component, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';

/**
 * Component which is used for displaying a context menu panel that allows
 * the user to assign priorities to selected learning goals.
 */
@Component({
  selector: 'jhi-exercise-sheet-context-menu',
  templateUrl: './exercise-sheet-context-menu.component.html',
  styleUrls: ['./exercise-sheet-context-menu.component.scss'],
})
export class ExerciseSheetContextMenuComponent {
  @Output()
  public selectionSaved: EventEmitter<number> = new EventEmitter<number>();
  @Output()
  public selectionCancel: EventEmitter<any> = new EventEmitter<any>();

  public possiblePriorities: number[] = [];

  @ViewChild('mainContainer')
  public mainRef?: ElementRef;

  private _priority = -1;

  /**
   * Returns the priority.
   */
  public get priority(): number {
    return this._priority;
  }

  /**
   * Sets the priority.
   *
   * @param value the priority value to set
   */
  @Input()
  public set priority(value: number) {
    this._priority = value;
  }

  /**
   * Returns whether or not a priority has been selected.
   */
  public get isPrioritySelected(): boolean {
    return this.priority >= 0;
  }

  /**
   * Performs the saving (notifies the listeners).
   */
  public save(): void {
    this.selectionSaved.emit(this.priority);
  }

  /**
   * Performs the cancel operation (notifies the listeners).
   */
  public cancel(): void {
    this.selectionCancel.emit();
  }

  /**
   * Returns the client's bounding rectangle.
   */
  public get clientRect(): ClientRect | undefined {
    return this.mainRef?.nativeElement?.getBoundingClientRect() as ClientRect | undefined;
  }

  @HostListener('document:keydown.escape', ['$event'])
  public onKeydownHandler(event: KeyboardEvent): void {
    event.preventDefault();
    this.cancel();
  }
}
