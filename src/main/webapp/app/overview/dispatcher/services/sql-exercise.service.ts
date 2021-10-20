import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Used to manage SQL-Exercises in the backend (dispatcher)
 */
const httpOptionsWithContent = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
  responseType: 'text' as 'json',
};

const httpOptionsTextResponse = {
  responseType: 'text' as 'json',
};

@Injectable({
  providedIn: 'root',
})
export class SqlExerciseService {
  constructor(private http: HttpClient) {}

  /**
   * Creates a schema, tables and data
   * @param schema the schema
   * @param createStatements the create-table-statements
   * @param insertSubmission the insert-into-statements for the submission version of the schema
   * @param insertDiagnose the insert-into-statements for the diagnose version of the schema
   */
  public executeDDL(schema: string, createStatements: string, insertSubmission: string, insertDiagnose: string): Observable<any> {
    const url = 'api/dispatcher/sql/schema';
    return this.http.post<string>(
      url,
      {
        createStatements: createStatements.trim().split(';'),
        insertStatementsSubmission: insertSubmission.trim().split(';'),
        insertStatementsDiagnose: insertDiagnose.trim().split(';'),
        schemaName: schema,
      },
      httpOptionsWithContent
    );
  }

  /**
   * Adds the solution of the task in the dispatcher backend
   * @param schemaName the name of the schema to execute student's submissions on
   * @param exerciseID the id of the exercise
   * @param solution the solution of the exercise
   */
  public async createExercise(schemaName: string, exerciseID: string, solution: string): Promise<void> {
    await this.deleteExercise(exerciseID).toPromise();
    const url = 'api/dispatcher/sql/exercise/' + schemaName + '/' + exerciseID;
    await this.http.put<string>(url, solution, httpOptionsWithContent).toPromise();
  }

  /**
   * Fetches the solution of a given exercise
   * @param id the id of the exercise
   */
  public getSolution(id: string): Observable<string> {
    const url = 'api/dispatcher/sql/exercise/' + id + '/solution';
    return this.http.get<string>(url, httpOptionsTextResponse);
  }

  /**
   * Updates the solution of an existing exercise
   * @param exerciseID the id
   * @param newSolution the solution
   */
  public updateExerciseSolution(exerciseID: string, newSolution: string): Observable<string> {
    const url = 'api/dispatcher/sql/exercise/' + exerciseID + '/solution';
    return this.http.post<string>(url, newSolution, httpOptionsTextResponse);
  }

  /**
   * Deletes a schema
   * @param schemaName the name of the schema
   */
  public deleteSchema(schemaName: string): Observable<string> {
    const url = 'api/dispatcher/sql/schema/' + schemaName;
    return this.http.delete<string>(url, httpOptionsTextResponse);
  }

  /**
   * Returns an available exercise id
   */
  public getExerciseId(): Observable<string> {
    const url = 'api/dispatcher/sql/exercise/reservation';
    return this.http.get<string>(url, httpOptionsTextResponse);
  }

  /**
   * Deletes the connection for a schema and all exercises that point to this connection
   * @param schemaName the schema name
   */
  public deleteConnection(schemaName: string): Observable<any> {
    const url = 'api/dispatcher/sql/schema/' + schemaName + '/connection';
    return this.http.delete(url, httpOptionsTextResponse);
  }

  /**
   * requests the deletion of a given exercise
   * @param exerciseId the id identifying the exercise
   * @private
   */
  public deleteExercise(exerciseId: string): Observable<string> {
    const url = 'api/dispatcher/sql/exercise/' + exerciseId;
    return this.http.delete<string>(url, httpOptionsTextResponse);
  }

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
