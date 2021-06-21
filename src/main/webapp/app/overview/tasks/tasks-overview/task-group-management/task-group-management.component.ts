import { Component } from '@angular/core';
import {TasksService} from "app/overview/tasks/tasks.service";

/**
 * Component for managing task groups.
 */
@Component({
  selector: 'jhi-task-group-management',
  templateUrl: './task-group-management.component.html',
})
export class TaskGroupManagementComponent {
  /**
   * Constructor.
   *
   * @param taskService the injected tasks service
   */
  constructor(private taskService: TasksService) {}
}
