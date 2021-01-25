import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { IExerciseSheetDTO, INewExerciseSheetDTO } from './exercise-sheets.model';
import { Observable } from 'rxjs';

type ExerciseSheetResult = HttpResponse<IExerciseSheetDTO>;

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
}
