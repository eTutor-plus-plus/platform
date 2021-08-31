import { Component, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators } from '@angular/forms';
import { IExerciseSheetDTO, ILearningGoalAssignmentDTO, INewExerciseSheetDTO } from '../exercise-sheets.model';
import { CustomValidators } from 'app/shared/validators/custom-validators';
import { TaskDifficulty } from '../../tasks/task.model';
import { ExerciseSheetsService } from '../exercise-sheets.service';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { LearningGoalsService } from '../../learning-goals/learning-goals.service';
import { AccountService } from 'app/core/auth/account.service';
import { EventManager } from 'app/core/util/event-manager.service';
import { ExerciseSheetContextMenuComponent } from '../exercise-sheet-context-menu/exercise-sheet-context-menu.component';

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

  public isContextMenuShown = false;
  public contextMenuPositionX = -1;
  public contextMenuPositionY = -1;

  public learningGoals: LearningGoalTreeviewItem[] = [];
  public selectedGoals: string[] = [];
  public selectedPriorities: [string, number][] = [];

  public currentPriority = -1;

  public updateForm = this.fb.group({
    name: ['', [CustomValidators.required]],
    difficulty: [this.difficulties[0], [Validators.required]],
    taskCount: [1, [Validators.required, Validators.min(1)]],
    generateWholeExerciseSheet: [false],
  });

  private _exerciseSheet?: IExerciseSheetDTO;
  private _selectedGoals: ILearningGoalAssignmentDTO[] = [];
  private _loginName: string;
  private _currentContextMenuGoalId = '';
  private _contextMenuComponent?: ExerciseSheetContextMenuComponent;

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
   * Ses the current context menu component.
   *
   * @param value the component to set
   */
  @ViewChild(ExerciseSheetContextMenuComponent, { static: false })
  public set contextMenuComponent(value: ExerciseSheetContextMenuComponent | undefined) {
    this._contextMenuComponent = value;
  }

  /**
   * Returns the current context menu component.
   */
  public get contextMenuComponent(): ExerciseSheetContextMenuComponent | undefined {
    return this._contextMenuComponent;
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
        learningGoals: this._selectedGoals.map(x => {
          const val = this.selectedPriorities.find(y => y[0] === x.learningGoal.id);
          const priority = val ? val[1] : x.priority;

          return {
            priority,
            learningGoal: x.learningGoal,
          };
        }),
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
        learningGoals: this._selectedGoals.map(x => {
          const val = this.selectedPriorities.find(y => y[0] === x.learningGoal.id);
          const priority = val ? val[1] : x.priority;

          return {
            priority,
            learningGoal: x.learningGoal,
          };
        }),
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
          learningGoal: {
            name: item.text,
            id: item.value,
          },
          priority: 1,
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

      this.selectedPriorities = value.learningGoals.map(x => [x.learningGoal.id, x.priority]);

      this._selectedGoals = value.learningGoals;
      this.selectedGoals = this._selectedGoals.map(x => x.learningGoal.id);
    }
  }

  /**
   * Returns the exercise sheet.
   */
  public get exerciseSheet(): IExerciseSheetDTO | undefined {
    return this._exerciseSheet;
  }

  /**
   * Handles any document click (used for hiding the context menu panel)
   */
  @HostListener('document:click', ['$event'])
  public documentClick(event: PointerEvent): void {
    /* eslint-disable no-console */
    if (this.isContextMenuShown) {
      const clientRect = this.contextMenuComponent!.clientRect!;
      const containsPanel =
        event.clientX >= clientRect.left &&
        event.clientX <= clientRect.right &&
        event.clientY >= clientRect.top &&
        event.clientY <= clientRect.bottom;

      if (!containsPanel) {
        this.prioritySelectionCancel();
        console.log('Cancel');
      }

      console.log(event);
      console.log(this.contextMenuComponent);
      console.log(JSON.stringify(this.contextMenuComponent?.clientRect));
      /* eslint-enable no-console */
    }
  }

  /**
   * Handles the priority selection event.
   *
   * @param priority the selected priority.
   */
  public prioritySelected(priority: number): void {
    if (priority <= 0) {
      this.prioritySelectionCancel();
      return;
    }

    const element = this.selectedPriorities.find(x => this._currentContextMenuGoalId === x[0]);
    if (element) {
      element[1] = priority;
    } else {
      this.selectedPriorities.push([this._currentContextMenuGoalId, priority]);
    }

    this.isContextMenuShown = false;
  }

  /**
   * Handles the selection panel cancel event.
   */
  public prioritySelectionCancel(): void {
    this.isContextMenuShown = false;
  }

  /**
   * Opens the context menu on the current position (from the given mouse event).
   *
   * @param event the mouse event
   * @param item the current tree view item
   */
  public displayPriorityContextMenu(event: MouseEvent, item: LearningGoalTreeviewItem): void {
    if (this.selectedGoals.indexOf(item.value) >= 0) {
      this._currentContextMenuGoalId = item.value;
      this.contextMenuPositionX = event.clientX;
      this.contextMenuPositionY = event.clientY;

      const element = this.selectedPriorities.find(x => x[0] === item.value);

      if (element) {
        this.currentPriority = element[1];
      } else {
        this.currentPriority = 1;
      }

      this.isContextMenuShown = true;
    }
  }

  /**
   * Returns the context menu's style.
   */
  public getContextMenuStyle(): { [p: string]: any } {
    return {
      position: 'fixed',
      left: `${this.contextMenuPositionX}px`,
      top: `${this.contextMenuPositionY}px`,
      'background-color': 'white',
      border: 'black',
    };
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
