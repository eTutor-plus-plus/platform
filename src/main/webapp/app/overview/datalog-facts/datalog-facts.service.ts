import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Used to fetch datalog facts
 */

const httpOptionsTextResponse = {
  responseType: 'text' as 'json',
};

@Injectable({
  providedIn: 'root',
})
export class DatalogFactsService {
  constructor(private http: HttpClient) {}

  /**
   * Requests an sql-table as html-table
   * @param id the id of the task-group
   */
  public getFacts(id: string): Observable<string> {
    const url = 'api/dispatcher/datalog/facts/id/' + id;
    return this.http.get<string>(url, httpOptionsTextResponse);
  }
}
