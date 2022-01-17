import { AfterContentChecked, Component, EventEmitter, Input, Output } from '@angular/core';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { AssignmentService } from 'app/overview/dispatcher/services/assignment.service';

/**
 * Component for handling an Assignment which has to be evaluated by the dispatcher
 */

@Component({
  selector: 'jhi-assignment',
  templateUrl: './assignment.component.html',
  styleUrls: ['./assignment.component.scss'],
})
export class AssignmentComponent implements AfterContentChecked {
  /**
   * The diagnose levels that can be chosen by the student
   */
  public diagnoseLevels = [
    'dispatcherAssignment.assignment.diagnoseLevel.none',
    'dispatcherAssignment.assignment.diagnoseLevel.little',
    'dispatcherAssignment.assignment.diagnoseLevel.some',
    'dispatcherAssignment.assignment.diagnoseLevel.much',
  ];
  /**
   * The id of the exercise
   */
  @Input() public exercise_id: string | undefined;
  /**
   * The task type {@see TaskAssignmentType}
   */
  @Input() public task_type: string | undefined;
  /**
   * The optional, stored submission
   */
  @Input() public submission: string | undefined;
  /**
   * The highest-diagnose level that has been chosen so far
   */
  @Input() public highestDiagnoseLevel!: number;
  /**
   * The points that have been achieved
   */
  @Input() public points!: number;
  /**
   * The maximum points for this exercise
   */
  @Input() public maxPoints = '';
  /**
   * The weighting of the diagnose level used to calculate the achieved points
   */
  @Input() public diagnoseLevelWeighting = '';
  /**
   * Flag indicating if the points should be displayed
   */
  @Input() public showPoints = true;
  /**
   * Flag indicating if the diagnose-option should be displayed
   */
  @Input() public showDiagnoseBar = true;
  /**
   * Flag indicating if the submit-option should be displayed
   */
  @Input() public showSubmitButton = true;
  /**
   * Output that notifies components if the submission has been sent to the dispatcher and the UUID for the submission has been received
   */
  @Output() public submissionUUIDReceived: EventEmitter<string> = new EventEmitter<string>();

  public diagnoseLevelText = '';
  /**
   * Indicates if a {@link GradingDTO} grading has been received
   */
  public gradingReceived = false;
  /**
   * Indicates if the submission contained errors according to the grading
   */
  public hasErrors = true;
  /**
   * The {@link GradingDTO} wrapping information about the graded submission
   */
  public gradingDto!: GradingDTO;
  /**
   * The default editor options
   */
  public editorOptions = { theme: 'vs-dark', language: '' };

  /**
   * Indicates the task type
   */
  public isXQueryTask = false;
  public isDatalogTask = false;

  /**
   *
   * @private
   */
  /**
   * The current diagnose level
   * @private
   */
  private diagnoseLevel = 0;
  /**
   * the {@link SubmissionDTO} wrapping all the information required by the dispatcher for evaluation
   * @private
   */
  private submissionDto!: SubmissionDTO;
  /**
   * Wraps the ID of the SubmissionDTO, needed to request the grading
   * @private
   */
  private submissionIdDto!: SubmissionIdDTO;
  private action!: string;

  /**
   * The constructor
   * @param assignmentService service for communicating with the dispatcher
   */
  constructor(private assignmentService: AssignmentService) {}

  /**
   * Maps the task type to the language for the editor
   * @param taskType the task type
   */
  public static mapEditorLanguage(taskType: string): string {
    switch (taskType) {
      case 'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#SQLTask':
        return 'pgsql';
      case 'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#RATask':
        return 'relationalAlgebra';
      case 'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#DLGTask':
        return 'datalog';
      case 'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#XQTask':
        return 'xquery';
      default:
        return 'pgsql';
    }
  }
  /**
   * Maps the diagnose level to the text representation
   * @param number the diagnose level as number
   */
  public mapDiagnoseLevel(number: number): string {
    switch (number) {
      case 0:
        return this.diagnoseLevels[0];
      case 1:
        return this.diagnoseLevels[1];
      case 2:
        return this.diagnoseLevels[2];
      case 3:
        return this.diagnoseLevels[3];
      default:
        return this.diagnoseLevels[0];
    }
  }

  /**
   * Lifecycle method that sets the language for the editor in accordance to the {@link task_type},
   * and the diagnose-level in the dropdown according to the {@link highestDiagnoseLevel}
   */
  public ngAfterContentChecked(): void {
    if (!this.editorOptions.language && this.task_type) {
      const lang = AssignmentComponent.mapEditorLanguage(this.task_type);
      let the = 'vs-dark';
      if (lang === 'relationalAlgebra') {
        the = 'relationalAlgebra-light';
      } else if (lang === 'xquery') {
        this.isXQueryTask = true;
        the = 'xquery-light';
      } else if (lang === 'datalog') {
        this.isDatalogTask = true;
        the = 'datalog-light';
      }

      this.editorOptions = { theme: the, language: lang };
    }
    if (!this.diagnoseLevelText && this.highestDiagnoseLevel) {
      this.diagnoseLevelText = this.mapDiagnoseLevel(this.highestDiagnoseLevel);
    }
  }

  /**
   * Handles a click on the diagnose Button
   */
  public onDiagnose(): void {
    this.action = 'diagnose';
    this.processSubmission();
  }

  /**
   * Handles a click on the submit Button
   */
  public onSubmit(): void {
    this.action = 'submit';
    this.processSubmission();
  }

  /**
   * Creates a SubmissionDTO and uses the {@link assignmentService} to send it to the dispatcher
   *
   */
  private processSubmission(): void {
    this.diagnoseLevel = this.mapDiagnoseText(this.diagnoseLevelText);
    const submissionDto = this.initializeSubmissionDTO();

    this.assignmentService.postSubmission(submissionDto).subscribe(submissionId => {
      this.submissionIdDto = submissionId;
      this.submissionDto.submissionId = submissionId.submissionId;
      setTimeout(() => {
        this.getGrading();
      }, 2000);
    });
  }

  /**
   * Requests the {@link GradingDTO} for the {@link submissionIdDto} using the {@link assignmentService}
   */
  private getGrading(): void {
    this.assignmentService.getGrading(this.submissionIdDto).subscribe(grading => {
      this.gradingDto = grading;
      this.gradingReceived = true;

      this.setHasErrors();
      this.emitSubmissionEvents();
    });
  }

  /**
   * Verifies if the submission has been evaluated as correct by the dispatcher
   * and sets {@link hasErrors} accordingly
   * @private
   */
  private setHasErrors(): void {
    this.gradingDto.maxPoints > this.gradingDto.points || this.gradingDto.maxPoints === 0
      ? (this.hasErrors = true)
      : (this.hasErrors = false);
  }

  /**
   * Emits the events in the course of a submission,
   * namely adjusting the {@link highestDiagnoseLevel} if needed and emitting the submission UUID {@see submissionUUIDReceived}
   * @private
   */
  private emitSubmissionEvents(): void {
    if (this.diagnoseLevel > this.highestDiagnoseLevel && this.action !== 'submit' && this.points === 0) {
      this.highestDiagnoseLevel = this.diagnoseLevel;
    }

    if (this.submissionIdDto.submissionId) {
      this.submissionUUIDReceived.emit(this.submissionIdDto.submissionId);
    }

    if (this.points === 0 && !this.hasErrors && this.action === 'submit') {
      const grading = this.calculatePoints();
      this.points = grading;
    }
  }

  /**
   * Calculates the achieved points with regards to the max-points,
   * the highest chosen diagnose level and the weighting of the diagnose level.
   * @private
   */
  private calculatePoints(): number {
    const points = parseInt(this.maxPoints, 10) - this.highestDiagnoseLevel * parseInt(this.diagnoseLevelWeighting, 10);
    if (points > 1) {
      return points;
    }
    return 1;
  }
  /**
   * Initializes the {@link SubmissionDTO} as required by the dispatcher
   * @private
   */
  private initializeSubmissionDTO(): SubmissionDTO {
    const attributes = new Map<string, string>();
    attributes.set('action', this.action);
    attributes.set('submission', this.submission ?? '');
    attributes.set('diagnoseLevel', this.diagnoseLevel.toFixed());

    const jsonAttributes: { [k: string]: any } = {};
    attributes.forEach((key, value) => {
      jsonAttributes[value] = key;
    });

    const submissionDto: SubmissionDTO = {
      submissionId: '',
      exerciseId: this.exercise_id ?? '',
      passedAttributes: jsonAttributes,
      taskType: this.task_type ?? '',
      passedParameters: new Map<string, string>(),
    };
    this.submissionDto = submissionDto;

    return submissionDto;
  }

  /**
   * Maps the diagnose level text to its numeric representation
   * @param text
   * @private
   */
  private mapDiagnoseText(text: string): number {
    switch (text) {
      case this.diagnoseLevels[0]:
        return 0;
      case this.diagnoseLevels[1]:
        return 1;
      case this.diagnoseLevels[2]:
        return 2;
      case this.diagnoseLevels[3]:
        return 3;
      default:
        return 0;
    }
  }
}
