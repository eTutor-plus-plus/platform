import { Component, Input, OnInit } from '@angular/core';
import { LearningGoalTreeviewItem } from '../learning-goal-treeview-item.model';

/**
 * Component for displaying the current selected learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-display',
  templateUrl: './learning-goal-display.component.html',
  styleUrls: ['./learning-goal-display.component.scss'],
})
export class LearningGoalDisplayComponent implements OnInit {
  @Input()
  public learningGoalTreeviewItem?: LearningGoalTreeviewItem;

  /**
   * Constructor
   */
  constructor() {}

  /**
   * Implements the on init method. See {@link OnInit}
   */
  public ngOnInit(): void {}
}
