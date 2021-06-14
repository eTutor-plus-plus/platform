import { Component, Input } from '@angular/core';
import { FileUploadService } from 'app/overview/shared/file-upload/file-upload.service';
import { IFileMetaDataModel } from 'app/overview/shared/file-upload/file.model';

/**
 * Component for displaying a downloadable file attachment
 */
@Component({
  selector: 'jhi-file-download',
  templateUrl: './file-download.component.html',
  styleUrls: ['./file-download.component.scss'],
})
export class FileDownloadComponent {
  public fileMetaData?: IFileMetaDataModel;

  private _fileId = -1;

  /**
   * Constructor.
   *
   * @param fileUploadService the injected file upload service
   */
  constructor(private fileUploadService: FileUploadService) {}

  /**
   * Sets the file id.
   *
   * @param value the value to set
   */
  @Input()
  public set fileId(value: number) {
    this._fileId = value;

    if (value > -1) {
      (async () => {
        this.fileMetaData = await this.fileUploadService.getFileMetaData(value).toPromise();
      })();
    } else {
      this.fileMetaData = undefined;
    }
  }

  /**
   * Returns the file id.
   */
  public get fileId(): number {
    return this._fileId;
  }

  /**
   * Downloads the current file.
   */
  public downloadCurrentFile(): void {
    this.fileUploadService.retrieveAndDownloadFile(this.fileId);
  }
}
