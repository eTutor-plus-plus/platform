import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

/**
 * Component that displays an sql-table
 */
@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './xml-file.component.html',
  styleUrls: ['./xml-file.component.scss'],
  providers: [TranslatePipe],
})
export class XmlFileComponent implements OnInit, OnDestroy {
  public xml?: string;

  private id!: string | null;
  private taskGroup?: string | null;
  private _paramMapSubscription?: Subscription;
  private _queryParamSubscription?: Subscription;

  /**
   * constructor
   * @param router the injected routing service
   * @param activatedRoute the injected activated route
   */
  constructor(private router: Router, private activatedRoute: ActivatedRoute) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this._queryParamSubscription = this.activatedRoute.queryParamMap.subscribe(queryParams => {
        this.taskGroup = queryParams.get('taskGroup');
        this.id = queryParams.get('id');
        this.xml = '' + this.id! + ' ' + this.taskGroup!;
      });
    });
  }

  /**
   * Implements the destroy method. See {@link OnDestroy}.
   */
  ngOnDestroy(): void {
    this._queryParamSubscription?.unsubscribe();
    this._paramMapSubscription?.unsubscribe();
  }
}
