import { Component } from '@angular/core';
import { TasksService } from 'app/overview/tasks/tasks.service';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';

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
   * @param taskGroupService the injected task group service
   */
  constructor(private taskService: TasksService, private taskGroupService: TaskGroupManagementService) {}
}
