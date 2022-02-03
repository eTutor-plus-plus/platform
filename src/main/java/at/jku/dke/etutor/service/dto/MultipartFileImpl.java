package at.jku.dke.etutor.service.dto;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * Implementation of the {@link MultipartFile} interface for the creation of PDF-Exercise Sheets
 */
public class MultipartFileImpl implements MultipartFile {
    private final ByteArrayOutputStream outputStream;
    private final byte[] outputStreamByteArray;
    private final String contentType;

    public MultipartFileImpl(ByteArrayOutputStream outputStream, String contentType){
        this.outputStream = outputStream;
        this.outputStreamByteArray = outputStream.toByteArray();
        this.contentType = contentType;
    }

    @Override
    public @NotNull String getName() {
        return "";
    }

    @Override
    public String getOriginalFilename() {
        return "";
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return outputStream.size() == 0;
    }

    @Override
    public long getSize() {
        return outputStream.size();
    }

    @Override
    public byte @NotNull [] getBytes() throws IOException {
        return outputStreamByteArray;
    }

    @Override
    public @NotNull InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(outputStreamByteArray);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        // Not required for implementation
    }
}
