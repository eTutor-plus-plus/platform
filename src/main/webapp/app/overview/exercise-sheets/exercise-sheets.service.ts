import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { IExerciseSheetDisplayDTO, IExerciseSheetDTO, INewExerciseSheetDTO } from './exercise-sheets.model';
import { Observable } from 'rxjs';
import { createRequestOption, Pagination } from '../../shared/util/request-util';

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
    return this.http.post<IExerciseSheetDTO>('/api/exercise-sheet', newExerciseSheet, { observe: 'response' });
  }

  /**
   * Gets the page data from the rest endpoint.
   *
   * @param page the pagination info
   * @param nameFilter the optional name fulltext filter
   */
  public getExerciseSheetPage(page: Pagination, nameFilter?: string): Observable<ExerciseSheetDisplayArrayResult> {
    const options = createRequestOption(page);
    let url = '/api/exercise-sheet/display/paged';

    if (nameFilter && nameFilter.trim().length > 0) {
      url += `?name=${nameFilter.trim()}`;
    }

    return this.http.get<IExerciseSheetDisplayDTO[]>(url, { params: options, observe: 'response' });
  }
}
