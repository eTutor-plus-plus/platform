package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.UploadFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

    /**
     * Constructor.
     *
     * @param uploadFileService the injected upload file service
     */
    public FileResource(UploadFileService uploadFileService) {
        this.uploadFileService = uploadFileService;
    }

    /**
     * REST endpoint for uploading files.
     *
     * @param file the file to upload
     * @return {@link ResponseEntity} containing the file's id
     */
    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Long> postFile(@RequestParam("file") MultipartFile file) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        try {
            long id = uploadFileService.uploadFile(matriculationNumber, file);

            return ResponseEntity.ok(id);
        } catch (at.jku.dke.etutor.service.exception.StudentNotExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * REST endpoint for retrieving files.
     *
     * @param id the internal file id
     * @return {@link ResponseEntity} containing the file resource
     */
    @GetMapping("{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Resource> getFile(@PathVariable long id) {

        try {
            FileEntity entity = uploadFileService.getFile(id);

            ByteArrayResource resource = new ByteArrayResource(entity.getContent());
            HttpHeaders headers = new HttpHeaders();

            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + entity.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, entity.getContentType());

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(entity.getSize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (at.jku.dke.etutor.service.exception.FileNotExistsException e) {
            e.printStackTrace();
            return null;
        }
    }
}
