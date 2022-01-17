import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { TranslateService } from '@ngx-translate/core';
import { SERVER_API_URL } from '../../../app.constants';

/**
 * Service for posting submissions and requesting gradings from the dispatcher
 */
const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root',
})
export class AssignmentService {
  constructor(private http: HttpClient, private translateService: TranslateService) {}

  /**
   * Posts a submission to the dispatcher
   * @param submission the {@link SubmissionDTO}
   */
  postSubmission(submission: SubmissionDTO): Observable<SubmissionIdDTO> {
    const url = `${SERVER_API_URL}api/dispatcher/submission`;
    const headers = httpOptions.headers.append('Accept-Language', this.translateService.currentLang);
    const options = {
      headers,
    };
    return this.http.post<SubmissionIdDTO>(url, submission, options);
  }

  /**
   * Requests a {@link GradingDTO} from the dispatcher
   * @param submissionId the id of the submission/grading
   */
  getGrading(submissionId: SubmissionIdDTO): Observable<GradingDTO> {
    const url = `${SERVER_API_URL}api/dispatcher/grading/${submissionId.submissionId}`;
    return this.http.get<GradingDTO>(url);
  }
}
