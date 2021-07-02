import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DISPATCHER_URL } from 'app/overview/dispatcher/constants';
import { Observable } from 'rxjs';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root',
})
export class SqlExerciseService {
  private API_URL: string = DISPATCHER_URL + '/sql';
  constructor(private http: HttpClient) {}

  createSchema(schemaName: string): Observable<any> {
    const url = this.API_URL + '/schema/' + schemaName;
    return this.http.put(url, null, httpOptions);
  }

  createTables(schemaName: string, statements: string): Observable<any> {
    const url = this.API_URL + '/schema/' + schemaName + '/table';
    return this.http.put(url, statements, httpOptions);
  }
}
