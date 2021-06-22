// noinspection JSIgnoredPromiseFromCall

import { Component, OnInit } from '@angular/core';
import { TasksService } from 'app/overview/tasks/tasks.service';
import { TaskGroupManagementService } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.service';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ITaskGroupDisplayDTO } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.model';
import { COUNT_HEADER, ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { TaskGroupUpdateComponent } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-update/task-group-update.component';

/**
 * Component for managing task groups.
 */
@Component({
  selector: 'jhi-task-group-management',
  templateUrl: './task-group-management.component.html',
})
export class TaskGroupManagementComponent implements OnInit {
  public page = 1;
  public readonly itemsPerPage: number;
  public totalItems = 0;
  public taskGroups: ITaskGroupDisplayDTO[] = [];
  public query = '';

  /**
   * Constructor.
   *
   * @param taskService the injected tasks service
   * @param taskGroupService the injected task group service
   * @param activeModal the injected active modal service
   * @param modalService the injected modal service
   */
  constructor(
    private taskService: TasksService,
    private taskGroupService: TaskGroupManagementService,
    private activeModal: NgbActiveModal,
    private modalService: NgbModal
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.loadPageAsync();
  }

  /**
   * Closes the modal  dialog.
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Opens the create task group modal window.
   */
  public createNewTaskGroup(): void {
    const modalRef = this.modalService.open(TaskGroupUpdateComponent, { backdrop: 'static', size: 'xl' });
    modalRef.result.then(() => {
      this.page = 1;
      this.query = '';
      this.transition();
    });
  }

  /**
   * Returns the identity id from the given item.
   *
   * @param index the index
   * @param item the item
   */
  public trackIdentity(index: number, item: ITaskGroupDisplayDTO): string {
    return item.id;
  }

  /**
   * Loads the currently selected page.
   */
  public transition(): void {
    this.loadPageAsync();
  }

  /**
   * Asynchronously loads the current page.
   */
  private async loadPageAsync(): Promise<any> {
    const response = await this.taskGroupService
      .getPagedTaskGroups(
        {
          page: this.page - 1,
          size: this.itemsPerPage,
          sort: [],
        },
        this.query
      )
      .toPromise();
    this.totalItems = Number(response.headers.get(COUNT_HEADER));
    this.taskGroups = response.body ?? [];
  }
}
