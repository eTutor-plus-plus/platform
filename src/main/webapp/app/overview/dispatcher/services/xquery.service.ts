import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from '../../../app.constants';

/**
 * Used to manage XQuery resources
 */

const httpOptionsTextResponse = {
  responseType: 'text' as 'json',
};

@Injectable({
  providedIn: 'root',
})
export class XQueryService {
  constructor(private http: HttpClient) {}

  /**
   * Returns the XML-File
   * @param id the id of the file
   */
  public getXML(id: string): Observable<string> {
    const url = 'api/dispatcher/xquery/xml/fileid/' + id;
    return this.http.get<string>(url, httpOptionsTextResponse);
  }

  /**
   * Initiates the download of the XML-File
   * @param id the id of the file
   */
  public downloadXML(id: string): void {
    this.http
      .get(`${SERVER_API_URL}api/dispatcher/xquery/xml/fileid/${id}/asinputstream`, {
        responseType: 'blob',
      })
      .subscribe(res => {
        const blob = new Blob([res], { type: 'application/application/xml' });
        const fileName = id + '.xml';
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
