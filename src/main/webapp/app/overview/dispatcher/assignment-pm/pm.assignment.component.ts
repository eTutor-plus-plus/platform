import { AfterContentChecked, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { FormBuilder } from '@angular/forms';
import { DispatcherAssignmentService } from '../services/dispatcher-assignment.service';

/**
 * Component for handling an PmTask Assignment which has to be evaluated by the dispatcher
 * @author Falk Görner
 * @version 1.0
 */

@Component({
  selector: 'jhi-pm-assignment',
  templateUrl: './pm.assignment.component.html',
  styleUrls: ['./pm.assignment.component.scss'],
})
export class PmAssignmentComponent implements OnInit {
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
   * The optional, stored submission of the ordering relations
   * public orI2: string  | undefined;
   */
  //public orI1= "";
  public orI2 = '';
  public orI3 = '';

  /**
   * The optional, stored submission of the alpha algorithm
   */
  public aaI1 = '';
  public aaI2 = '';
  public aaI3 = '';
  public aaI4 = '';
  public aaI5 = '';
  public aaI6 = '';
  public aaI7 = '';

  /**
   * The optional, stored submission
   */
  public _submission = '';
  @Input() set submission(value: string) {
    this._submission = value;
    this.getSubmissionFromJson();
  }

  /**
   * The highest-diagnose level that has been chosen so far
   * Cannot be null or undefined
   */
  public _highestDiagnoseLevel = 0;
  @Input() set highestDiagnoseLevel(value: number) {
    this._highestDiagnoseLevel = value;
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
   * The identifier for identifying a certain task
   * Needed to get the right event log
   */
  @Input() public exerciseSheetUUID!: string;
  @Input() public taskNo!: number;
  @Input() public courseInstanceUUID!: string;

  @Input() public isSubmitted = false;

  /**
   * Output that notifies components id the submission has been sent to the dispatcher
   * and the UUID for the submission has been received
   * Output: CHILD -> PARENT
   */
  @Output() public submissionUUIDReceived: EventEmitter<string> = new EventEmitter<string>();
  // @Output() public orI1Change = new EventEmitter<string>();

  public diagnoseLevelText = '';

  /**
   * Indicates if a {@link GradingDTO} grading has been received
   */
  public gradingReceived = false;

  /**
   * The {@link GradingDTO} wrapping information about the graded submission
   */
  public gradingDTO!: GradingDTO;

  /**
   * The default editor options
   */
  // public editorOptions = {theme: 'vs-dark', language: ''};

  // added on 29.11 // readonly?
  public updateForm = this.fb.group({
    orI1: [''],
  });

  /**
   * The log corresponding to the respective dispatcherID {@link dispatcherExerciseID}
   * @private
   */
  public log!: string[][];
  private dispatcherExerciseID!: number;

  /**
   * The current diagnose level
   */
  private diagnoseLevel = 0;

  /**
   * The {@link SubmissionDTO} wrapping all the information required by the dispatcher for the evaluation
   */
  private submissionDTO!: SubmissionDTO;

  /**
   * Wraps the ID of the SubmissionDTO, needed to request the grading
   */
  private submissionIdDTO!: SubmissionIdDTO;

  /**
   * Part of submissionDTO
   * either diagnose or submission
   * used to differentiate between both
   * @private
   */
  private action!: string;

  /**
   * The constructor
   * @param assignmentService service for communicating with the dispatcher
   */
  constructor(
    private assignmentService: DispatcherAssignmentService,
    // added on 29.11
    private fb: FormBuilder
  ) {}

  public ngOnInit(): void {
    this.assignmentService.getPmLogForIndividualTask(this.courseInstanceUUID, this.exerciseSheetUUID, this.taskNo).subscribe(DTO => {
      this.log = DTO.log;
      this.dispatcherExerciseID = DTO.exerciseId;
    });
  }

  /**
   * Creates a SubmissionDTO and uses the {@link assignmentService} to send it to the dispatcher
   */
  public processSubmission(action: string): void {
    this.action = action;
    this.diagnoseLevel = this.diagnoseLevels.indexOf(this.diagnoseLevelText);
    const submissionDTO = this.initializeSubmissionDTO();

    this.assignmentService.postSubmission(submissionDTO).subscribe(submissionId => {
      this.submissionIdDTO = submissionId;
      this.submissionDTO.submissionId = submissionId.submissionId;
      setTimeout(() => {
        this.getGrading();
      }, 2000);
    });
  }

  /**
   * Initializes the {@link SubmissionDTO} as required by the dispatcher
   * sends a map of attributes to the dispatcher as required
   * this.orI2 ?? ''
   * @private
   */
  private initializeSubmissionDTO(): SubmissionDTO {
    const attributes = new Map<string, string>();
    attributes.set('action', this.action);
    // attributes.set('orI1', this.orI1);
    //attributes.set('orI1', this.updateForm.get('orI1')!.value)
    attributes.set('orI1', this.updateForm.controls['orI1'].value ?? '');
    attributes.set('orI2', this.orI2);
    attributes.set('orI3', this.orI3);

    attributes.set('aaI1', this.aaI1);
    attributes.set('aaI2', this.aaI2);
    attributes.set('aaI3', this.aaI3);
    attributes.set('aaI4', this.aaI4);
    attributes.set('aaI5', this.aaI5);
    attributes.set('aaI6', this.aaI6);
    attributes.set('aaI7', this.aaI7);
    const diagnoseLevel = this.diagnoseLevel ? this.diagnoseLevel.toFixed() : '0';
    attributes.set('diagnoseLevel', diagnoseLevel);
    attributes.set('isPmTask', 'true');
    const exerciseId = this.dispatcherExerciseID ? this.dispatcherExerciseID.toFixed() : '';
    attributes.set('exerciseId', exerciseId);

    const jsonAttributes: { [k: string]: any } = {};
    attributes.forEach((key, value) => {
      jsonAttributes[value] = key;
    });

    const submissionDTO: SubmissionDTO = {
      submissionId: '',
      exerciseId: this.exercise_id ?? '',
      passedAttributes: jsonAttributes,
      taskType: this.task_type ?? '',
      passedParameters: new Map<string, string>(),
      maxPoints: this.maxPoints,
    };

    this.submissionDTO = submissionDTO;

    return submissionDTO;
  }

  /**
   * Requests the {@link GradingDTO} for the {@link submissionIdDTO} using the {@link assignmentService}
   */
  private getGrading(): void {
    this.assignmentService.getGrading(this.submissionIdDTO).subscribe(grading => {
      this.gradingDTO = grading;
      this.gradingReceived = true;
      this.emitSubmissionEvents();
    });
  }

  /**
   * Emits the events in the course of a submission,
   * namely adjusting the {@link _highestDiagnoseLevel} if needed and emitting the submission UUID
   * {@see submissionUUIDReceived}
   * @private
   */
  private emitSubmissionEvents(): void {
    if (this.diagnoseLevel > this._highestDiagnoseLevel && this.action !== 'submit' && this.points === 0) {
      this._highestDiagnoseLevel = this.diagnoseLevel;
    }

    if (this.submissionIdDTO.submissionId) {
      this.submissionUUIDReceived.emit(this.submissionIdDTO.submissionId);
    }
  }

  /**
   * Special Method for Pm-task:
   * Maps the submission JSON string to a map of attributes
   * and reassigns it to the input variables
   * this.orI2 = map.get("orI2");
   * @private
   */
  private getSubmissionFromJson(): void {
    const json = this._submission;
    const map = new Map<string, string>(Object.entries(JSON.parse(json)));
    this.updateForm.controls['orI1'].patchValue(<string>map.get('orI1'));
    this.orI2 = <string>map.get('orI2');
    this.orI3 = <string>map.get('orI3');
    this.aaI1 = <string>map.get('aaI1');
    this.aaI2 = <string>map.get('aaI2');
    this.aaI3 = <string>map.get('aaI3');
    this.aaI4 = <string>map.get('aaI4');
    this.aaI5 = <string>map.get('aaI5');
    this.aaI6 = <string>map.get('aaI6');
    this.aaI7 = <string>map.get('aaI7');
  }
}
