/* eslint-disable @typescript-eslint/ban-ts-comment */
/* eslint-disable @angular-eslint/component-selector */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable @typescript-eslint/no-unnecessary-condition */
/* eslint-disable @angular-eslint/directive-selector */
/* eslint-disable @angular-eslint/no-host-metadata-property */
/* eslint-disable @typescript-eslint/no-unused-vars */

// @ts-nocheck

import { Directive, HostBinding } from '@angular/core';
import { DropdownDirective } from './dropdown.directive';

@Directive({
  selector: '[ngxDropdownMenu]',
  host: {
    '[class.dropdown-menu]': 'true',
    '[class.show]': 'dropdown.isOpen',
  },
})
export class DropdownMenuDirective {
  constructor(public dropdown: DropdownDirective) {}
}
