import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { DatalogFactsService } from './datalog-facts.service';

/**
 * Component that displays datalog facts
 */
@Component({
  selector: 'jhi-datalog-facts',
  templateUrl: './datalog-facts.component.html',
  styleUrls: ['./datalog-facts.component.scss'],
  providers: [TranslatePipe],
})
export class DatalogFactsComponent implements OnInit, OnDestroy {
  public facts?: string;

  private id!: string;
  private _paramMapSubscription?: Subscription;

  /**
   * constructor
   * @param router the injected routing service
   * @param activatedRoute the injected activated route
   * @param service the injected datalog-facts-service
   */
  constructor(private router: Router, private activatedRoute: ActivatedRoute, private service: DatalogFactsService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this.id = paramMap.get('id')!;
      (async () => {
        this.facts = await this.service.getFacts(this.id).toPromise();
      })();
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  ngOnDestroy(): void {
    this._paramMapSubscription?.unsubscribe();
  }

  /**
   * Downloads the facts as DLV file
   */
  public download(): void {
    this.service.downloadFacts(this.id);
  }
}
