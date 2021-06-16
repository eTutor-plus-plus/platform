import { Component, Input } from '@angular/core';
import { Assignment } from 'app/dispatcher/entities/Assignment';
import { ASSIGNMENTS } from 'app/dispatcher/mock-assignments';

@Component({
  selector: 'jhi-assignments',
  templateUrl: './assignments.component.html',
  styleUrls: ['./assignments.component.scss'],
})
export class AssignmentsComponent {
  assignments: Assignment[] = ASSIGNMENTS;
  solvedAssignments: Set<Assignment> = new Set<Assignment>();
  assignmentsSubmitted = false;

  @Input() diagnoseLevel = '3';

  assignmentSolved(assignment: Assignment): void {
    this.solvedAssignments.add(assignment);
  }

  onSubmit(): void {
    this.assignmentsSubmitted = true;
  }
}
