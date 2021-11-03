import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ITaskDisplayModel } from '../task.model';
import { TasksService } from '../tasks.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TaskUpdateComponent } from './task-update/task-update.component';
import { Subscription } from 'rxjs';
import { TaskAssignmentUpdateComponent } from './task-assignment-update/task-assignment-update.component';
import { TaskDisplayComponent } from './task-display/task-display.component';
import { TranslatePipe } from '@ngx-translate/core';
import { AccountService } from 'app/core/auth/account.service';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { EventManager } from 'app/core/util/event-manager.service';
import { ITEMS_PER_SLICE } from 'app/config/pagination.constants';
import { TaskGroupManagementComponent } from 'app/overview/tasks/tasks-overview/task-group-management/task-group-management.component';
import { SqlExerciseService } from '../../dispatcher/services/sql-exercise.service';

/**
 * Component which provides an overview of the tasks.
 */
@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './tasks-overview.component.html',
  styleUrls: ['./tasks-overview.component.scss'],
  providers: [TranslatePipe],
})
export class TasksOverviewComponent implements OnInit, OnDestroy {
  public singleEntryDisplay = false;

  public hasNextPage = false;
  public page = 0;
  public entries: ITaskDisplayModel[] = [];
  public filterString = '';
  public taskGroupFilterString = '';

  public popoverTitle = 'taskManagement.popover.title';
  public popoverMessage = 'taskManagement.popover.message';
  public popoverCancelButtonTxt = 'taskManagement.popover.cancelBtn';
  public popoverConfirmBtnTxt = 'taskManagement.popover.confirmBtn';

  private itemsPerPage: number;
  private subscription?: Subscription;
  private routingSubscription?: Subscription;
  private userLogin = '';

  /**
   * Constructor.
   *
   * @param tasksService the injected tasks service
   * @param modalService the injected modal service
   * @param eventManager the injected event manager service
   * @param translatePipe the injected translation pipe
   * @param accountService the injected account service
   * @param activatedRoute the injected activated route
   * @param router the injected routing service
   */
  constructor(
    private tasksService: TasksService,
    private sqlExerciseService: SqlExerciseService,
    private modalService: NgbModal,
    private eventManager: EventManager,
    private translatePipe: TranslatePipe,
    private accountService: AccountService,
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {
    this.itemsPerPage = ITEMS_PER_SLICE;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.userLogin = this.accountService.getLoginName() ?? '';
    this.popoverTitle = this.translatePipe.transform(this.popoverTitle);
    this.popoverMessage = this.translatePipe.transform(this.popoverMessage);
    this.popoverCancelButtonTxt = this.translatePipe.transform(this.popoverCancelButtonTxt);
    this.popoverConfirmBtnTxt = this.translatePipe.transform(this.popoverConfirmBtnTxt);

    this.subscription = this.eventManager.subscribe('taskModification', () => {
      this.entries.length = 0;
      this.loadPage(0);
    });

    this.routingSubscription = this.activatedRoute.queryParams.subscribe((queryParams: Params) => {
      if (queryParams['selectedTaskAssignment']) {
        const id = queryParams['selectedTaskAssignment'];

        this.tasksService.getTaskAssignmentById(id, true).subscribe(response => {
          if (response.body) {
            this.entries.length = 0;
            this.entries.push({
              taskId: response.body.id,
              privateTask: response.body.privateTask,
              header: response.body.header,
              internalCreator: response.body.internalCreator,
            });
            this.singleEntryDisplay = true;
          }
        });
      } else {
        this.filterString = '';
        this.taskGroupFilterString = '';
        this.entries.length = 0;
        this.singleEntryDisplay = false;
        this.loadPage(0);
      }
    });
  }

  /**
   * Resets the single entry view.
   */
  public reset(): void {
    this.router.navigate(['overview', 'tasks']);
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  public ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.routingSubscription) {
      this.routingSubscription.unsubscribe();
    }
  }

  /**
   * Loads the given page (will be appended to the current entries).
   *
   * @param page the page to load
   */
  public loadPage(page: number): void {
    this.page = page;
    this.loadEntries();
  }

  /**
   * Track identity function for a task display model.
   *
   * @param index the current index
   * @param item the current selected task display model item
   */
  public trackId(index: number, item: ITaskDisplayModel): string {
    return item.taskId;
  }

  /**
   * Opens the new task creation window.
   */
  public createNewTask(): void {
    this.modalService.open(TaskUpdateComponent, { size: 'lg', backdrop: 'static' });
  }

  /**
   * Opens the edit task modal window for the given selected task model.
   *
   * @param selectedModel the selected task model
   */
  public editTask(selectedModel: ITaskDisplayModel): void {
    this.tasksService.getTaskAssignmentById(selectedModel.taskId).subscribe(value => {
      const modalRef = this.modalService.open(TaskUpdateComponent, { size: 'lg', backdrop: 'static' });
      (modalRef.componentInstance as TaskUpdateComponent).taskModel = value.body!;
    });
  }

  /**
   * Opens the edit learning goal assignment modal window for the given selected task model.
   *
   * @param selectedModel the selected task model
   */
  public editLearningGoalAssignments(selectedModel: ITaskDisplayModel): void {
    const modalRef = this.modalService.open(TaskAssignmentUpdateComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as TaskAssignmentUpdateComponent).taskDisplayModel = selectedModel;
  }

  /**
   * Opens the given task model in a modal window.
   *
   * @param selectedModel the selected task model
   */
  public displayTask(selectedModel: ITaskDisplayModel): void {
    const modalRef = this.modalService.open(TaskDisplayComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as TaskDisplayComponent).taskDisplayModel = selectedModel;
  }

  /**
   * Performs the filter operation and
   * calls the REST endpoint.
   */
  public performFiltering(): void {
    const wordSearch = this.filterString;

    setTimeout(() => {
      if (wordSearch === this.filterString) {
        this.entries.length = 0;
        this.loadPage(0);
      }
    }, 500);
  }

  /**
   * Performs the task group filtering operation
   * and calls the REST endpoint.
   */
  public performTaskGroupFiltering(): void {
    const wordSearch = this.taskGroupFilterString;

    setTimeout(() => {
      if (wordSearch === this.taskGroupFilterString) {
        this.entries.length = 0;
        this.loadPage(0);
      }
    }, 500);
  }

  /**
   * Returns whether the currently logged-in user is allowed to edit the given task or not.
   *
   * @param currentModel the current task model
   */
  public isCurrentUserAllowedToEdit(currentModel: ITaskDisplayModel): boolean {
    return currentModel.internalCreator === this.userLogin;
  }

  /**
   * Deletes the given task assignment.
   *
   * @param currentModel the current task model
   */
  public deleteAssignment(currentModel: ITaskDisplayModel): void {
    this.tasksService.deleteAssignment(currentModel.taskId).subscribe(() => {
      this.entries.length = 0;
      this.loadPage(0);
    });
  }

  /**
   * Opens the manage task groups modal.
   */
  public manageTaskGroups(): void {
    this.modalService.open(TaskGroupManagementComponent, { backdrop: 'static', size: 'xl' });
  }

  // region Private helper methods
  /**
   * Loads the entries and performs the paging.
   */
  private loadEntries(): void {
    this.tasksService
      .queryTaskDisplayList(
        {
          page: this.page,
          size: this.itemsPerPage,
        },
        this.filterString,
        this.taskGroupFilterString
      )
      .subscribe((res: HttpResponse<ITaskDisplayModel[]>) => this.paginate(res.body, res.headers));
  }

  /**
   * Performs the pagination.
   *
   * @param data the entries from the rest endpoint
   * @param headers the header of the http endpoint request
   */
  private paginate(data: ITaskDisplayModel[] | null, headers: HttpHeaders): void {
    this.hasNextPage = headers.get('X-Has-Next-Page') === 'true';

    if (data) {
      this.entries.push(...data);
    }
  }

  // endregion
}
