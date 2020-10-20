import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import { FormBuilder, Validators } from "@angular/forms";
import { LearningGoalsService } from "../learning-goals.service";
import { INewLearningGoalModel } from "../learning-goal-model";

/**
 * Component for creating a new learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-creation',
  templateUrl: './learning-goal-creation.component.html',
  styleUrls: ['./learning-goal-creation.component.scss']
})
export class LearningGoalCreationComponent implements OnInit {

  @Output()
  public learningGoalCreated = new EventEmitter<void>();

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
  }
}
