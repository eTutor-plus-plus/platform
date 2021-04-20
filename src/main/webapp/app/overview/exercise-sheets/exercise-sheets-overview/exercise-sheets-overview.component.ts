import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ExerciseSheetUpdateComponent } from '../exercise-sheet-update/exercise-sheet-update.component';
import { Subscription } from 'rxjs';
import { ExerciseSheetsService } from '../exercise-sheets.service';
import { IExerciseSheetDisplayDTO } from '../exercise-sheets.model';
import { HttpHeaders } from '@angular/common/http';
import { TranslatePipe } from '@ngx-translate/core';
import { EventManager } from 'app/core/util/event-manager.service';
import { ITEMS_PER_SLICE } from 'app/config/pagination.constants';
import { Pagination } from 'app/core/request/request.model';

/**
 * Component for displaying the exercise sheets.
 */
@Component({
  selector: 'jhi-exercise-sheets-overview',
  templateUrl: './exercise-sheets-overview.component.html',
  styleUrls: ['./exercise-sheets-overview.component.scss'],
  providers: [TranslatePipe],
})
export class ExerciseSheetsOverviewComponent implements OnInit {
  public hasNextPage = false;
  public page = 0;
  public entries: IExerciseSheetDisplayDTO[] = [];
  public filterString = '';

  public popoverTitle = 'exerciseSheets.popover.title';
  public popoverMessage = 'exerciseSheets.popover.message';
  public popoverCancelButtonTxt = 'exerciseSheets.popover.cancelBtn';
  public popoverConfirmBtnTxt = 'exerciseSheets.popover.confirmBtn';

  private exerciseSubscription?: Subscription;
  private readonly itemsPerPage: number;

  /**
   * Constructor.
   *
   * @param modalService the injected modal service
   * @param eventManager the injected event manager service
   * @param exerciseSheetsService the injected exercise sheets service
   * @param translationPipe the injected translation pipe
   */
  constructor(
    private modalService: NgbModal,
    private eventManager: EventManager,
    private exerciseSheetsService: ExerciseSheetsService,
    private translationPipe: TranslatePipe
  ) {
    this.itemsPerPage = ITEMS_PER_SLICE;

    this.popoverTitle = this.translationPipe.transform(this.popoverTitle);
    this.popoverMessage = this.translationPipe.transform(this.popoverMessage);
    this.popoverCancelButtonTxt = this.translationPipe.transform(this.popoverCancelButtonTxt);
    this.popoverConfirmBtnTxt = this.translationPipe.transform(this.popoverConfirmBtnTxt);
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
   * Opens the edit dialog for the given exercise sheet.
   *
   * @param exerciseSheet the exercise sheet to edit
   */
  public editExerciseSheet(exerciseSheet: IExerciseSheetDisplayDTO): void {
    this.editExerciseSheetAsync(exerciseSheet);
  }

  /**
   * Deletes the given exercise sheet.
   *
   * @param exerciseSheet the exercise sheet to remove
   */
  public deleteExerciseSheet(exerciseSheet: IExerciseSheetDisplayDTO): void {
    this.exerciseSheetsService.deleteExerciseSheetById(exerciseSheet.internalId).subscribe(() => {
      this.eventManager.broadcast('exercise-sheets-changed');
    });
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

  /**
   * Asynchronously opens the edit dialog.
   *
   * @param exerciseSheetDisplay the exercise sheet to edit
   */
  private async editExerciseSheetAsync(exerciseSheetDisplay: IExerciseSheetDisplayDTO): Promise<any> {
    const exerciseSheetResponse = await this.exerciseSheetsService.getExerciseSheetById(exerciseSheetDisplay.internalId).toPromise();
    const exerciseSheet = exerciseSheetResponse.body!;
    const modalRef = this.modalService.open(ExerciseSheetUpdateComponent, { size: 'lg', backdrop: 'static' });
    (modalRef.componentInstance as ExerciseSheetUpdateComponent).exerciseSheet = exerciseSheet;
  }
}
