/**
 * Interface that represents file meta data.
 */
export interface IFileMetaDataModel {
  /**
   * The file name.
   */
  fileName: string;
  /**
   * The content type.
   */
  contentType: string;
  /**
   * The submission date.
   */
  submissionDate: Date;
}
