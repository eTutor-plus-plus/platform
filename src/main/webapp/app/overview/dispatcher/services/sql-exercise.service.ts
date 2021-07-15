import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DISPATCHER_URL } from 'app/overview/dispatcher/constants';
import { Observable } from 'rxjs';
import { Response } from 'app/overview/dispatcher/entities/Response';

/**
 * Used to manage SQL-Exercises in the backend (dispatcher)
 */
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

  /**
   * Creates a schema, tables and data
   * @param schemaName
   * @param createStatements
   * @param insertSubmission
   * @param insertDiagnose
   */
  public executeDDL(schema: string, createStatements: string, insertSubmission: string, insertDiagnose: string): Observable<any> {
    const url = this.API_URL + '/schema';
    return this.http.post<string>(
      url,
      {
        createStatements: createStatements.trim().split(';'),
        insertStatementsSubmission: insertSubmission.trim().split(';'),
        insertStatementsDiagnose: insertDiagnose.trim().split(';'),
        schemaName: schema,
      },
      {
        headers: new HttpHeaders({
          'Content-Type': 'application/json',
        }),
        responseType: 'text' as 'json',
      }
    );
  }

  /**
   * public method that adds the solution of the task in the dispatcher backend
   * @param schemaName the name of the schema to execute student's submissions on
   * @param exerciseID the id of the exercise
   * @param solution the solution of the exercise
   */
  public async createExercise(schemaName: string, exerciseID: string, solution: string): Promise<void> {
    await this.deleteExercise(exerciseID).toPromise();
    const url = this.API_URL + '/exercise/' + schemaName + '/' + exerciseID;
    await this.http
      .put<string>(url, solution, {
        headers: new HttpHeaders({
          'Content-Type': 'application/json',
        }),
        responseType: 'text' as 'json',
      })
      .toPromise();
  }

  /**
   * Deletes a schema
   * @param schemaName the name of the schema
   */
  public deleteSchema(schemaName: string): Observable<string> {
    const url = this.API_URL + '/schema/' + schemaName;
    return this.http.delete<string>(url, {
      responseType: 'text' as 'json',
    });
  }

  /**
   * Returns an available exercise id
   */
  public getExerciseId(): Observable<string> {
    const url = this.API_URL + '/exercise/reservation';
    return this.http.get<string>(url, {
      responseType: 'text' as 'json',
    });
  }

  /**
   * Deletes the connection for a schema and all exercises that point to this connection
   * @param schemaName the schema name
   */
  public deleteConnection(schemaName: string): Observable<any> {
    const url = this.API_URL + '/schema/' + schemaName + '/connection';
    return this.http.delete(url, {
      responseType: 'text' as 'json',
    });
  }

  /**
   * requests the deletion of a given exercise
   * @param exerciseId the id identifying the exercise
   * @private
   */
  private deleteExercise(exerciseId: string): Observable<string> {
    const url = this.API_URL + '/exercise/' + exerciseId;
    return this.http.delete<string>(url, {
      responseType: 'text' as 'json',
    });
  }
}
