import { Component, Input } from '@angular/core';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { Router } from '@angular/router';
import { ITaskAssignmentDisplay } from '../../tasks/task.model';

/**
 * Component for displaying the current selected learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-display',
  templateUrl: './learning-goal-display.component.html',
  styleUrls: ['./learning-goal-display.component.scss'],
})
export class LearningGoalDisplayComponent {
  @Input()
  public learningGoalTreeviewItem?: LearningGoalTreeviewItem;
  @Input()
  public assignments: ITaskAssignmentDisplay[] = [];
  @Input()
  public dependencies: string[] = [];

  /**
   * Constructor.
   *
   * @param router the injected routing service
   */
  constructor(private router: Router) {}

  /**
   * Navigates to the assignment overview.
   *
   * @param assignment the name of the assignment
   */
  public navigateToAssignment(assignment: ITaskAssignmentDisplay): void {
    const id = assignment.id.substr(assignment.id.lastIndexOf('#') + 1);

    this.router.navigate(['overview', 'tasks'], {
      queryParams: {
        selectedTaskAssignment: id,
      },
    });
  }
}
