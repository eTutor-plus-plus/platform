import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Assignment } from 'app/dispatcher/entities/Assignment';
import { SubmissionDTO } from 'app/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/dispatcher/entities/SubmissionIdDTO';
import { AssignmentService } from 'app/dispatcher/services/assignment.service';

/**
 * Component for handling an Assignment which has to be evaluated by the dispatcher
 */

@Component({
  selector: 'jhi-assignment',
  templateUrl: './assignment.component.html',
  styleUrls: ['./assignment.component.scss'],
})
export class AssignmentComponent {
  @Input() diagnoseLevel = '3';
  @Input() assignment!: Assignment;
  @Output() solutionCorrect: EventEmitter<Assignment> = new EventEmitter();

  submission = '';
  action = 'diagnose';
  gradingReceived = false;
  hasErrors = true;

  submissionDto!: SubmissionDTO;
  submissionIdDto!: SubmissionIdDTO;
  gradingDto!: GradingDTO;

  editorOptions = { theme: 'vs-dark', language: 'sql' };

  constructor(private assignmentService: AssignmentService) {}
  onDiagnose(): void {
    this.action = 'diagnose';
    this.processSubmission(false);
  }

  onSubmit(): void {
    this.action = 'submit';
    this.processSubmission(true);
  }

  processSubmission(toBeSubmitted: boolean): void {
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

    this.assignmentService.postSubmission(submissionDto).subscribe(s => {
      this.submissionIdDto = s;
      setTimeout(() => {
        this.getGrading(toBeSubmitted);
      }, 2000);
    });
  }

  getGrading(toBeSubmitted: boolean): void {
    this.assignmentService.getGrading(this.submissionIdDto).subscribe(g => {
      this.gradingDto = g;
      this.gradingReceived = true;
      this.gradingDto.maxPoints > this.gradingDto.points || this.gradingDto.maxPoints === 0
        ? (this.hasErrors = true)
        : (this.hasErrors = false);
      if (!this.hasErrors && toBeSubmitted) {
        this.solutionCorrect.emit(this.assignment);
      }
    });
  }
}
