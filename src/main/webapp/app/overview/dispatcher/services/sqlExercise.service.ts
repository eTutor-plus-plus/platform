import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DISPATCHER_URL } from 'app/overview/dispatcher/constants';
import { Observable } from 'rxjs';
import { Response } from 'app/overview/dispatcher/entities/Response';

/**
 * Used to manage SQL-Exercises
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
   * public method that requests the creation of an exercise including tables, data, and solution.
   * @param schemaName the schema name for the extension
   * @param createStatements the SQL statements used to create the necessary schemas
   * @param insertSubmission the SQL statements used to insert data into the submission-version of the tables
   * @param insertDiagnose SQL statements used to insert data into the submission-version of the tables
   * @param exerciseID the Exercise-ID of the task for identification in the dispatcher backend
   * @param solution the SQL Solution for the exercise
   */
  public create(
    schemaName: string,
    createStatements: string,
    insertSubmission: string,
    insertDiagnose: string,
    exerciseID: string,
    solution: string
  ): void {
    this.deleteSchema(schemaName).subscribe(() => {
      this.deleteExercise(exerciseID).subscribe();
      this.createSchema(schemaName).subscribe(() => {
        this.createTables(schemaName, createStatements).subscribe(() => {
          this.insertSubmission(schemaName, insertSubmission).subscribe();
          this.insertDiagnose(schemaName, insertDiagnose).subscribe();
          this.createExercise(schemaName, exerciseID, solution).subscribe();
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
  public add(schemaName: string, exerciseID: string, solution: string): void {
    this.deleteExercise(exerciseID).subscribe(() => this.createExercise(schemaName, exerciseID, solution).subscribe());
  }

  /**
   * requests the deletion of a given schema
   * @param schemaName the name of the schema to be deleted
   * @private
   */
  private deleteSchema(schemaName: string): Observable<Response> {
    const url = this.API_URL + '/schema/' + schemaName;
    return this.http.delete<Response>(url, httpOptions);
  }

  /**
   * requests the creation of a given schema
   * @param schemaName the name of the schema to be created
   * @private
   */
  private createSchema(schemaName: string): Observable<Response> {
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
  private createExercise(schemaName: string, exerciseId: string, solution: string): Observable<Response> {
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
