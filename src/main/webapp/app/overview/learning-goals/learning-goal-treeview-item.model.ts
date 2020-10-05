import { TreeItem, TreeviewItem } from "ngx-treeview";
import { isNil } from "lodash";

/**
 * The {@link LearningGoalTreeviewItem} interface extends the {@link TreeItem} interface
 * by providing learning goal display specific properties.
 */
export interface LearningGoalTreeItem extends TreeItem {
  /**
   * Indicates whether the learning goal is marked as private or not.
   */
  markedAsPrivate?: boolean;

  /**
   * The description of the learning goal.
   */
  description: string;

  /**
   * The optional children list.
   */
  children?: LearningGoalTreeItem[];
}

/**
 * The class {@link LearningGoalTreeviewItem} extends the {@link TreeviewItem} class by
 * providing learning goal display specific properties.
 */
export class LearningGoalTreeviewItem extends TreeviewItem {

  private _markedAsPrivate: boolean;
  private _description: string;

  /**
   * Constructor.
   *
   * @param item the root item
   */
  constructor(item: LearningGoalTreeItem) {
    super(item);

    this._markedAsPrivate = item.markedAsPrivate === true;
    this._description = item.description;

    if (!isNil(item.children) && item.children.length > 0) {
      super.children = item.children.map(child => {

        if (super.disabled === true) {
          child.disabled = true;
        }
        return new LearningGoalTreeviewItem(child);
      });
    }
  }

  /**
   * Returns whether the item is marked as private or not.
   *
   * @returns {@code true} if the item is marked as private, otherwise {@code false}
   */
  public get markedAsPrivate(): boolean {
    return this._markedAsPrivate;
  }

  /**
   * Sets whether the item is marked as private or not.
   *
   * @param value the value to set
   */
  public set markedAsPrivate(value: boolean) {
    this._markedAsPrivate = value;
  }

  /**
   * Returns the description.
   *
   * @returns the description
   */
  public get description(): string {
    return this._description;
  }

  /**
   * Sets the description.
   *
   * @param value the value to set
   */
  public set description(value: string) {
    this._description = value;
  }
}
