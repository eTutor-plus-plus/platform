import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import {
  ICourseInstanceInformationDTO,
  ICourseInstanceProgressOverviewDTO,
  IStudentFullNameInfoDTO,
  IStudentInfoDTO,
  IStudentTaskListInfoDTO,
} from './students.model';
import { map } from 'rxjs/operators';

/**
 * Service for managing students.
 */
@Injectable({
  providedIn: 'root',
})
export class StudentService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Returns all available students.
   */
  public getAvailableStudents(): Observable<IStudentFullNameInfoDTO[]> {
    return this.http.get<IStudentInfoDTO[]>(`${SERVER_API_URL}api/student`).pipe(
      map(students =>
        students.map(x => ({
          matriculationNumber: x.matriculationNumber,
          firstName: x.firstName,
          lastName: x.lastName,
          fullName: x.firstName + ' ' + x.lastName,
        }))
      )
    );
  }

  /**
   * Retrieves the courses of the currently logged-in student.
   */
  public getCourseInstancesOfLoggedInStudent(): Observable<ICourseInstanceInformationDTO[]> {
    return this.http.get<ICourseInstanceInformationDTO[]>(`${SERVER_API_URL}api/student/courses`);
  }

  /**
   * Returns the progress on course assignments from a given course of the currently logged-in
   * student.
   *
   * @param courseInstanceId the course instance URI
   */
  public getStudentCourseInstanceProgressOverview(courseInstanceId: string): Observable<ICourseInstanceProgressOverviewDTO[]> {
    const uuid = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);
    return this.http.get<ICourseInstanceProgressOverviewDTO[]>(`${SERVER_API_URL}api/student/courses/${uuid}/progress`);
  }

  /**
   * Returns the exercise sheet tasks.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet uuid
   */
  public getExerciseSheetTasks(courseInstanceId: string, exerciseSheetUUID: string): Observable<HttpResponse<IStudentTaskListInfoDTO[]>> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<IStudentTaskListInfoDTO[]>(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/list`,
      { observe: 'response' }
    );
  }

  /**
   * Marks the given task as submitted.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet uuid
   * @param taskNo the task number
   */
  public markTaskAsSubmitted(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.post(
      `${SERVER_API_URL}/api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/task/${taskNo}/submit`,
      null
    );
  }

  /**
   * Returns whether the task is submitted or not.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet uuid
   * @param taskNo the task number
   */
  public isTaskSubmitted(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number): Observable<boolean> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<boolean>(
      `${SERVER_API_URL}/api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/task/${taskNo}/submitted`
    );
  }

  /**
   * Opens the given exercise sheet.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet uuid
   */
  public openExerciseSheet(courseInstanceId: string, exerciseSheetUUID: string): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.post(`${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/open`, null);
  }

  /**
   * Returns whether a new task can be assigned or not
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet uuid
   */
  public canAssignNextTask(courseInstanceId: string, exerciseSheetUUID: string): Observable<boolean> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<boolean>(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/can-assign-new-task`
    );
  }

  /**
   * Assigns a new task.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet uuid
   */
  public assignNewTask(courseInstanceId: string, exerciseSheetUUID: string): Observable<boolean> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.post<boolean>(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/assign-new-task`,
      null
    );
  }

  /**
   * Removes an upload task's file attachment.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task no
   * @param fileId the file id
   */
  public removeUploadTaskAttachment(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number, fileId: number): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.delete(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/uploadTask/${taskNo}/${fileId}`
    );
  }

  /**
   * Sets the upload task's attachment.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task no
   * @param fileId the file id
   */
  public setUploadTaskAttachment(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number, fileId: number): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.put(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/uploadTask/${taskNo}/${fileId}`,
      undefined
    );
  }

  /**
   * Returns the file attachment id.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task no
   */
  public getFileAttachmentId(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number): Observable<number> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<number>(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/uploadTask/${taskNo}/file-attachment`
    );
  }

  /**
   * Sets the submitted solution for the task
   * @param courseInstanceId the course instance
   * @param exerciseSheetUUID the exercise sheet
   * @param taskNo the task number
   * @param submission the submission to be set
   */
  public setSubmissionForAssignment(
    courseInstanceId: string,
    exerciseSheetUUID: string,
    taskNo: number,
    submission: string
  ): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.put(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/${taskNo}/submission`,
      submission
    );
  }

  public getSubmissionForAssignment(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<any>(`${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/${taskNo}/submission`, {
      responseType: 'text' as 'json',
    });
  }
  /**
   * Sets the points assigned by the dispatcher.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task no
   * @param points the points
   */
  public setDispatcherPoints(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number, points: number): Observable<any> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.put(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/${taskNo}/${points}`,
      undefined
    );
  }

  /**
   * Returns points assigned by the dispatcher.
   *
   * @param courseInstanceId the course instance id
   * @param exerciseSheetUUID the exercise sheet UUID
   * @param taskNo the task no
   */
  public getDispatcherPoints(courseInstanceId: string, exerciseSheetUUID: string, taskNo: number): Observable<number> {
    const instanceUUID = courseInstanceId.substr(courseInstanceId.lastIndexOf('#') + 1);

    return this.http.get<number>(
      `${SERVER_API_URL}api/student/courses/${instanceUUID}/exercises/${exerciseSheetUUID}/${taskNo}/dispatcherpoints`
    );
  }
}
