import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubmissionDTO } from 'app/overview/dispatcher/entities/SubmissionDTO';
import { SubmissionIdDTO } from 'app/overview/dispatcher/entities/SubmissionIdDTO';
import { GradingDTO } from 'app/overview/dispatcher/entities/GradingDTO';
import { TranslateService } from '@ngx-translate/core';
import { SERVER_API_URL } from '../../../app.constants';
import { PmLogModel } from '../assignment-pm/PmLogDTO';

/**
 * Service for posting submissions and requesting gradings from the dispatcher, using the proxy of the platform.
 */
const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root',
})
export class DispatcherAssignmentService {
  constructor(private http: HttpClient, private translateService: TranslateService) {}

  /**
   * Posts a submission to the dispatcher
   * @param submission the {@link SubmissionDTO}
   */
  postSubmission(submission: SubmissionDTO): Observable<SubmissionIdDTO> {
    let url: string | undefined = undefined;
    if (submission.taskType === 'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#BpmnTask') {
      url = `${SERVER_API_URL}api/bpmn/dispatcher/submission`;
    } else {
      url = `${SERVER_API_URL}api/dispatcher/submission`;
    }
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
    let url: string | undefined = undefined;
    if (submissionId.isBpmnTask !== undefined) {
      url = `${SERVER_API_URL}api/bpmn/dispatcher/grading/${submissionId.submissionId}`;
    } else {
      url = `${SERVER_API_URL}api/dispatcher/grading/${submissionId.submissionId}`;
    }
    return this.http.get<GradingDTO>(url);
  }

  /**
   * Requests a {@link PmLogModel} from the dispatcher
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task number
   */
  public getPmLogForIndividualTask(
    courseInstanceId: string,
    exerciseSheetUUID: string,
    taskNo: number,
    taskAssignmentId: string
  ): Observable<PmLogModel> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);
    return this.http.get<PmLogModel>(
      `api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/task/${taskNo}/taskassignment/${taskAssignmentId}/pmlog`
    );
  }

  /**
   * Request exists already in student-service.ts => therefore no use for this request right now
   * Request the information if a task is already submitted
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUI
   * @param taskNo the task no
   */
  public isTaskSubmitted(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number): Observable<boolean> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);
    return this.http.get<boolean>(`api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/task/${taskNo}/submitted`);
  }
}
