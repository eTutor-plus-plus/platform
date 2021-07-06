/* eslint-disable */
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { TreeviewI18n } from '../../../shared/ngx-treeview/models/treeview-i18n';
import { TreeviewSelection } from '../../../shared/ngx-treeview/models/treeview-item';

/**
 * I18n provider service for the angular tree view.
 */
@Injectable()
export class DefaultTreeviewI18n extends TreeviewI18n {
  /**
   * Constructor.
   *
   * @param translate the injected translation service
   */
  constructor(private translate: TranslateService) {
    super();
  }

  /**
   * Returns the all checkbox test - currently not in use.
   */
  public getAllCheckboxText(): string {
    return '';
  }

  /**
   * Returns the no items found text.
   */
  public getFilterNoItemsFoundText(): string {
    return this.translate.instant('treeview.notFound');
  }

  /**
   * Returns the filter placeholder text.
   */
  public getFilterPlaceholder(): string {
    return this.translate.instant('treeview.filterPlaceholder');
  }

  /**
   * Returns the text for the current selection - currently not in use.
   *
   * @param selection the selection
   */
  public getText(selection: TreeviewSelection): string {
    return '';
  }

  /**
   * Returns the collapse and expand text.
   *
   * @param isCollapse true, if the expanding text should be returned, otherwise the collapsing text will be returned
   */
  public getTooltipCollapseExpandText(isCollapse: boolean): string {
    if (isCollapse) {
      return this.translate.instant('treeview.expand');
    }
    return this.translate.instant('treeview.collapse');
  }
}
