import { Component, OnInit } from '@angular/core';

/**
 * Component for displaying the current selected learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-display',
  templateUrl: './learning-goal-display.component.html',
  styleUrls: ['./learning-goal-display.component.scss']
})
export class LearningGoalDisplayComponent implements OnInit {

  /**
   * Constructor
   */
  constructor() { }

  /**
   * Implements the on init method. See {@link OnInit}
   */
  public ngOnInit(): void {
  }
}
