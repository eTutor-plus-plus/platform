import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { IExerciseSheetDTO, INewExerciseSheetDTO } from '../exercise-sheets.model';
import { CustomValidators } from 'app/shared/validators/custom-validators';
import { TaskDifficulty } from '../../tasks/task.model';
import { ExerciseSheetsService } from '../exercise-sheets.service';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { LearningGoalsService } from '../../learning-goals/learning-goals.service';
import { ILearningGoalDisplayModel } from '../../shared/learning-goal-model';
import { AccountService } from 'app/core/auth/account.service';
import { EventManager } from 'app/core/util/event-manager.service';

/**
 * Modal component which is used for creating / editing an exercise sheet.
 */
@Component({
  selector: 'jhi-exercise-sheet-update',
  templateUrl: './exercise-sheet-update.component.html',
  styleUrls: ['./exercise-sheet-update.component.scss'],
})
export class ExerciseSheetUpdateComponent implements OnInit {
  public isSaving = false;
  public isNew = true;
  public readonly difficulties = TaskDifficulty.Values;

  public learningGoals: LearningGoalTreeviewItem[] = [];
  public selectedGoals: string[] = [];

  public updateForm = this.fb.group({
    name: ['', [CustomValidators.required]],
    difficulty: [this.difficulties[0], [Validators.required]],
    taskCount: [1, [Validators.required, Validators.min(1)]],
    generateWholeExerciseSheet: [false],
  });

  private _exerciseSheet?: IExerciseSheetDTO;
  private _selectedGoals: ILearningGoalDisplayModel[] = [];
  private _loginName: string;

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   * @param fb the injected form builder
   * @param eventManager the injected event manager
   * @param exerciseSheetService the injected exercise sheet service
   * @param learningGoalsService the injected learning goals service
   * @param accountService the injected account service
   */
  constructor(
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private eventManager: EventManager,
    private exerciseSheetService: ExerciseSheetsService,
    private learningGoalsService: LearningGoalsService,
    private accountService: AccountService
  ) {
    this._loginName = this.accountService.getLoginName()!;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this._loginName).subscribe(values => (this.learningGoals = values));
  }

  /**
   * Closes the active modal.
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Saves the data
   */
  public save(): void {
    this.isSaving = true;
    const difficultyId = (this.updateForm.get(['difficulty'])!.value as TaskDifficulty).value;

    if (this.isNew) {
      const newExerciseSheet: INewExerciseSheetDTO = {
        name: (this.updateForm.get(['name'])!.value as string).trim(),
        difficultyId,
        learningGoals: this._selectedGoals,
        taskCount: this.updateForm.get(['taskCount'])!.value,
        generateWholeExerciseSheet: this.updateForm.get(['generateWholeExerciseSheet'])!.value,
      };

      this.exerciseSheetService.insertExerciseSheet(newExerciseSheet).subscribe(
        () => this.onSaveSuccess(),
        () => this.onSaveError()
      );
    } else {
      const exerciseSheet: IExerciseSheetDTO = {
        name: (this.updateForm.get(['name'])!.value as string).trim(),
        difficultyId,
        learningGoals: this._selectedGoals,
        creationDate: this.exerciseSheet!.creationDate,
        internalCreator: this.exerciseSheet!.internalCreator,
        id: this.exerciseSheet!.id,
        taskCount: this.updateForm.get(['taskCount'])!.value,
        generateWholeExerciseSheet: this.updateForm.get(['generateWholeExerciseSheet'])!.value,
      };

      this.exerciseSheetService.updateExerciseSheet(exerciseSheet).subscribe(
        () => this.onSaveSuccess(),
        () => this.onSaveError()
      );
    }
  }

  /**
   * Handles the click events and selects or deselects items.
   *
   * @param event the mouse event
   * @param item the item to select or deselect
   */
  public handleGoalClicked(event: MouseEvent, item: LearningGoalTreeviewItem): void {
    const idx = this.selectedGoals.indexOf(item.value);

    if (event.ctrlKey) {
      if (idx > -1) {
        this.selectedGoals.splice(idx, 1);
        this._selectedGoals.splice(idx, 1);
      }
    } else {
      if (idx === -1) {
        this.selectedGoals.push(item.value);
        this._selectedGoals.push({
          name: item.text,
          id: item.value,
        });
      } else {
        this.selectedGoals.splice(idx, 1);
        this._selectedGoals.splice(idx, 1);
      }
    }
  }

  /**
   * Sets the exercise sheet.
   *
   * @param value the value to set
   */
  @Input()
  public set exerciseSheet(value: IExerciseSheetDTO | undefined) {
    this._exerciseSheet = value;

    if (value) {
      this.isNew = false;

      const difficulty = TaskDifficulty.fromString(value.difficultyId)!;

      this.updateForm.patchValue({
        name: value.name,
        difficulty,
        taskCount: value.taskCount,
        generateWholeExerciseSheet: value.generateWholeExerciseSheet,
      });

      this._selectedGoals = value.learningGoals;
      this.selectedGoals = this._selectedGoals.map(x => x.id);
    }
  }

  /**
   * Returns the exercise sheet.
   */
  public get exerciseSheet(): IExerciseSheetDTO | undefined {
    return this._exerciseSheet;
  }

  /**
   * Handles the on save success.
   */
  private onSaveSuccess(): void {
    this.eventManager.broadcast('exercise-sheets-changed');
    this.isSaving = false;
    this.activeModal.close();
  }

  /**
   * Handles the on save error.
   */
  private onSaveError(): void {
    this.isSaving = false;
  }
}
