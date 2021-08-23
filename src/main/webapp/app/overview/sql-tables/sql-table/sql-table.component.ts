import { Component, OnInit } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { SqlExerciseService } from '../../dispatcher/services/sql-exercise.service';

@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './sql-table.component.html',
  styleUrls: ['./sql-table.component.scss'],
  providers: [TranslatePipe],
})
export class SqlTableComponent implements OnInit {
  public htmlTable = '';

  private tableName = '';
  private _paramMapSubscription?: Subscription;

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private sqlService: SqlExerciseService) {}

  public ngOnInit(): void {
    this._paramMapSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this.tableName = paramMap.get('tableName')!;

      (async () => {
        this.htmlTable = await this.sqlService.getHTMLTable(this.tableName).toPromise();
      })();
    });
  }
}
