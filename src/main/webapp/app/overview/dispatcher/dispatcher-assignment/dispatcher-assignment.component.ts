import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { DispatcherAssignmentService } from 'app/overview/dispatcher/services/dispatcher-assignment.service';
import { getEditorOptionsForTaskTypeUrl, MonacoEditorOptions, switchModeOfEditorConfig } from '../monaco-config';
import { TaskAssignmentType } from '../../tasks/task.model';

/**
 * Component for handling an Assignment which has to be evaluated by the dispatcher
 */

@Component({
  selector: 'jhi-dispatcher-assignment',
  templateUrl: './dispatcher-assignment.component.html',
  styleUrls: ['./dispatcher-assignment.component.scss'],
})
export class DispatcherAssignmentComponent {
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
  public _task_type = '';
  @Input() set task_type(value: string) {
    this._task_type = value;
    this.editorOptions = getEditorOptionsForTaskTypeUrl(value, false);
  }
  /**
   * The optional, stored submission
   */
  @Input() public submission: string | undefined;
  /**
   * The highest-diagnose level that has been chosen so far
   */
  public _highestChosenDiagnoseLevel = 0;
  @Input() set highestDiagnoseLevel(value: number) {
    this._highestChosenDiagnoseLevel = value;
    this.diagnoseLevelText = this.diagnoseLevels[value];
  }
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

  public diagnoseLevelText = this.diagnoseLevels[0];

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
  public editorOptions: MonacoEditorOptions = { theme: 'vs-light', language: 'pgsql', readOnly: false };

  public isXQueryTask = false;
  public isDatalogTask = false;
  public isBpmnTask = false;

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
  constructor(private assignmentService: DispatcherAssignmentService) {}

  /**
   * Creates a SubmissionDTO and uses the {@link assignmentService} to send it to the dispatcher
   *
   */
  public processSubmission(action = 'diagnose'): void {
    this.action = action;
    if (this.diagnoseLevels.includes(this.diagnoseLevelText)) {
      this.diagnoseLevel = this.diagnoseLevels.indexOf(this.diagnoseLevelText);
    } else {
      this.diagnoseLevel = 0;
    }
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
      this.setTaskTypeFlags();
      this.hasErrors = !grading.submissionSuitsSolution;
      this.emitSubmissionEvents();
    });
  }

  /**
   * Emits the events in the course of a submission,
   * namely adjusting the {@link highestDiagnoseLevel} if needed and emitting the submission UUID {@see submissionUUIDReceived}
   * @private
   */
  private emitSubmissionEvents(): void {
    if (this.submissionIdDto.submissionId) {
      !this.submissionIdDto.isBpmnTask
        ? this.submissionUUIDReceived.emit(this.submissionIdDto.submissionId)
        : this.submissionUUIDReceived.emit(this.submissionIdDto.submissionId + '#BpmnTask');
    }
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
      taskType: this._task_type,
      passedParameters: new Map<string, string>(),
      maxPoints: this.maxPoints,
    };
    this.submissionDto = submissionDto;

    return submissionDto;
  }

  /**
   * Helper methods identifying the current task by the task_type field.
   */
  private setTaskTypeFlags(): void {
    this.isXQueryTask = this._task_type === TaskAssignmentType.XQueryTask.value;
    this.isDatalogTask = this._task_type === TaskAssignmentType.DatalogTask.value;
    this.isBpmnTask = this._task_type === TaskAssignmentType.BpmnTask.value;
  }

  switchEditorMode() {
    const newOptions = switchModeOfEditorConfig(this.editorOptions);
    this.editorOptions = switchModeOfEditorConfig(this.editorOptions);
  }
}
