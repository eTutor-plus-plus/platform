import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe for transforming role ids into their corresponding i18n key.
 */
@Pipe({ name: 'translateRole' })
export class TranslateRolePipe implements PipeTransform {
  /**
   * Performs the transformation from the given role id into corresponding i18n key.
   *
   * @param value the role id
   * @returns the i18n key
   */
  public transform(value: string): string {
    return 'roles.' + value.toUpperCase();
  }
}
