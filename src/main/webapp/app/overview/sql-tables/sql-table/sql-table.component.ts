import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'jhi-tasks-overview',
  templateUrl: './sql-table.component.html',
  styleUrls: ['./sql-table.component.scss'],
  providers: [TranslatePipe],
})
export class SqlTableComponent {}
