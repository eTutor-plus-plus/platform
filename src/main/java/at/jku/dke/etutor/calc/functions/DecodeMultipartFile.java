package at.jku.dke.etutor.calc.functions;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class DecodeMultipartFile implements MultipartFile {

    private final byte [] content;

    public DecodeMultipartFile(byte[] content) {
        this.content = content;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOriginalFilename() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File file) throws IOException, IllegalStateException {
        new FileOutputStream(file).write(content);
    }
}
