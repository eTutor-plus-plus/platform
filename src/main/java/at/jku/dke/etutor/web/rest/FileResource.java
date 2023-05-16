package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.UploadFileService;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.dto.taskassignment.AprioriConf;
import at.jku.dke.etutor.web.rest.errors.EmptyFileNotAllowedException;
import at.jku.dke.etutor.web.rest.errors.FileStorageException;
import at.jku.dke.etutor.web.rest.errors.StudentNotExistsException;
import com.google.common.net.UrlEscapers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for managing file uploads.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api/files")
public class FileResource {

	private final UploadFileService uploadFileService;
	/** start apriori */
//	@Value("${application.apriori.baseUrl}")
	private final String aprioriUrl;

//	@Value("${application.apriori.key}")
	private final String aprioriKey;
	/** apriori end */

	/**
	 * Constructor.
	 *
	 * @param uploadFileService the injected upload file service
	 */
	public FileResource(UploadFileService uploadFileService, ApplicationProperties properties) {
		this.uploadFileService = uploadFileService;
        this.aprioriKey = properties.getApriori().getKey();
        this.aprioriUrl = properties.getApriori().getBaseUrl();
	}


	/** start apriori */

	/**
	 * REST endpoint for retrieving apriori configuration
	 * @return	ResponseEntity containing apriori configuration
	 */
	@GetMapping(path = "/apriori", produces = { MediaType.APPLICATION_JSON_VALUE })
	@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR
			+ "\")")
	public ResponseEntity<AprioriConf> getAprioriConf() {
		return ResponseEntity.status(HttpStatus.OK).body(new AprioriConf(aprioriUrl, aprioriKey));
	}

	/** apriori end */

    /**
     * REST endpoint for uploading files.
     *
     * @param file     the file to upload
     * @param fileName the optional file name
     * @return {@link ResponseEntity} containing the file's id
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Long> postFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "fileName", defaultValue = "", required = false) String fileName) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        if (file.isEmpty()) {
            throw new EmptyFileNotAllowedException();
        }

        try {
            long id = uploadFileService.uploadFile(matriculationNumber, file, fileName);

            return ResponseEntity.ok(id);
        } catch (at.jku.dke.etutor.service.exception.StudentNotExistsException e) {
            throw new StudentNotExistsException();
        } catch (IOException e) {
            throw new FileStorageException();
        }
    }

    /**
     * REST endpoint for retrieving files.
     *
     * @param id the internal file id
     * @return {@link ResponseEntity} containing the file resource
     */
    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Resource> getFile(@PathVariable long id) {

        try {
            FileEntity entity = uploadFileService.getFile(id);

            ByteArrayResource resource = new ByteArrayResource(entity.getContent());
            HttpHeaders headers = new HttpHeaders();

            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + UrlEscapers.urlFragmentEscaper().escape(entity.getName()).replace(",", "_"));
            headers.add(HttpHeaders.CONTENT_TYPE, entity.getContentType());
            headers.add("X-Filename", entity.getName());
            headers.add("X-Content-Type", entity.getContentType());

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(entity.getSize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (at.jku.dke.etutor.service.exception.FileNotExistsException e) {
            throw new StudentNotExistsException();
        }
    }

    /**
     * Returns the request file's meta data.
     *
     * @param id the file id
     * @return {@link ResponseEntity} containing the meta data dto
     */
    @GetMapping("{id}/metadata")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<FileMetaDataModelDTO> getFileMetaData(@PathVariable long id) {
        return ResponseEntity.of(uploadFileService.getFileMetaData(id));
    }

    /**
     * REST endpoint for removing files.
     *
     * @param id the internal file id
     * @return empty {@link ResponseEntity}
     */
    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteFile(@PathVariable long id) {
        uploadFileService.removeFile(id);
        return ResponseEntity.noContent().build();
    }
}
