/* eslint-disable @typescript-eslint/ban-ts-comment */
/* eslint-disable @angular-eslint/component-selector */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-unnecessary-condition */
/* eslint-disable @angular-eslint/directive-selector */
/* eslint-disable @angular-eslint/no-host-metadata-property */

// @ts-nocheck

import { Directive, ElementRef } from '@angular/core';
import { DropdownDirective } from './dropdown.directive';

@Directive({
  selector: '[ngxDropdownToggle]',
  host: {
    class: 'dropdown-toggle',
    'aria-haspopup': 'true',
    '[attr.aria-expanded]': 'dropdown.isOpen',
    '(click)': 'dropdown.toggle()',
  },
})
export class DropdownToggleDirective {
  constructor(public dropdown: DropdownDirective, elementRef: ElementRef) {
    dropdown.toggleElement = elementRef.nativeElement;
  }
}
