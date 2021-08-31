import { isNil } from 'lodash';
import { ILearningGoalModel } from './learning-goal-model';
import { TreeItem, TreeviewItem } from '../../shared/ngx-treeview/models/treeview-item';

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
  description?: string;

  /**
   * The optional children list.
   */
  children?: LearningGoalTreeItem[];

  /**
   * The count of courses which uses this learning goal.
   */
  referencedFromCnt?: number;

  /**
   * The login name of the goal's owner.
   */
  owner: string;

  /**
   * The change date.
   */
  changeDate: Date;

  /**
   * Indicates whether the learning goal needs additional verification
   * (only used when the goal is a super goal, i.e. the goal contains sub goals).
   */
  needVerification: boolean;

  /**
   * The id of the root node.
   */
  rootId?: string;
}

/**
 * The class {@link LearningGoalTreeviewItem} extends the {@link TreeviewItem} class by
 * providing learning goal display specific properties.
 */
export class LearningGoalTreeviewItem extends TreeviewItem {
  private _markedAsPrivate: boolean;
  private _description?: string;
  private _referencedFromCnt: number;
  private _owner: string;
  private _changeDate: Date;
  private readonly _needVerification: boolean;
  private readonly _currentUser: string;
  private readonly _parent?: LearningGoalTreeviewItem;
  private readonly _rootId?: string;

  /**
   * Constructor.
   *
   * @param item the root item
   * @param currentUser the currently logged in user
   * @param parent the parent goal
   */
  constructor(item: LearningGoalTreeItem, currentUser: string, parent: LearningGoalTreeviewItem | undefined = undefined) {
    super(item);

    this._markedAsPrivate = item.markedAsPrivate === true;
    this._description = item.description;
    this._referencedFromCnt = item.referencedFromCnt !== undefined ? item.referencedFromCnt : 0;
    this._owner = item.owner;
    this._changeDate = item.changeDate;
    this._currentUser = currentUser;
    this._parent = parent;
    this._rootId = item.rootId;
    this._needVerification = item.needVerification;

    if (!isNil(item.children) && item.children.length > 0) {
      super.children = item.children.map(child => {
        if (super.disabled) {
          child.disabled = true;
        }
        return new LearningGoalTreeviewItem(child, currentUser, this);
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
  public get description(): string | undefined {
    return this._description;
  }

  /**
   * Sets the description.
   *
   * @param value the value to set
   */
  public set description(value: string | undefined) {
    this._description = value;
  }

  /**
   * Returns the referenced from count.
   *
   * @returns the referenced from count
   */
  public get referencedFromCnt(): number {
    return this._referencedFromCnt;
  }

  /**
   * Sets the referenced from count.
   *
   * @param value the value to set
   */
  public set referencedFromCnt(value: number) {
    this._referencedFromCnt = value;
  }

  /**
   * Returns the owner's login.
   *
   * @returns the owner's login
   */
  public get owner(): string {
    return this._owner;
  }

  /**
   * Sets the owner's login.
   *
   * @param value the owner's login to set
   */
  public set owner(value: string) {
    this._owner = value;
  }

  /**
   * Returns the change date.
   *
   * @returns the change date
   */
  public get changeDate(): Date {
    return this._changeDate;
  }

  /**
   * Sets the change date.
   *
   * @param value the change date to set
   */
  public set changeDate(value: Date) {
    this._changeDate = value;
  }

  /**
   * Returns the parent.
   *
   * @returns the parent
   */
  public get parent(): LearningGoalTreeviewItem | undefined {
    return this._parent;
  }

  /**
   * Returns the optional root id.
   *
   * @returns the optional root id
   */
  public get rootId(): string | undefined {
    return this._rootId;
  }

  /**
   * Returns whether additional verification is needed for this learning goal or not no.
   */
  public get needVerification(): boolean {
    return this._needVerification;
  }

  /**
   * Converts this object into the corresponding {@link ILearningGoalModel} representation.
   *
   * @returns the {@link ILearningGoalModel} representation of this object
   */
  public toILearningGoalModel(): ILearningGoalModel {
    const model: ILearningGoalModel = {
      name: this.text,
      description: this.description,
      privateGoal: this.markedAsPrivate,
      referencedFromCount: this.referencedFromCnt,
      owner: this.owner,
      lastModifiedDate: this.changeDate,
      id: this.value,
      subGoals: [],
      needVerification: this.needVerification,
    };

    if (!isNil(this.childItems)) {
      for (const child of this.childItems) {
        model.subGoals.push(child.toILearningGoalModel());
      }
    }

    return model;
  }

  /**
   * Converts this object into the corresponding {@link LearningGoalTreeItem} representation.
   */
  public toLearningGoalTreeItem(): LearningGoalTreeItem {
    const treeItem: LearningGoalTreeItem = {
      markedAsPrivate: this._markedAsPrivate,
      description: this._description,
      children: [],
      referencedFromCnt: this._referencedFromCnt,
      owner: this._owner,
      changeDate: this._changeDate,
      rootId: this._rootId,
      value: this.value,
      text: this.text,
      disabled: this.disabled,
      checked: this.checked,
      collapsed: this.collapsed,
      needVerification: this.needVerification,
    };

    if (!isNil(this.childItems)) {
      for (const child of this.childItems) {
        treeItem.children!.push(child.toLearningGoalTreeItem());
      }
    }

    return treeItem;
  }

  /**
   * Returns whether the currently logged in user is allowed to modify this entry.
   *
   * @returns {code true} if the current user is allowed to modify this entry, otherwise {@code false}
   */
  public isUserAllowedToModify(): boolean {
    return this._currentUser === this._owner;
  }

  /**
   * Returns the current user.
   */
  public get currentUser(): string {
    return this._currentUser;
  }

  /**
   * Returns the children as `LearningGoalTreeviewItem[]`.
   */
  public get childItems(): LearningGoalTreeviewItem[] {
    /* eslint-disable @typescript-eslint/no-unnecessary-condition */
    return (super.children as LearningGoalTreeviewItem[]) ?? [];
    /* eslint-enable @typescript-eslint/no-unnecessary-condition */
  }

  /**
   * Sets the children.
   *
   * @param value the children to set
   */
  public set childItems(value: LearningGoalTreeviewItem[]) {
    super.children = value;
  }
}

/**
 * Class which represents a filtered learning goal treeview item.
 */
export class FilterLearningGoalTreeviewItem extends LearningGoalTreeviewItem {
  private readonly refItem: LearningGoalTreeviewItem;

  constructor(item: LearningGoalTreeviewItem) {
    super(
      {
        text: item.text,
        markedAsPrivate: item.markedAsPrivate,
        description: item.description,
        children: item.toLearningGoalTreeItem().children,
        referencedFromCnt: item.referencedFromCnt,
        owner: item.owner,
        changeDate: item.changeDate,
        rootId: item.rootId,
        value: item.value,
        disabled: item.disabled,
        checked: item.checked,
        collapsed: item.collapsed,
        needVerification: item.needVerification,
      },
      item.currentUser,
      item.parent
    );
    this.refItem = item;
  }

  /**
   * Updates the reference check.
   */
  public updateRefChecked(): void {
    this.children.forEach(child => {
      if (child instanceof FilterLearningGoalTreeviewItem) {
        child.updateRefChecked();
      }
    });

    let refChecked = this.checked;
    if (refChecked) {
      for (const refChild of this.refItem.children) {
        if (!refChild.checked) {
          refChecked = false;
          break;
        }
      }
    }
    this.refItem.checked = refChecked;
  }
}
