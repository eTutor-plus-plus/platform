import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubmissionDTO } from 'app/dispatcher/entities/SubmissionDTO';
import { SubmissionIdDTO } from 'app/dispatcher/entities/SubmissionIdDTO';
import { GradingDTO } from 'app/dispatcher/entities/GradingDTO';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root',
})
export class AssignmentService {
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  postSubmission(submission: SubmissionDTO): Observable<SubmissionIdDTO> {
    const url = this.apiUrl + '/submission';
    return this.http.post<SubmissionIdDTO>(url, submission, httpOptions);
  }

  getGrading(submissionId: SubmissionIdDTO): Observable<GradingDTO> {
    const url = this.apiUrl + '/grading/' + submissionId.submissionId;
    return this.http.get<GradingDTO>(url);
  }
}
