import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, Validators } from "@angular/forms";
import { LearningGoalsService } from "../learning-goals.service";
import { ILearningGoalModel, INewLearningGoalModel } from "../learning-goal-model";
import { isNil } from 'lodash';

/**
 * Component for creating a new learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-creation',
  templateUrl: './learning-goal-creation.component.html',
  styleUrls: ['./learning-goal-creation.component.scss']
})
export class LearningGoalCreationComponent implements OnInit {

  private _learningGoal?: ILearningGoalModel;

  @Output()
  public learningGoalCreated = new EventEmitter<void>();

  @Output()
  public learningGoalUpdated = new EventEmitter<void>();

  public learningGoalForm = this.fb.group({
    learningGoalName: ['', [Validators.required]],
    learningGoalDescription: [''],
    privateGoal: [false]
  });

  /**
   * Constructor.
   *
   * @param fb the injected form builder
   * @param learningGoalsService the injected learning goals service
   */
  constructor(private fb: FormBuilder, private learningGoalsService: LearningGoalsService) {
  }

  /**
   * Implements the on init method. See {@link OnInit}
   */
  public ngOnInit(): void {
  }

  /**
   * Resets the form.
   */
  public reset(): void {
    this.learningGoalForm.patchValue({
      learningGoalName: '',
      learningGoalDescription: '',
      privateGoal: false
    });
  }

  /**
   * Saves the newly created learning goal.
   */
  public save(): void {
    if (this.isNewLearningGoal()) {
      const newGoal: INewLearningGoalModel = {
        name: this.learningGoalForm.get(['learningGoalName'])!.value,
        description: this.learningGoalForm.get(['learningGoalDescription'])!.value,
        privateGoal: this.learningGoalForm.get(['privateGoal'])!.value
      };

      this.learningGoalsService.postNewLearningGoal(newGoal)
        .subscribe(() => {
          this.reset();
          this.learningGoalCreated.emit();
        });
    } else { // Update
      const goal = {...this._learningGoal} as ILearningGoalModel;
      goal.name = this.learningGoalForm.get(['learningGoalName'])!.value;
      goal.description = this.learningGoalForm.get(['learningGoalDescription'])!.value;
      goal.privateGoal = this.learningGoalForm.get(['privateGoal'])!.value;

      this.learningGoalsService.updateLearningGoal(goal).subscribe(() => {
        this.learningGoal = undefined;
        this.learningGoalUpdated.emit();
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
        privateGoal: value.privateGoal
      });
    }
  }

  /**
   * Returns the current learning goal model, or {@code null} if a new learning goal
   * should be created.
   */
  public get learningGoal(): ILearningGoalModel | undefined {
    return this._learningGoal;
  }
}
