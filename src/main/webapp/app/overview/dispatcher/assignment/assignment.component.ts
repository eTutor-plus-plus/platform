import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { AssignmentService } from 'app/overview/dispatcher/services/assignment.service';
import { mapEditorOption } from '../services/EditorOptionsMapper';

/**
 * Component for handling an Assignment which has to be evaluated by the dispatcher
 */

@Component({
  selector: 'jhi-assignment',
  templateUrl: './assignment.component.html',
  styleUrls: ['./assignment.component.scss'],
})
export class AssignmentComponent implements OnInit {
  @Input() public exercise_id: string | undefined;
  @Input() public task_type: string | undefined;
  @Input() public submission: string | undefined;
  @Input() public diagnoseLevel = '0';
  @Input() public highestDiagnoseLevel = 0;
  @Input() public points = 0;
  @Input() public maxPoints = '';

  @Output() public solutionCorrect: EventEmitter<number> = new EventEmitter<number>();
  @Output() public submissionAdded: EventEmitter<string> = new EventEmitter<string>();
  @Output() public diagnoseLevelIncreased: EventEmitter<number> = new EventEmitter<number>();

  public diagnoseLevels = ['0', '1', '2', '3'];
  public gradingReceived = false;
  public hasErrors = true;
  public gradingDto!: GradingDTO;
  public editorOptions = { theme: 'vs-dark', language: 'sql' };

  private submissionDto!: SubmissionDTO;
  private submissionIdDto!: SubmissionIdDTO;
  private action!: string;

  /**
   * The constructor
   * @param assignmentService service for communicating with the dispatcher
   */
  constructor(private assignmentService: AssignmentService) {}
  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    if (this.task_type) {
      this.editorOptions.language = mapEditorOption(this.task_type);
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
   * Returns wheter this assignment has already been solved by the student
   */
  public isSolved(): boolean {
    return this.points !== 0;
  }

  /**
   * Creates a SubmissionDTO and uses assignment.service to send it to dispatcher
   *
   */
  private processSubmission(): void {
    this.emitSubmissionEvents();

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
   * Requests the grading for the submission identified by this.submissionIdDto from the assignment service
   */
  private getGrading(): void {
    this.assignmentService.getGrading(this.submissionIdDto).subscribe(grading => {
      this.gradingDto = grading;
      this.gradingReceived = true;

      this.hasError();
      this.emitGrading();
    });
  }

  /**
   * Verifies if the submission has been evaluated as correct by the dispatcher
   * and sets this.hasErrors accordingly
   * @private
   */
  private hasError(): void {
    this.gradingDto.maxPoints > this.gradingDto.points || this.gradingDto.maxPoints === 0
      ? (this.hasErrors = true)
      : (this.hasErrors = false);
  }

  /**
   * Emits the events in the course of a submission
   * @private
   */
  private emitSubmissionEvents(): void {
    const diagnoseLevel = parseInt(this.diagnoseLevel, 10);
    if (diagnoseLevel > this.highestDiagnoseLevel && this.action !== 'submit' && this.points === 0) {
      this.diagnoseLevelIncreased.emit(diagnoseLevel);
      this.highestDiagnoseLevel = diagnoseLevel;
    }
    this.submissionAdded.emit(this.submission);
  }

  /**
   * Emits the points if the assignment has been submitted and solved
   * @private
   */
  private emitGrading(): void {
    if (!this.hasErrors && this.action === 'submit') {
      const grading = parseInt(this.maxPoints, 10) - this.highestDiagnoseLevel;
      this.solutionCorrect.emit(grading);
      this.points = grading;
    }
  }
  /**
   * Initializes the SubmissionDTO as required by the dispatcher
   * @private
   */
  private initializeSubmissionDTO(): SubmissionDTO {
    const attributes = new Map<string, string>();
    attributes.set('action', this.action);
    attributes.set('submission', this.submission ?? '');
    attributes.set('diagnoseLevel', this.diagnoseLevel);

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
}
