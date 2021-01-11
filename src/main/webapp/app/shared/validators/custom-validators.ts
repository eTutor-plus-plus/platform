import { AbstractControl, ValidationErrors } from '@angular/forms';

/**
 * Function which checks whether the given value is empty or not.
 * If the value is a string and the trimString parameter is set to true,
 * blank strings will be treated as empty.
 *
 * @param value the value to check
 * @param trimString the trim indicator for string values
 */
function isEmptyInputValue(value: any, trimString: boolean): boolean {
  if (typeof value === 'string' && trimString) {
    return !value || value.length === 0 || value.trim().length === 0;
  } else {
    return value === null || value.length === 0;
  }
}

/**
 * Class which contains custom reactive form validators.
 */
export class CustomValidators {
  /**
   * @description
   * Validator that requires the control have a non-empty value.
   *
   * @usageNotes
   *
   * ### Validate that the field is non-empty and on strings to be not blanks
   *
   * ```typescript
   * const control = new FormControl('', Validators.required);
   *
   * console.log(control.errors); // {required: true}
   * ```
   *
   * @returns An error map with the `required` property
   * if the validation check fails, otherwise `null`.
   *
   * @see `updateValueAndValidity()`
   *
   */
  public static required(control: AbstractControl): ValidationErrors | null {
    return isEmptyInputValue(control.value, true) ? { required: true } : null;
  }
}
