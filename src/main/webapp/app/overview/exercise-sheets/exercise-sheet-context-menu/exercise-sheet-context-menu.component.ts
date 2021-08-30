import { Component, OnInit } from '@angular/core';

/**
 * Component which is used for displaying a context menu panel that allows
 * the user to assign priorites to selected learning goals.
 */
@Component({
  selector: 'jhi-exercise-sheet-context-menu',
  templateUrl: './exercise-sheet-context-menu.component.html',
  styleUrls: ['./exercise-sheet-context-menu.component.scss'],
})
export class ExerciseSheetContextMenuComponent implements OnInit {
  /**
   * Constructor.
   */
  constructor() {}

  /**
   * Handles the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}
}
