import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import { IFileMetaDataModel } from 'app/overview/shared/file-upload/file.model';
import { map } from 'rxjs/operators';

/**
 * Service for providing file upload functionality.
 */
@Injectable({
  providedIn: 'root',
})
export class FileUploadService {
  /**
   * Constructor.
   *
   * @param http the injected http client
   */
  constructor(private http: HttpClient) {}

  /**
   * Uploads a file.
   *
   * @param file the file object
   */
  public uploadFile(file: File): Observable<number> {
    const formData = new FormData();
    formData.append('file', new Blob([file], { type: file.type }));
    formData.append('fileName', file.name);

    return this.http.post<number>(`${SERVER_API_URL}api/files`, formData);
  }

  /**
   * Deletes a file.
   *
   * @param fileId file file id
   */
  public deleteFile(fileId: number): Observable<any> {
    return this.http.delete(`${SERVER_API_URL}api/files/${fileId}`);
  }

  /**
   * Retrieves and also downloads the given file.
   *
   * @param fileId the file id
   */
  public retrieveAndDownloadFile(fileId: number): void {
    this.http.get(`${SERVER_API_URL}api/files/${fileId}`, { responseType: 'blob', observe: 'response' }).subscribe(contentFromService => {
      const type = contentFromService.headers.get('X-Content-Type')!;
      const fileName = contentFromService.headers.get('X-Filename')!;

      const blob = new Blob([contentFromService.body!], { type });

      const objectUrl: string = URL.createObjectURL(blob);
      const a: HTMLAnchorElement = document.createElement('a');

      a.href = objectUrl;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();

      document.body.removeChild(a);
      URL.revokeObjectURL(objectUrl);
    });
  }

  /**
   * Retrieves the requested file's meta data.
   *
   * @param fileId the file id
   */
  public getFileMetaData(fileId: number): Observable<IFileMetaDataModel> {
    return this.http.get<IFileMetaDataModel>(`${SERVER_API_URL}api/files/${fileId}/metadata`).pipe(
      map(x => {
        x.submissionDate = new Date(x.submissionDate);
        return x;
      })
    );
  }
}
