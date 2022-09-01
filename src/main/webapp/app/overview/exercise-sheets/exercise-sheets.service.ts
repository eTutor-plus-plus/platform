import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { IExerciseSheetDisplayDTO, IExerciseSheetDTO, INewExerciseSheetDTO } from './exercise-sheets.model';
import { Observable } from 'rxjs';
import { Pagination } from 'app/core/request/request.model';
import { createRequestOption } from 'app/core/request/request-util';
import { SERVER_API_URL } from '../../app.constants';

type ExerciseSheetResult = HttpResponse<IExerciseSheetDTO>;
type ExerciseSheetDisplayArrayResult = HttpResponse<IExerciseSheetDisplayDTO[]>;

/**
 * Service which manages the exercise sheets.
 */
@Injectable({
  providedIn: 'root',
})
export class ExerciseSheetsService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Inserts a new exercise sheet.
   *
   * @param newExerciseSheet the exercise sheet to insert
   */
  public insertExerciseSheet(newExerciseSheet: INewExerciseSheetDTO): Observable<ExerciseSheetResult> {
    return this.http.post<IExerciseSheetDTO>(`${SERVER_API_URL}api/exercise-sheet`, newExerciseSheet, { observe: 'response' });
  }

  /**
   * Gets the page data from the rest endpoint.
   *
   * @param page the pagination info
   * @param nameFilter the optional name fulltext filter
   */
  public getExerciseSheetPage(page: Pagination, nameFilter?: string): Observable<ExerciseSheetDisplayArrayResult> {
    const options = createRequestOption(page);
    let url = `${SERVER_API_URL}api/exercise-sheet/display/sliced`;

    if (nameFilter && nameFilter.trim().length > 0) {
      url += `?name=${nameFilter.trim()}`;
    }

    return this.http.get<IExerciseSheetDisplayDTO[]>(url, { params: options, observe: 'response' });
  }

  /**
   * Gets the paged data from the rest endpoint.
   *
   * @param page the pagination info
   * @param nameFilter the optional name fulltext filter
   */
  public getPagedExerciseSheetPage(page: Pagination, nameFilter?: string): Observable<ExerciseSheetDisplayArrayResult> {
    const options = createRequestOption(page);
    let url = `${SERVER_API_URL}api/exercise-sheet/display/paged`;

    if (nameFilter && nameFilter.trim().length > 0) {
      url += `?name=${nameFilter.trim()}`;
    }

    return this.http.get<IExerciseSheetDisplayDTO[]>(url, { params: options, observe: 'response' });
  }

  /**
   * Returns an exercise sheet model by its id.
   *
   * @param exerciseSheetId the exercise sheet's id
   */
  public getExerciseSheetById(exerciseSheetId: string): Observable<ExerciseSheetResult> {
    const idxOfHashTag = exerciseSheetId.lastIndexOf('#');

    let internalId;
    if (idxOfHashTag >= 0) {
      internalId = exerciseSheetId.substring(idxOfHashTag + 1);
    } else {
      internalId = exerciseSheetId;
    }

    return this.http.get<IExerciseSheetDTO>(`${SERVER_API_URL}api/exercise-sheet/${internalId}`, { observe: 'response' });
  }

  /**
   * Deletes an exercise sheet.
   *
   * @param exerciseSheetId the exercise sheet's id
   */
  public deleteExerciseSheetById(exerciseSheetId: string): Observable<HttpResponse<any>> {
    const internalId = exerciseSheetId.substring(exerciseSheetId.lastIndexOf('#') + 1);
    return this.http.delete(`${SERVER_API_URL}api/exercise-sheet/${internalId}`, { observe: 'response' });
  }

  /**
   * Updates the given exercise sheet.
   *
   * @param exerciseSheet the exercise sheet to update
   */
  public updateExerciseSheet(exerciseSheet: IExerciseSheetDTO): Observable<HttpResponse<any>> {
    return this.http.put(`${SERVER_API_URL}api/exercise-sheet`, exerciseSheet, { observe: 'response' });
  }
}
