import { AfterContentChecked, Component, EventEmitter, Input, Output } from '@angular/core';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { AssignmentService } from 'app/overview/dispatcher/services/assignment.service';
import { SubmissionEvent } from '../entities/SubmissionEvent';

/**
 * Component for handling an Assignment which has to be evaluated by the dispatcher
 */

@Component({
  selector: 'jhi-assignment',
  templateUrl: './assignment.component.html',
  styleUrls: ['./assignment.component.scss'],
})
export class AssignmentComponent implements AfterContentChecked {
  public diagnoseLevels = [
    'dispatcherAssignment.assignment.diagnoseLevel.none',
    'dispatcherAssignment.assignment.diagnoseLevel.little',
    'dispatcherAssignment.assignment.diagnoseLevel.some',
    'dispatcherAssignment.assignment.diagnoseLevel.much',
  ];
  @Input() public exercise_id: string | undefined;
  @Input() public task_type: string | undefined;
  @Input() public submission: string | undefined;
  @Input() public highestDiagnoseLevel!: number;
  @Input() public points!: number;
  @Input() public maxPoints = '';
  @Input() public diagnoseLevelWeighting = '';

  @Output() public solutionCorrect: EventEmitter<number> = new EventEmitter<number>();
  @Output() public submissionAdded: EventEmitter<SubmissionEvent> = new EventEmitter<SubmissionEvent>();
  @Output() public diagnoseLevelIncreased: EventEmitter<number> = new EventEmitter<number>();

  public diagnoseLevelText = '';

  public gradingReceived = false;
  public hasErrors = true;
  public gradingDto!: GradingDTO;
  public editorOptions = { theme: 'vs-dark', language: '' };

  private diagnoseLevel = 0;
  private submissionDto!: SubmissionDTO;
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
        return 'sql';
      default:
        return 'sql';
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
   * Lyfecycle method that sets the language for the editor in accordance to the task type
   * and the diagnose-level in the dropdown according to the highest chosen diagnose level
   */
  public ngAfterContentChecked(): void {
    if (!this.editorOptions.language && this.task_type) {
      this.editorOptions = { theme: 'vs-dark', language: AssignmentComponent.mapEditorLanguage(this.task_type) };
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
   * Returns whether this assignment has already been solved by the student
   */
  public isSolved(): boolean {
    return this.points !== 0;
  }

  /**
   * Creates a SubmissionDTO and uses the assignment service to send it to the dispatcher
   *
   */
  private processSubmission(): void {
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
      this.emitSubmissionEvents();
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
   * Emits the events in the course of a submission,
   * namely adjusting the highest diagnose level if needed and updating the latest submission
   * @private
   */
  private emitSubmissionEvents(): void {
    const hasBeenSubmitted = this.action === 'submit';
    const diagnoseLevel = this.mapDiagnoseText(this.diagnoseLevelText);
    this.diagnoseLevel = diagnoseLevel;
    if (diagnoseLevel > this.highestDiagnoseLevel && !hasBeenSubmitted && this.points === 0) {
      this.diagnoseLevelIncreased.emit(diagnoseLevel);
      this.highestDiagnoseLevel = diagnoseLevel;
    }

    this.submissionAdded.emit({ submission: this.submission ?? '', isSubmitted: hasBeenSubmitted, hasBeenSolved: !this.hasErrors });
  }

  /**
   * Emits the points if the assignment has been submitted and solved but not previously solved
   * @private
   */
  private emitGrading(): void {
    if (!this.isSolved() && !this.hasErrors && this.action === 'submit') {
      const grading = this.calculatePoints();
      this.solutionCorrect.emit(grading);
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
   * Initializes the SubmissionDTO as required by the dispatcher
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
   * Maps the diagnose level text to its number representation
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
