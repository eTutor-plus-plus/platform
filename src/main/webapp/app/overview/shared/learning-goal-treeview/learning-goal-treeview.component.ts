import { Component, Input, TemplateRef } from '@angular/core';
import { FilterLearningGoalTreeviewItem, LearningGoalTreeviewItem } from '../learning-goal-treeview-item.model';
import { isNil, includes } from 'lodash';
import { DefaultTreeviewI18n } from './default-treeview-i18n';
import { TreeviewI18n } from '../../../shared/ngx-treeview/models/treeview-i18n';
import { TreeviewItemTemplateContext } from '../../../shared/ngx-treeview/models/treeview-item-template-context';
import { TreeviewConfig } from '../../../shared/ngx-treeview/models/treeview-config';

/**
 * Component for displaying a filterable learning goal treeview.
 */
@Component({
  selector: 'jhi-learning-goal-treeview',
  templateUrl: './learning-goal-treeview.component.html',
  styleUrls: ['./learning-goal-treeview.component.scss'],
  providers: [{ provide: TreeviewI18n, useClass: DefaultTreeviewI18n }],
})
export class LearningGoalTreeviewComponent {
  public filterText = '';

  @Input()
  public itemTemplate!: TemplateRef<TreeviewItemTemplateContext>;

  public readonly config = TreeviewConfig.create({
    hasAllCheckBox: false,
    hasFilter: false,
    hasCollapseExpand: true,
  });

  private _allItems: LearningGoalTreeviewItem[] = [];
  private _filteredItems: LearningGoalTreeviewItem[] = [];

  /**
   * Event handler for the filter text change event.
   *
   * @param text the new filter text
   */
  public onFilterTextChange(text: string): void {
    this.filterText = text;
    this.updateFilterItems();
  }

  /**
   * Updates the filtered items.
   */
  private updateFilterItems(): void {
    if (this.filterText !== '') {
      const filterItems: LearningGoalTreeviewItem[] = [];
      const filterText = this.filterText.toLowerCase();
      this._allItems.forEach(item => {
        const newItem = this.filterItem(item, filterText);
        if (!isNil(newItem)) {
          filterItems.push(newItem);
        }
      });
      this._filteredItems = filterItems;
    } else {
      this._filteredItems = this._allItems;
    }
  }

  /**
   * Filters the given item.
   *
   * @param item the item to filter
   * @param filterText the filter text for the filtration
   */
  private filterItem(item: LearningGoalTreeviewItem, filterText: string): LearningGoalTreeviewItem | undefined {
    const isMatch = includes(item.text.toLowerCase(), filterText);
    if (isMatch) {
      return item;
    } else {
      if (!isNil(item.children)) {
        const children: LearningGoalTreeviewItem[] = [];
        item.childItems.forEach(child => {
          const newChild = this.filterItem(child, filterText);
          if (!isNil(newChild)) {
            children.push(newChild);
          }
        });
        if (children.length > 0) {
          const newItem = new FilterLearningGoalTreeviewItem(item);
          newItem.collapsed = false;
          newItem.childItems = children;
          return newItem;
        }
      }
    }

    return undefined;
  }

  /**
   * Sets the learning goal items.
   *
   * @param value the items to set
   */
  @Input()
  public set items(value: LearningGoalTreeviewItem[]) {
    this._allItems = value;

    if (!isNil(this._allItems)) {
      this.updateFilterItems();
    }
  }

  /**
   * Returns the learning goal items.
   */
  public get items(): LearningGoalTreeviewItem[] {
    return this._allItems;
  }

  /**
   * Returns the filtered learning goal items.
   */
  public get filteredItems(): LearningGoalTreeviewItem[] {
    return this._filteredItems;
  }
}
