import { Component, Input, OnInit } from '@angular/core';
import { ICourseModel, ILearningGoalUpdateAssignment } from '../course-mangement.model';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CourseManagementService } from '../course-management.service';
import { LearningGoalTreeviewItem } from '../../shared/learning-goal-treeview-item.model';
import { LearningGoalsService } from '../../learning-goals/learning-goals.service';
import { AccountService } from '../../../core/auth/account.service';
import { cloneDeep } from 'lodash';

/**
 * Component which is used to add learning goals
 */
@Component({
  selector: 'jhi-learning-goal-assignment-update',
  templateUrl: './learning-goal-assignment-update.component.html',
  styleUrls: ['./learning-goal-assignment-update.component.scss'],
})
export class LearningGoalAssignmentUpdateComponent implements OnInit {
  public isSaving = false;

  public availableLearningGoals: LearningGoalTreeviewItem[] = [];
  public selectedLearningGoals: LearningGoalTreeviewItem[] = [];

  public selectedAvailableGoal?: LearningGoalTreeviewItem;
  public selectedSelectedGoal?: LearningGoalTreeviewItem;

  private _selectedCourse?: ICourseModel;
  private loginName = '';
  private initialLoadingDone = false;

  private extractedLearningGoals: LearningGoalTreeviewItem[] = [];
  private allAvailableLearningGoals: LearningGoalTreeviewItem[] = [];

  /**
   * Constructor.
   *
   * @param activeModal the injected active modal service
   * @param courseManagementService the injected course management service
   * @param learningGoalsService the injected learning goals service
   * @param accountService the injected account service
   */
  constructor(
    private activeModal: NgbActiveModal,
    private courseManagementService: CourseManagementService,
    private learningGoalsService: LearningGoalsService,
    private accountService: AccountService
  ) {
    this.loginName = this.accountService.getLoginName()!;
  }

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems(this.loginName).subscribe(value => {
      this.availableLearningGoals = value;
      this.allAvailableLearningGoals = [];
      this.availableLearningGoals.forEach(x => this.allAvailableLearningGoals.push(cloneDeep(x)));
      this.initialLoadingDone = true;
    });
  }

  /**
   * Closes the dialog.
   */
  public close(): void {
    this.activeModal.dismiss();
  }

  /**
   * Saves the course's assignment.
   */
  public save(): void {
    this.isSaving = true;

    const ids = this.selectedLearningGoals.map(x => x.value as string);

    const assignment: ILearningGoalUpdateAssignment = {
      courseId: this.selectedCourse.id!,
      learningGoalIds: ids,
    };

    this.courseManagementService.setLearningGoalAssignment(assignment).subscribe(
      () => {
        this.isSaving = false;
        this.activeModal.close();
      },
      () => {
        this.isSaving = false;
      }
    );
  }

  /**
   * Sets the selected course.
   *
   * @param value the course value to set
   */
  @Input()
  public set selectedCourse(value: ICourseModel) {
    this._selectedCourse = value;

    this.loadAssignmentsFromCourseAsync();
  }

  /**
   * Returns the selected course.
   */
  public get selectedCourse(): ICourseModel {
    return this._selectedCourse!;
  }

  /**
   * Event handler for the selection of an available learning goal.
   *
   * @param item the selected tree view item
   */
  public onSelectLearningGoal(item: LearningGoalTreeviewItem): void {
    this.removeGoalFromAvailable(item);

    const originalItem = this.removeSubTreesOfSelectedItem(item);
    this.selectedLearningGoals.push(originalItem);
    this.selectedLearningGoals.sort((a, b) => a.text.localeCompare(b.text));
    this.selectedAvailableGoal = undefined;
  }

  /**
   * Event handler for the deselection of an assigned learning goal.
   *
   * @param item the selected tree view item
   */
  public onDeselectLearningGoal(item: LearningGoalTreeviewItem): void {
    let idx = this.extractedLearningGoals.findIndex(x => x.value === item.value);
    const extractedItem = this.extractedLearningGoals[idx];
    this.extractedLearningGoals.splice(idx, 1);

    const originalItem = cloneDeep(this.getOriginalAvailableItem(item));

    if (extractedItem.parent) {
      extractedItem.parent.children.push(originalItem);
      extractedItem.parent.children.sort((a, b) => a.text.localeCompare(b.text));
    } else {
      this.availableLearningGoals.push(originalItem);
      this.availableLearningGoals.sort((a, b) => a.text.localeCompare(b.text));
    }

    idx = this.selectedLearningGoals.findIndex(x => x.value === item.value);
    this.selectedLearningGoals.splice(idx, 1);
    this.selectedSelectedGoal = undefined;
  }

  /**
   * Event handler for the on select learning goal button.
   */
  public onSelectedLearningGoal(): void {
    this.onSelectLearningGoal(this.selectedAvailableGoal!);
  }

  /**
   * Event handler for the on deselect learning goal button.
   */
  public onDeselectedLearningGoal(): void {
    this.onDeselectLearningGoal(this.selectedSelectedGoal!);
  }

  /**
   * Event handler for the selection of all available learning goals.
   */
  public onSelectAllLearningGoals(): void {
    this.availableLearningGoals = [];

    this.selectedLearningGoals = [];
    this.extractedLearningGoals = [];

    this.allAvailableLearningGoals.forEach(x => {
      this.selectedLearningGoals.push(cloneDeep(x));
      this.extractedLearningGoals.push(cloneDeep(x));
    });

    this.selectedAvailableGoal = undefined;
  }

  /**
   * Event handler for the deselection of all selected learning goals.
   */
  public onDeselectAllLearningGoals(): void {
    this.selectedLearningGoals = [];
    this.availableLearningGoals = [];
    this.allAvailableLearningGoals.forEach(x => this.availableLearningGoals.push(cloneDeep(x)));
    this.extractedLearningGoals = [];

    this.selectedSelectedGoal = undefined;
  }

  /**
   * Handles a simple left click on the selected goals tree view.
   * If the left click also contains a pressed CTRL key, the selection will be removed.
   *
   * @param event the corresponding mouse event
   * @param item the selected learning goal tree view item
   */
  public handleSelectedGoalsClicked(event: MouseEvent, item: LearningGoalTreeviewItem): void {
    if (event.ctrlKey || !this.isRootItemIn(item, this.selectedLearningGoals)) {
      this.selectedSelectedGoal = undefined;
    } else {
      this.selectedSelectedGoal = item;
    }
  }

  /**
   * Checks whether the current selected contexts menu's entry
   * is enabled.
   *
   * @param item the current item
   */
  public isSelectedContextMenuAllowed = (item: LearningGoalTreeviewItem): boolean => this.isRootItemIn(item, this.selectedLearningGoals);

  /**
   * Handles a simple left click on the available goals tree view.
   * If the left click also contains a pressed CTRL key, the selection will be removed.
   *
   * @param event the corresponding mouse event
   * @param item the selected learning goal tree view item
   */
  public handleAvailableGoalsClicked(event: MouseEvent, item: LearningGoalTreeviewItem): void {
    if (event.ctrlKey) {
      this.selectedAvailableGoal = undefined;
    } else {
      this.selectedAvailableGoal = item;
    }
  }

  /**
   * Returns whether the given item is a root node in the given list.
   *
   * @param item the item which should be checked
   * @param list the corresponding list
   */
  private isRootItemIn(item: LearningGoalTreeviewItem, list: LearningGoalTreeviewItem[]): boolean {
    return list.find(x => x.value === item.value) !== undefined;
  }

  /**
   * Removes the sub trees of a selected item.
   *
   * @param item the selected item
   */
  private removeSubTreesOfSelectedItem(item: LearningGoalTreeviewItem): LearningGoalTreeviewItem {
    const originalItem = this.getOriginalAvailableItem(item);

    this.removeSubTreeOfSelectedItemRecursive(originalItem);
    return originalItem;
  }

  /**
   * Removes the sub trees of a selected item while
   * recursively looping through the selected available item.
   *
   * @param item the current item
   */
  private removeSubTreeOfSelectedItemRecursive(item: LearningGoalTreeviewItem): void {
    const idx = this.selectedLearningGoals.findIndex(x => x.value === item.value);

    if (idx > -1) {
      this.selectedLearningGoals.splice(idx, 1);
    } else {
      for (const child of item.childItems) {
        this.removeSubTreesOfSelectedItem(child);
      }
    }
  }

  /**
   * Returns the original item from the available items list.
   *
   * @param item the which might not be the original one
   */
  private getOriginalAvailableItem(item: LearningGoalTreeviewItem): LearningGoalTreeviewItem {
    let parent = item;
    while (parent.parent) {
      parent = parent.parent;
    }

    let start = this.allAvailableLearningGoals.find(x => x.value === parent.value);

    if (start === undefined) {
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
      start = this.allAvailableLearningGoals.find(x => x.value === parent.rootId)!;
    }

    const itemToReturn = this.getOriginalAvailableItemRecursive(start, item);

    return itemToReturn ?? item;
  }

  /**
   * Searches recursively for a given item. If the item is not found, undefined will be returned.
   *
   * @param item the item whose tree will be searched
   * @param itemToSearch the item which should be searched
   */
  private getOriginalAvailableItemRecursive(
    item: LearningGoalTreeviewItem,
    itemToSearch: LearningGoalTreeviewItem
  ): LearningGoalTreeviewItem | undefined {
    if (item.value === itemToSearch.value) {
      return item;
    }

    for (const child of item.childItems) {
      const ret = this.getOriginalAvailableItemRecursive(child, itemToSearch);
      if (ret) {
        return ret;
      }
    }
    return undefined;
  }

  /**
   * Removes the given learning goal tree view item from the available ones.
   *
   * @param item the item to remove
   * @param itemFromEndpoint
   */
  private removeGoalFromAvailable(item: LearningGoalTreeviewItem, itemFromEndpoint = false): void {
    if (itemFromEndpoint) {
      let idx = this.availableLearningGoals.findIndex(x => x.value === item.rootId);
      const originalParent = this.availableLearningGoals[idx];
      const originalItem = this.getOriginalAvailableItemRecursive(originalParent, item) ?? item;

      this.extractedLearningGoals.push(originalItem);
      if (originalItem.value === originalParent.value) {
        this.availableLearningGoals.splice(idx, 1);
      } else {
        idx = originalItem.parent!.children.findIndex(x => x.value === originalItem.value);
        originalItem.parent!.children.splice(idx, 1);
      }
    } else {
      if (!item.parent) {
        const idx = this.availableLearningGoals.findIndex(x => x.value === item.value);
        this.extractedLearningGoals.push(item);
        this.availableLearningGoals.splice(idx, 1);
      } else {
        const idx = item.parent.children.findIndex(x => x.value === item.value);
        item.parent.children.splice(idx, 1);
        this.extractedLearningGoals.push(item);
      }
    }
  }

  /**
   * Asynchronously loads the assignment from the current course.
   */
  private async loadAssignmentsFromCourseAsync(): Promise<any> {
    const goalsFromService = await this.courseManagementService
      .getLearningGoalsFromCourse(this._selectedCourse!, this.loginName)
      .toPromise();
    while (!this.initialLoadingDone) {
      await this.delay(10);
    }

    this.selectedLearningGoals = goalsFromService;

    for (const goal of goalsFromService) {
      this.removeGoalFromAvailable(goal, true);
    }
  }

  /**
   * Performs an awaitable delay.
   *
   * @param ms the amount in milliseconds
   */
  private delay(ms: number): Promise<any> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
