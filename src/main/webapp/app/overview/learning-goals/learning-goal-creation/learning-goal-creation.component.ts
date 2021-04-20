import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { LearningGoalsService } from '../learning-goals.service';
import { ILearningGoalModel, INewLearningGoalModel } from '../../shared/learning-goal-model';
import { isNil } from 'lodash';

/**
 * Component for creating a new learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-creation',
  templateUrl: './learning-goal-creation.component.html',
  styleUrls: ['./learning-goal-creation.component.scss'],
})
export class LearningGoalCreationComponent {
  @Output()
  public learningGoalCreated = new EventEmitter<void>();

  @Output()
  public learningGoalUpdated = new EventEmitter<void>();

  @Output()
  public subGoalCreated = new EventEmitter<void>();

  @Input()
  public loggedInUser!: string;

  public learningGoalForm = this.fb.group({
    learningGoalName: ['', [Validators.required]],
    learningGoalDescription: [''],
    privateGoal: [false],
    needVerification: [false],
  });

  private _learningGoal?: ILearningGoalModel;
  private _parentGoal?: string;

  /**
   * Constructor.
   *
   * @param fb the injected form builder
   * @param learningGoalsService the injected learning goals service
   */
  constructor(private fb: FormBuilder, private learningGoalsService: LearningGoalsService) {}

  /**
   * Resets the form.
   */
  public reset(): void {
    this.learningGoalForm.patchValue({
      learningGoalName: '',
      learningGoalDescription: '',
      privateGoal: false,
      needVerification: false,
    });
  }

  /**
   * Saves the newly created learning goal.
   */
  public save(): void {
    if (this.isNewLearningGoal() && !this.isParentGoalSet()) {
      const newGoal: INewLearningGoalModel = {
        name: this.learningGoalForm.get(['learningGoalName'])!.value,
        description: this.learningGoalForm.get(['learningGoalDescription'])!.value,
        privateGoal: this.learningGoalForm.get(['privateGoal'])!.value,
        needVerification: this.learningGoalForm.get(['needVerification'])!.value,
      };

      this.learningGoalsService.postNewLearningGoal(newGoal).subscribe(() => {
        this.reset();
        this.learningGoalCreated.emit();
      });
    } else if (!this.isParentGoalSet()) {
      // Update
      const goal = { ...this._learningGoal } as ILearningGoalModel;
      goal.name = this.learningGoalForm.get(['learningGoalName'])!.value;
      goal.description = this.learningGoalForm.get(['learningGoalDescription'])!.value;
      goal.privateGoal = this.learningGoalForm.get(['privateGoal'])!.value;
      goal.needVerification = this.learningGoalForm.get(['needVerification'])!.value;

      this.learningGoalsService.updateLearningGoal(goal).subscribe(() => {
        this.learningGoal = undefined;
        this.learningGoalUpdated.emit();
      });
    } else {
      // New sub goal created
      const newGoal: INewLearningGoalModel = {
        name: this.learningGoalForm.get(['learningGoalName'])!.value,
        description: this.learningGoalForm.get(['learningGoalDescription'])!.value,
        privateGoal: this.learningGoalForm.get(['privateGoal'])!.value,
        needVerification: this.learningGoalForm.get(['needVerification'])!.value,
      };

      this.learningGoalsService.createSubGoal(newGoal, this.parentGoal!, this.loggedInUser).subscribe(() => {
        this.reset();
        this.parentGoal = undefined;
        this.subGoalCreated.emit();
      });
    }
  }

  /**
   * Returns whether a new learning goal should be created or edited.
   *
   * @returns {@code true} if the component is in creation mode, otherwise {@code false}
   */
  public isNewLearningGoal(): boolean {
    return isNil(this._learningGoal);
  }

  /**
   * Returns whether the parent goal is set or not.
   *
   * @returns {@code true} if the parent goal is set, otherwise {@code false}
   */
  public isParentGoalSet(): boolean {
    return !isNil(this._parentGoal);
  }

  /**
   * Sets the current learning goal.
   *
   * @param value the new learning goal model to set
   */
  @Input()
  public set learningGoal(value: ILearningGoalModel | undefined) {
    this._learningGoal = value;

    if (isNil(value)) {
      this.reset();
    } else {
      this.learningGoalForm.patchValue({
        learningGoalName: value.name,
        learningGoalDescription: value.description,
        privateGoal: value.privateGoal,
        needVerification: value.needVerification,
      });
    }
  }

  /**
   * Returns the current learning goal model or {@code null} if a new learning goal
   * should be created.
   *
   * @returns the current learning goal model or {@code null}
   */
  public get learningGoal(): ILearningGoalModel | undefined {
    return this._learningGoal;
  }

  /**
   * Sets the parent goal id.
   *
   * @param value the parent goal id to set
   */
  @Input()
  public set parentGoal(value: string | undefined) {
    this._parentGoal = value;
  }

  /**
   * Returns the parent goal name or {@code null}, when no parent id is set!
   *
   * @returns the parent goal name or {@code null}
   */
  public get parentGoal(): string | undefined {
    return this._parentGoal;
  }

  /**
   * Method which handles a sub goal creation request.
   *
   * @param parentGoalId the id of the parent goal
   */
  public subGoalCreationRequest(parentGoalId: string): void {
    this.learningGoal = undefined;
    this.parentGoal = parentGoalId;
  }

  /**
   * Clears this control.
   */
  public clear(): void {
    this._learningGoal = undefined;
    this._parentGoal = undefined;
    this.learningGoalForm.patchValue({
      learningGoalName: '',
      learningGoalDescription: '',
      privateGoal: false,
      needVerification: false,
    });
  }
}
