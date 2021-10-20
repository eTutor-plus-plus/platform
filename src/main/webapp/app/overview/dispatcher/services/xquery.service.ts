import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  public getXML(id: string): Observable<string> {
    const url = 'api/dispatcher/xquery/xml/fileid/' + id;
    return this.http.get<string>(url, httpOptionsTextResponse);
  }
}
