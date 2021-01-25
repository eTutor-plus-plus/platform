import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { ExerciseSheetUpdateComponent } from '../exercise-sheet-update/exercise-sheet-update.component';
import { Subscription } from 'rxjs';
import { ExerciseSheetsService } from '../exercise-sheets.service';

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

  /**
   * Constructor.
   *
   * @param modalService the injected modal service
   * @param eventManager the injected event manager service
   * @param exerciseSheetsService the injected exercise sheets service
   */
  constructor(
    private modalService: NgbModal,
    private eventManager: JhiEventManager,
    private exerciseSheetsService: ExerciseSheetsService
  ) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.exerciseSubscription = this.eventManager.subscribe('exercise-sheets-changed', () => this.loadExerciseSheets());
  }

  /**
   * Opens the create new exercise sheet modal window.
   */
  public createNewExerciseSheet(): void {
    this.modalService.open(ExerciseSheetUpdateComponent, { size: 'lg', backdrop: 'static' });
  }

  /**
   * Loads the exercise sheets.
   */
  private loadExerciseSheets(): void {}
}
