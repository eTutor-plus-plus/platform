import { Component, OnInit } from '@angular/core';
import { StudentSelfEvaluationService } from './student-self-evaluation.service';

/**
 * The component for the student self evaluation.
 */
@Component({
  selector: 'jhi-student-self-evaluation',
  templateUrl: './student-self-evaluation.component.html',
})
export class StudentSelfEvaluationComponent implements OnInit {
  /**
   * Constructor.
   *
   * @param studentSelfEvaluationService the injected student self evaluation service
   */
  constructor(private studentSelfEvaluationService: StudentSelfEvaluationService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {}
}
