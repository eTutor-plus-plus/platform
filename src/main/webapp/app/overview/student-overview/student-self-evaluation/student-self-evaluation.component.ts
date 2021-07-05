import { Component, OnInit } from '@angular/core';
import { StudentSelfEvaluationService } from './student-self-evaluation.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ICourseInstanceInformationDTO } from '../../shared/students/students.model';
import { CourseManagementService } from '../../course-management/course-management.service';
import { AccountService } from 'app/core/auth/account.service';
import { IStudentSelfEvaluationLearningGoal, IStudentSelfEvaluationLearningGoalWithReference } from './student-self-evaluation.model';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';

// noinspection JSIgnoredPromiseFromCall
/**
 * Component for the student self evaluation.
 */
@Component({
  selector: 'jhi-student-self-evaluation',
  templateUrl: './student-self-evaluation.component.html',
})
export class StudentSelfEvaluationComponent implements OnInit {
  public goals: IStudentSelfEvaluationLearningGoalWithReference[] = [];

  public studentSelfEvaluationForm = this.fb.group({
    learningObjectives: new FormArray([]),
  });

  public isSaving = false;

  private readonly _instance?: ICourseInstanceInformationDTO;
  private _login: string;

  /**
   * Constructor.
   *
   * @param studentSelfEvaluationService the injected student self evaluation service
   * @param activatedRoute the injected activated route service
   * @param router the injected router
   * @param courseManagementService the injected course management service
   * @param accountService the injected account service
   * @param fb the injected reactive form builder
   */
  constructor(
    private studentSelfEvaluationService: StudentSelfEvaluationService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private courseManagementService: CourseManagementService,
    private accountService: AccountService,
    private fb: FormBuilder
  ) {
    this._login = this.accountService.getLoginName()!;
    const nav = this.router.getCurrentNavigation();
    if (nav?.extras.state) {
      this._instance = nav.extras.state.instance;
    } else {
      this.router.navigate(['/']);
    }
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.loadLearningGoalsAsync();
  }

  /**
   * Submits the changes.
   */
  public submitChanges(): void {
    this.isSaving = true;

    const goals: IStudentSelfEvaluationLearningGoal[] = [];

    for (const formGroup of this.formArrayControlsAsFormGroup) {
      const id: string = formGroup.get('id')!.value;
      const text: string = formGroup.get('text')!.value;
      const completed: boolean = formGroup.get('completed')!.value;

      goals.push({
        id,
        text,
        completed,
      });
    }

    this.studentSelfEvaluationService.saveEvaluation(this.courseInstanceInfo.instanceId, goals).subscribe(
      () => {
        this.isSaving = false;
        this.navigateBack();
      },
      () => {
        this.isSaving = false;
      }
    );
  }

  /**
   * Navigates back.
   */
  public navigateBack(): void {
    this.router.navigate(['/', 'overview']);
  }

  /**
   * Returns the controls as form array.
   */
  public get controlsAsFormArray(): FormArray {
    return this.studentSelfEvaluationForm.get('learningObjectives')! as FormArray;
  }

  /**
   * Returns the controls as a form group.
   */
  public get formArrayControlsAsFormGroup(): FormGroup[] {
    return this.controlsAsFormArray.controls as FormGroup[];
  }

  /**
   * Returns the course instance information.
   */
  public get courseInstanceInfo(): ICourseInstanceInformationDTO {
    return this._instance!;
  }

  /**
   * Marks the selected goal (and all its sub goals) as reached.
   *
   * @param goal the selected goal
   */
  public markGoalAsReachedClicked(goal: IStudentSelfEvaluationLearningGoalWithReference): void {
    this.markGoalAsReachedRecursive(goal);
  }

  /**
   * Marks the selected goal (and all its super goals) as not reached.
   *
   * @param goal the selected goal
   */
  public markGoalAsNotReachedClicked(goal: IStudentSelfEvaluationLearningGoalWithReference): void {
    this.markGoalAsUnreachedRecursive(goal);
  }

  /**
   * Recursively marks the goal and its sub goals as reached.
   *
   * @param goal the goal which should be marked as reached
   */
  private markGoalAsReachedRecursive(goal: IStudentSelfEvaluationLearningGoalWithReference): void {
    goal.completed = true;
    goal.group.patchValue({
      completed: true,
    });

    for (let i = 0; i < goal.subGoals.length; i++) {
      this.markGoalAsReachedRecursive(goal.subGoals[i]);
    }
  }

  /**
   * Recursively
   *
   * @param goal the goal which should be marked as unreached
   */
  private markGoalAsUnreachedRecursive(goal: IStudentSelfEvaluationLearningGoalWithReference): void {
    goal.completed = false;
    goal.group.patchValue({
      completed: false,
    });
    if (goal.parentGoal) {
      this.markGoalAsUnreachedRecursive(goal.parentGoal);
    }
  }

  /**
   * Asynchronously loads the learning goals and initializes the
   * dynamic form.
   */
  private async loadLearningGoalsAsync(): Promise<void> {
    if (this._instance) {
      const goals = await this.courseManagementService.getLearningGoalsFromCourse(this._instance.courseName, this._login).toPromise();
      const formArray = this.controlsAsFormArray;

      for (const goal of goals) {
        this.getLearningGoalsRecursive(goal, formArray, undefined);
      }
    }
  }

  /**
   * Recursively goes through the given learning goal tree
   * and adds the current goal to the list of available goals.
   *
   * @param goal the root goal
   * @param formArray the form array
   * @param parentGoal the optional parent goal
   */
  private getLearningGoalsRecursive(
    goal: LearningGoalTreeviewItem,
    formArray: FormArray,
    parentGoal?: IStudentSelfEvaluationLearningGoalWithReference
  ): IStudentSelfEvaluationLearningGoalWithReference {
    const group = this.fb.group({
      id: [goal.value],
      text: [goal.text],
      completed: [false],
    });

    formArray.push(group);

    const newGoal: IStudentSelfEvaluationLearningGoalWithReference = {
      id: goal.value,
      text: goal.text,
      completed: false,
      subGoals: [],
      parentGoal,
      group,
    };
    this.goals.push(newGoal);

    for (const child of goal.childItems) {
      const childGoal = this.getLearningGoalsRecursive(child, formArray, newGoal);
      newGoal.subGoals.push(childGoal);
    }

    return newGoal;
  }
}
