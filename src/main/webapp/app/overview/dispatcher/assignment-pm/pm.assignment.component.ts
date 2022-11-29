import { AfterContentChecked, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { AssignmentService } from 'app/overview/dispatcher/services/assignment.service';
import { FormBuilder } from '@angular/forms';

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
export class PmAssignmentComponent implements AfterContentChecked, OnInit {
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
  @Input() public submission: string | undefined;

  /**
   * The highest-diagnose level that has been chosen so far
   * Cannot be null or undefined
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
   * Indicates if the submission contained errors according to the grading
   * see method {@link setHasErrors}
   */
  public hasErrors = true;

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
    private assignmentService: AssignmentService,
    // added on 29.11
    private fb: FormBuilder
  ) {}

  /**
   * Maps the diagnose level to the text representation
   * @param number the diagnose level as a number
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

  public ngOnInit(): void {
    this.assignmentService.getPmLogForIndividualTask(this.courseInstanceUUID, this.exerciseSheetUUID, this.taskNo).subscribe(DTO => {
      this.log = DTO.log;
      this.dispatcherExerciseID = DTO.exerciseId;
    });
  }

  public ngAfterContentChecked(): void {
    if (!this.diagnoseLevelText && this.highestDiagnoseLevel) {
      this.diagnoseLevelText = this.mapDiagnoseLevel(this.highestDiagnoseLevel);
    }

    // reassign submission in form of JSON to variables
    if (this.submission) {
      this.getSubmissionFromJson();
    }
  }

  /**
   * Handles a click on the DIAGNOSE button
   * calls {@link processSubmission}
   */
  public onDiagnose(): void {
    this.action = 'diagnose';
    this.processSubmission();
  }

  /**
   * Handles a click on the SUBMIT button
   * calls {@link processSubmission}
   */
  public onSubmit(): void {
    this.action = 'submit';
    this.processSubmission();
  }

  /**
   * Creates a SubmissionDTO and uses the {@link assignmentService} to send it to the dispatcher
   */
  private processSubmission(): void {
    this.diagnoseLevel = this.mapDiagnoseText(this.diagnoseLevelText);
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
    attributes.set('orI1', this.updateForm.controls['orI1'].value);
    attributes.set('orI2', this.orI2);
    attributes.set('orI3', this.orI3);

    attributes.set('aaI1', this.aaI1);
    attributes.set('aaI2', this.aaI2);
    attributes.set('aaI3', this.aaI3);
    attributes.set('aaI4', this.aaI4);
    attributes.set('aaI5', this.aaI5);
    attributes.set('aaI6', this.aaI6);
    attributes.set('aaI7', this.aaI7);
    attributes.set('diagnoseLevel', this.diagnoseLevel.toFixed());
    attributes.set('isPmTask', 'true');
    attributes.set('exerciseId', this.dispatcherExerciseID.toFixed());

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
      // console.log(this.gradingDTO);
      this.gradingReceived = true;

      this.setHasErrors();
      this.emitSubmissionEvents();
    });
  }

  /**
   * Verifies if the submission has been evaluated as correct by the dispatcher
   * and sets {@link hasErrors} accordingly
   * has errors either if the maximal points are not achieved (points are less than maxPoints)
   * or if maxPoints equal 0
   * @private
   */
  private setHasErrors(): void {
    this.gradingDTO.maxPoints > this.gradingDTO.points || this.gradingDTO.maxPoints === 0
      ? (this.hasErrors = true)
      : (this.hasErrors = false);
  }

  /**
   * Emits the events in the course of a submission,
   * namely adjusting the {@link highestDiagnoseLevel} if needed and emitting the submission UUID
   * {@see submissionUUIDReceived}
   * @private
   */
  private emitSubmissionEvents(): void {
    if (this.diagnoseLevel > this.highestDiagnoseLevel && this.action !== 'submit' && this.points === 0) {
      this.highestDiagnoseLevel = this.diagnoseLevel;
    }

    if (this.submissionIdDTO.submissionId) {
      this.submissionUUIDReceived.emit(this.submissionIdDTO.submissionId);
    }

    // dont need this anymore since parent component has isSubmitted request
    /*this.assignmentService.isTaskSubmitted(this.courseInstanceUUID, this.exerciseSheetUUID, this.taskNo).subscribe(isSubmitted =>{
      this.isSubmitted = isSubmitted;
    }
    );*/

    // if action == submit: recalculate points
    if (this.action === 'submit' && this.points === 0 && !this.isSubmitted) {
      const grading = this.calculatePoints();
      this.points = grading;
      this.isSubmitted = true;
    }
  }

  /**
   * Calculates the achieved points with regard to the max points,
   * the highest chosen diagnose level and the weighting of the diagnose level
   * @private
   */
  private calculatePoints(): number {
    const weighting = this.diagnoseLevelWeighting ? parseInt(this.diagnoseLevelWeighting, 10) : 0;
    const dispatcherPoints = this.gradingDTO.points;
    let points: number;

    if (this.highestDiagnoseLevel === 3) {
      points = dispatcherPoints - 2 * this.highestDiagnoseLevel * weighting;
      if (points > 0) {
        points = 0;
      }
    } else if (this.highestDiagnoseLevel === 2) {
      points = dispatcherPoints - this.highestDiagnoseLevel * weighting;
      if (points < 0) {
        points = 0;
      }
    } else if (this.highestDiagnoseLevel === 1) {
      points = dispatcherPoints - this.highestDiagnoseLevel * weighting;
      if (points < 0) {
        points = 0;
      }
    } else {
      points = dispatcherPoints; // full points
    }

    return points;
    // note: status as of 23.11.22
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

  /**
   * Special Method for Pm-task:
   * Maps the submission JSON string to a map of attributes
   * and reassigns it to the input variables
   * this.orI2 = map.get("orI2");
   * @private
   */
  private getSubmissionFromJson(): void {
    const json = this.submission;
    if (typeof json === 'string') {
      const map = new Map<string, string>(Object.entries(JSON.parse(json)));

      //this.orI1 = <string>map.get("orI1");
      //this.updateForm.get('orI1')?.patchValue(<string>map.get("orI1"));
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
}
