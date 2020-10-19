import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import { LearningGoalsService } from "../learning-goals.service";

/**
 * Component for creating a new learning goal.
 */
@Component({
  selector: 'jhi-learning-goal-creation',
  templateUrl: './learning-goal-creation.component.html',
  styleUrls: ['./learning-goal-creation.component.scss']
})
export class LearningGoalCreationComponent implements OnInit {

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

    this.reset();
  }
}
