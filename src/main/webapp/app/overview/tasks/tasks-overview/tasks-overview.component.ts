import { Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ITaskDisplayModel } from './task.model';
import { ITEMS_PER_PAGE } from '../../../shared/constants/pagination.constants';
import { TasksService } from '../tasks.service';

/**
 * Component which provides an overview of the tasks.
 */
@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './tasks-overview.component.html',
  styleUrls: ['./tasks-overview.component.scss'],
})
export class TasksOverviewComponent implements OnInit {
  private itemsPerPage: number;

  public hasNextPage = false;
  public page = 0;
  public entries: ITaskDisplayModel[] = [];
  public filterString = '';

  /**
   * Constructor.
   *
   * @param tasksService the injected tasks service
   */
  constructor(private tasksService: TasksService) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.loadEntries();
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
  public createNewTask(): void {}

  /**
   * Performs the filter operation and
   * calls the REST endpoint.
   */
  public performFiltering(): void {
    let wordSearch = this.filterString;

    setTimeout(() => {
      if (wordSearch === this.filterString) {
        this.entries.length = 0;
        this.loadPage(0);
      }
    }, 500);
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
        this.filterString
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
