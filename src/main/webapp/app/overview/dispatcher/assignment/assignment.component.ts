import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { Assignment } from 'app/overview/dispatcher/entities/Assignment';
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
  @Input() public submission!: string;
  @Input() public assignment!: Assignment;
  @Input() public diagnoseLevel!: string;
  @Input() public action!: string;
  @Input() public points!: number;
  @Output() public solutionCorrect: EventEmitter<Assignment> = new EventEmitter();
  @Output() public submissionAdded: EventEmitter<string> = new EventEmitter<string>();

  public diagnoseLevels = ['0', '1', '2', '3'];
  public gradingReceived = false;
  public hasErrors = true;
  public gradingDto!: GradingDTO;
  public editorOptions = { theme: 'vs-dark', language: 'sql' };

  private submissionDto!: SubmissionDTO;
  private submissionIdDto!: SubmissionIdDTO;

  constructor(private assignmentService: AssignmentService) {}
  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.editorOptions.language = mapEditorOption(this.assignment.task_type);
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
    this.submissionAdded.emit(this.submission);

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
   * Requests the grading for the submission identified by this.submissionIdDto
   */
  private getGrading(): void {
    this.assignmentService.getGrading(this.submissionIdDto).subscribe(grading => {
      this.gradingDto = grading;
      this.gradingReceived = true;
      this.gradingDto.maxPoints > this.gradingDto.points || this.gradingDto.maxPoints === 0
        ? (this.hasErrors = true)
        : (this.hasErrors = false);

      if (!this.hasErrors && this.action === 'submit') {
        this.solutionCorrect.emit(this.assignment);
        this.points = 1;
      }
    });
  }

  private initializeSubmissionDTO(): SubmissionDTO {
    const attributes = new Map<string, string>();
    attributes.set('action', this.action);
    attributes.set('submission', this.submission);
    attributes.set('diagnoseLevel', this.diagnoseLevel);

    const jsonAttributes: { [k: string]: any } = {};
    attributes.forEach((key, value) => {
      jsonAttributes[value] = key;
    });

    const submissionDto: SubmissionDTO = {
      submissionId: '',
      exerciseId: this.assignment.exercise_id,
      passedAttributes: jsonAttributes,
      taskType: this.assignment.task_type,
      passedParameters: new Map<string, string>(),
    };
    this.submissionDto = submissionDto;

    return submissionDto;
  }
}
