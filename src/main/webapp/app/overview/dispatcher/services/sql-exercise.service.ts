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
  public createSchema(schemaName: string, createStatements: string, insertSubmission: string, insertDiagnose: string): void {
    this.deleteSchemaUtil(schemaName).subscribe(() => {
      this.createSchemaUtil(schemaName).subscribe(() => {
        this.createTables(schemaName, createStatements).subscribe(() => {
          this.insertSubmission(schemaName, insertSubmission).subscribe();
          this.insertDiagnose(schemaName, insertDiagnose).subscribe();
        });
      });
    });
  }

  /**
   * public method that adds the solution of the task in the dispatcher backend
   * @param schemaName the name of the schema to execute student's submissions on
   * @param exerciseID the id of the exercise
   * @param solution the solution of the exercise
   */
  public createExercise(schemaName: string, exerciseID: string, solution: string): void {
    this.deleteExercise(exerciseID).subscribe(() => this.createExerciseUtil(schemaName, exerciseID, solution).subscribe());
  }

  /**
   * Deletes a schema
   * @param schemaName the name of the schema
   */
  public deleteSchema(schemaName: string): void {
    this.deleteSchemaUtil(schemaName).subscribe();
  }

  /**
   * Returns an available exercise id
   */
  public getExerciseId(): Observable<Response> {
    const url = this.API_URL + '/exercise/reservation';
    return this.http.get<Response>(url);
  }

  /**
   * Deletes the connection for a schema and all exercises that point to this connection
   * @param schemaName the schema name
   */
  public deleteConnection(schemaName: string): void {
    const url = this.API_URL + '/schema/' + schemaName + '/connection';
    this.http.delete(url).subscribe();
  }
  /**
   * requests the deletion of a given schema
   * @param schemaName the name of the schema to be deleted
   * @private
   */
  private deleteSchemaUtil(schemaName: string): Observable<Response> {
    const url = this.API_URL + '/schema/' + schemaName;
    return this.http.delete<Response>(url, httpOptions);
  }

  /**
   * requests the creation of a given schema
   * @param schemaName the name of the schema to be created
   * @private
   */
  private createSchemaUtil(schemaName: string): Observable<Response> {
    const url = this.API_URL + '/schema/' + schemaName;
    return this.http.put<Response>(url, null, httpOptions);
  }

  /**
   * requests the execution of the given statements on the given schema
   * @param schemaName the name of the schema
   * @param statements the statements to be executed
   * @private
   */
  private createTables(schemaName: string, statements: string): Observable<Response> {
    const url = this.API_URL + '/schema/' + schemaName + '/table';
    return this.http.put<Response>(url, statements, httpOptions);
  }

  /**
   * requests the execution of the given statements on the submission-version of the given schema
   * @param schemaName the name of the schema
   * @param statements the statements to be executed
   * @private
   */
  private insertSubmission(schemaName: string, statements: string): Observable<Response> {
    const url = this.API_URL + '/schema/' + schemaName + '/submission/data';
    return this.http.post<Response>(url, statements, httpOptions);
  }
  /**
   * requests the execution of the given statements on the diagnose-version of the given schema
   * @param schemaName the name of the schema
   * @param statements the statements to be executed
   * @private
   */
  private insertDiagnose(schemaName: string, statements: string): Observable<Response> {
    const url = this.API_URL + '/schema/' + schemaName + '/diagnose/data';
    return this.http.post<Response>(url, statements, httpOptions);
  }

  /**
   * requests the adding of the solution
   * @param schemaName the name of the schema where the exercise has to be executed
   * @param exerciseId the id identifying the exercise
   * @param solution the solution for the exercise
   * @private
   */
  private createExerciseUtil(schemaName: string, exerciseId: string, solution: string): Observable<Response> {
    const url = this.API_URL + '/exercise/' + schemaName + '/' + exerciseId;
    return this.http.put<Response>(url, solution, httpOptions);
  }

  /**
   * requests the deletion of a given exercise
   * @param exerciseId the id identifying the exercise
   * @private
   */
  private deleteExercise(exerciseId: string): Observable<Response> {
    const url = this.API_URL + '/exercise/' + exerciseId;
    return this.http.delete<Response>(url, httpOptions);
  }
}
