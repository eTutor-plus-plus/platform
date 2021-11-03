import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { TranslateService } from '@ngx-translate/core';

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

  postSubmission(submission: SubmissionDTO): Observable<SubmissionIdDTO> {
    const url = 'api/dispatcher/submission';
    const headers = httpOptions.headers.append('Accept-Language', this.translateService.currentLang);
    const options = {
      headers,
    };
    return this.http.post<SubmissionIdDTO>(url, submission, options);
  }

  getGrading(submissionId: SubmissionIdDTO): Observable<GradingDTO> {
    const url = 'api/dispatcher/grading/' + submissionId.submissionId;
    return this.http.get<GradingDTO>(url);
  }
}
