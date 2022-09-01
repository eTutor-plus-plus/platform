import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

/**
 * Component for displaying the description of a learning goal in a modal window
 */

@Component({
  selector: 'jhi-learning-goal-description-modal',
  templateUrl: './learning-goal-description-modal.component.html',
  styleUrls: ['./learning-goal-description-modal.component.scss'],
})
export class LearningGoalDescriptionModalComponent {
  private _description = '';
  private _name = '';
  private dummy = -1;

  /**
   * Constructor
   * @param activeModal
   */
  constructor(private activeModal: NgbActiveModal) {
    this.dummy = -1;
  }

  /**
   * Closes the modal dialog.
   */
  public cancel(): void {
    this.activeModal.dismiss();
  }

  /**
   * Closes the current modal window.
   */
  public close(): void {
    this.activeModal.close();
  }

  /**
   * Sets the name
   * @param value
   */
  public set description(value: string) {
    this._description = value;
  }

  /**
   * Returns the description
   */
  public get description(): string {
    return this._description;
  }

  /**
   * Sets the name
   * @param value
   */
  public set name(value: string) {
    this._name = value;
  }

  /**
   * Returns the name
   */
  public get name(): string {
    return this._name;
  }
}
