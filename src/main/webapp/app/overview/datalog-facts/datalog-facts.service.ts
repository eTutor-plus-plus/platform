import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from '../../app.constants';

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
   * Requests the facts in html format
   * @param id the id of the facts
   */
  public getFacts(id: string): Observable<string> {
    const url = 'api/dispatcher/datalog/facts/id/' + id;
    return this.http.get<string>(url, httpOptionsTextResponse);
  }

  /**
   * Downloads the datalog facts as dlv file
   * @param id the id of the facts
   */
  public downloadFacts(id: string): void {
    this.http
      .get(`${SERVER_API_URL}api/dispatcher/datalog/facts/id/${id}/asinputstream`, {
        responseType: 'blob',
      })
      .subscribe(res => {
        const blob = new Blob([res], { type: 'text/plain' });
        const fileName = id + '.dlv';
        const objectURL = URL.createObjectURL(blob);
        const a = document.createElement('a');

        a.href = objectURL;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();

        document.body.removeChild(a);
        URL.revokeObjectURL(objectURL);
      });
  }
}
