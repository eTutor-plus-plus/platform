import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { SqlExerciseService } from '../dispatcher/services/sql-exercise.service';

/**
 * Component that displays an sql-table
 */
@Component({
  selector: 'jhi-datalog-facts',
  templateUrl: './datalog-facts.component.html',
  styleUrls: ['./datalog-facts.component.scss'],
  providers: [TranslatePipe],
})
export class DatalogFactsComponent implements OnInit, OnDestroy {
  public htmlTable?: string;
  public testSubmissions?: string;

  private tableName!: string;
  private exerciseId?: string | null;
  private taskGroup?: string | null;
  private _paramMapSubscription?: Subscription;
  private _queryParamSubscription?: Subscription;

  /**
   * constructor
   * @param router the injected routing service
   * @param activatedRoute the injected activated route
   * @param sqlService the injected sql-service
   */
  constructor(private router: Router, private activatedRoute: ActivatedRoute, private sqlService: SqlExerciseService) {}

  /**
   * Implements the init method. See {@link OnInit}.
   */
  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this.tableName = paramMap.get('id')!;
      this._queryParamSubscription = this.activatedRoute.queryParamMap.subscribe(queryParams => {
        this.exerciseId = queryParams.get('exerciseId');
        this.taskGroup = queryParams.get('taskGroup');
        (async () => {
          this.htmlTable = 'Success';
          await this.sqlService.getHTMLTable(this.tableName, this.exerciseId, this.taskGroup).toPromise();
        })();
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
