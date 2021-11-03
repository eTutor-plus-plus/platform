import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Used to manage SQL-Exercise related resources in the backend (dispatcher)
 */

const httpOptionsTextResponse = {
  responseType: 'text' as 'json',
};

@Injectable({
  providedIn: 'root',
})
export class SqlExerciseService {
  constructor(private http: HttpClient) {}

  /**
   * Requests an sql-table as html-table
   * @param tableName the name of the table
   * @param exerciseId an optional id providing context
   * @param taskGroup optional taskGroup providing context
   */
  public getHTMLTable(
    tableName: string,
    exerciseId?: string | null | undefined,
    taskGroup?: string | null | undefined
  ): Observable<string> {
    let url = 'api/dispatcher/sql/table/' + tableName;
    if (exerciseId) {
      url += '?exerciseId=' + exerciseId;
      if (taskGroup) {
        url += '&&taskGroup=' + taskGroup;
      }
    } else if (taskGroup) {
      url += '?taskGroup=' + taskGroup;
    }

    return this.http.get<string>(url, httpOptionsTextResponse);
  }
}
