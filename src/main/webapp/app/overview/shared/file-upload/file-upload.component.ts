import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FileUploadService } from 'app/overview/shared/file-upload/file-upload.service';
import { FormBuilder } from '@angular/forms';
import { IFileMetaDataModel } from 'app/overview/shared/file-upload/file.model';

/**
 * Component for handling file uploads.
 */
@Component({
  selector: 'jhi-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss'],
})
export class FileUploadComponent {
  @Output()
  public fileRemoved = new EventEmitter<number>();
  @Output()
  public fileAdded = new EventEmitter<number>();
  @Output()
  public fileModified = new EventEmitter<[number, number]>();

  public fileUploadGroup = this.fb.group({
    fileUpload: [null, []],
  });
  public fileMetaData?: IFileMetaDataModel;
  public isSaving = false;
  @Input()
  public disabled = false;

  private _fileId = -1;
  private _originalId = -1;
  private _fileRemoved = false;
  private _fileSelected = false;

  /**
   * Constructor.
   *
   * @param fileUploadService the injected file upload service
   * @param fb the injected form builder
   */
  constructor(private fileUploadService: FileUploadService, private fb: FormBuilder) {}

  /**
   * Sets the file id.
   *
   * @param value the value to set
   */
  @Input()
  public set fileId(value: number) {
    this._fileId = value;

    if (value > -1) {
      this._originalId = value;

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
   * Returns whether the file is set or not.
   */
  public isFileSet(): boolean {
    return this._fileId > -1;
  }

  /**
   * Event handler for a file change.
   */
  public fileChanged(): void {
    this._fileSelected = true;
  }

  /**
   * Downloads the current file.
   */
  public downloadCurrentFile(): void {
    this.fileUploadService.retrieveAndDownloadFile(this.fileId);
  }

  /**
   * Marks the current file as removed.
   */
  public removeCurrentFile(): void {
    this.fileId = -1;
    this._fileRemoved = true;
  }

  /**
   * Asynchronously saves the current file selection.
   */
  public async saveAsync(): Promise<void> {
    let newId = -1;

    this.isSaving = true;

    if (this._fileRemoved) {
      await this.fileUploadService.deleteFile(this._originalId).toPromise();
    }

    if (this._fileSelected) {
      const fileList = this.fileUploadGroup.get(['fileUpload'])!.value as FileList;
      const file = fileList.item(0)!;

      newId = await this.fileUploadService.uploadFile(file).toPromise();
    }

    if (this._fileRemoved && newId === -1) {
      this.fileRemoved.emit(this._originalId);
      this._fileRemoved = false;
    } else if (!this._fileRemoved && this._originalId < 0 && newId >= 0) {
      this.fileAdded.emit(newId);
    } else if (this._originalId >= 0 && this._originalId !== newId && newId >= 0) {
      this.fileModified.emit([this._originalId, newId]);
    }

    this.fileId = newId;

    this.isSaving = false;
  }
}
