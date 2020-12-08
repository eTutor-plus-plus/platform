import { Component } from '@angular/core';

/**
 * Component for the footer.
 */
@Component({
  selector: 'jhi-footer',
  templateUrl: './footer.component.html',
})
export class FooterComponent {
  readonly imprintUrl: string = 'https://www.jku.at/impressum';
  readonly contactUrl: string = 'mailto:ernst@dke.uni-linz.ac.at';
}
