import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
}
