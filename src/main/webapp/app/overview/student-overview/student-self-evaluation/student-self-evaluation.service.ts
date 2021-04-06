import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

/**
 * Service for the student self evaluation.
 */
@Injectable({
  providedIn: 'root',
})
export class StudentSelfEvaluationService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}
}
