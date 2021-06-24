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
  @Input() public diagnoseLevel = '3';
  @Input() public action = 'diagnose';
  @Output() public solutionCorrect: EventEmitter<Assignment> = new EventEmitter();

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
  onDiagnose(): void {
    this.action = 'diagnose';
    this.processSubmission();
  }

  /**
   * Handles a click on the submit Button
   */
  onSubmit(): void {
    this.action = 'submit';
    this.processSubmission();
  }

  /**
   * Creates a SubmissionDTO and uses assignment.service to send it to dispatcher
   * @param toBeSubmitted defines if grading for submission needs to be emitted
   */
  processSubmission(): void {
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

    this.assignmentService.postSubmission(submissionDto).subscribe(submissionId => {
      this.submissionIdDto = submissionId;
      setTimeout(() => {
        this.getGrading();
      }, 2000);
    });
  }

  /**
   * Requests the grading for the submission identified by this.submissionIdDto
   */
  getGrading(): void {
    this.assignmentService.getGrading(this.submissionIdDto).subscribe(grading => {
      this.gradingDto = grading;
      this.gradingReceived = true;
      this.gradingDto.maxPoints > this.gradingDto.points || this.gradingDto.maxPoints === 0
        ? (this.hasErrors = true)
        : (this.hasErrors = false);

      if (!this.hasErrors && this.action === 'submit') {
        this.solutionCorrect.emit(this.assignment);
      }
    });
  }
}
