import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { SqlExerciseService } from '../../dispatcher/services/sql-exercise.service';

@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './sql-table.component.html',
  styleUrls: ['./sql-table.component.scss'],
  providers: [TranslatePipe],
})
export class SqlTableComponent implements OnInit, OnDestroy {
  public htmlTable?: string;

  private tableName!: string;
  private exerciseId?: string | null;
  private taskGroup?: string | null;
  private _paramMapSubscription?: Subscription;
  private _queryParamSubscription?: Subscription;

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private sqlService: SqlExerciseService) {}

  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this.tableName = paramMap.get('tableName')!;
      this._queryParamSubscription = this.activatedRoute.queryParamMap.subscribe(queryParams => {
        this.exerciseId = queryParams.get('exerciseId');
        this.taskGroup = queryParams.get('taskGroup');
        (async () => {
          this.htmlTable = await this.sqlService.getHTMLTable(this.tableName, this.exerciseId, this.taskGroup).toPromise();
        })();
      });
    });
  }

  ngOnDestroy(): void {
    this._queryParamSubscription?.unsubscribe();
    this._paramMapSubscription?.unsubscribe();
  }
}
