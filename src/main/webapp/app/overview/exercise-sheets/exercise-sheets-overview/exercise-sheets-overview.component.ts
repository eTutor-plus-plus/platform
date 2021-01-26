import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { ExerciseSheetUpdateComponent } from '../exercise-sheet-update/exercise-sheet-update.component';
import { Subscription } from 'rxjs';
import { ExerciseSheetsService } from '../exercise-sheets.service';
import { IExerciseSheetDisplayDTO } from '../exercise-sheets.model';
import { ITEMS_PER_SLICE } from '../../../shared/constants/pagination.constants';
import { HttpHeaders } from '@angular/common/http';
import { Pagination } from '../../../shared/util/request-util';

/**
 * Component for displaying the exercise sheets.
 */
@Component({
  selector: 'jhi-exercise-sheets-overview',
  templateUrl: './exercise-sheets-overview.component.html',
  styleUrls: ['./exercise-sheets-overview.component.scss'],
})
export class ExerciseSheetsOverviewComponent implements OnInit {
  private exerciseSubscription?: Subscription;
  private itemsPerPage: number;

  public hasNextPage = false;
  public page = 0;
  public entries: IExerciseSheetDisplayDTO[] = [];
  public filterString = '';

  /**
   * Constructor.
   *
   * @param modalService the injected modal service
   * @param eventManager the injected event manager service
   * @param exerciseSheetsService the injected exercise sheets service
   */
  constructor(private modalService: NgbModal, private eventManager: JhiEventManager, private exerciseSheetsService: ExerciseSheetsService) {
    this.itemsPerPage = ITEMS_PER_SLICE;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.exerciseSubscription = this.eventManager.subscribe('exercise-sheets-changed', () => {
      this.entries.length = 0;
      this.loadPage(0);
    });
    this.loadPage(0);
  }

  /**
   * Opens the create new exercise sheet modal window.
   */
  public createNewExerciseSheet(): void {
    this.modalService.open(ExerciseSheetUpdateComponent, { size: 'lg', backdrop: 'static' });
  }

  /**
   * Performs the filter operation.
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
   * Track identity function for a exercise sheet display dto.
   * @param index the current index
   * @param item the current selected
   */
  public trackId(index: number, item: IExerciseSheetDisplayDTO): string {
    return item.internalId;
  }

  /**
   * Loads the data from the given page.
   *
   * @param page the page
   */
  public loadPage(page: number): void {
    this.page = page;
    this.loadAsync();
  }

  /**
   * Performs the pagination.
   *
   * @param data the entries from the rest endpoint
   * @param headers the header of the http endpoint request
   */
  private paginate(data: IExerciseSheetDisplayDTO[] | null, headers: HttpHeaders): void {
    this.hasNextPage = headers.get('X-Has-Next-Page') === 'true';

    if (data) {
      this.entries.push(...data);
    }
  }

  /**
   * Asynchronously loads the entries.
   */
  private async loadAsync(): Promise<void> {
    const pagination: Pagination = {
      page: this.page,
      size: this.itemsPerPage,
      sort: [],
    };
    const response = await this.exerciseSheetsService.getExerciseSheetPage(pagination, this.filterString).toPromise();
    this.paginate(response.body, response.headers);
  }
}
