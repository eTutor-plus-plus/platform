package at.jku.dke.etutor.service;

import at.jku.dke.etutor.calc.functions.DecodeMultipartFile;
import at.jku.dke.etutor.calc.models.RandomInstruction;
import at.jku.dke.etutor.calc.service.CorrectionService;
import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.repository.FileRepository;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import at.jku.dke.etutor.service.exception.StudentNotExistsException;
import at.jku.dke.etutor.calc.exception.WrongCalcParametersException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for upload file related operations.
 *
 * @author fne
 */
@Service
public class UploadFileService {

    private final FileRepository fileRepository;

    /**
     * Constructor.
     *
     * @param fileRepository the injected file repository
     */
    public UploadFileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Uploads a file.
     *
     * @param matriculationNumber the matriculation number
     * @param file                the multipart file to update
     * @param fileName            the optional file name
     * @return the internal file id
     * @throws StudentNotExistsException if the student does not exist
     * @throws IOException               if a file related error occurs
     */
    @Transactional
    public long uploadFile(String matriculationNumber, MultipartFile file, String fileName) throws StudentNotExistsException, IOException {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(file);

        String fileNameStr = fileName;

        if (ObjectUtils.isEmpty(fileNameStr)) {
            fileNameStr = file.getOriginalFilename();
        }
        String name = StringUtils.cleanPath(Objects.requireNonNull(fileNameStr));
        return fileRepository.uploadFile(matriculationNumber, name, file.getContentType(),
            file.getBytes(), file.getSize());
    }



    /**
     * @param calcInstructionFileId id of the calc instruction
     * @param calcSolutionFileId id of the calc solution
     * @param writerInstructionFileId id of the writer instruction
     * @param login string id of the student
     * @return Creates a randomised instruction and returns a list with the file ids (calc instruction, calc solution, writer instruction)
     */
    @Transactional
    public List<Long> createRandomInstruction (Long calcInstructionFileId, Long calcSolutionFileId, Long writerInstructionFileId, String login) throws WrongCalcParametersException {
        try {
            FileEntity calcInstructionFileOld = fileRepository.getById(calcInstructionFileId);
            FileEntity calcSolutionFileOld = fileRepository.getById(calcSolutionFileId);
            FileEntity writerInstructionFileOld = fileRepository.getById(writerInstructionFileId);

            InputStream calcInstructionStreamOld = new ByteArrayInputStream(calcInstructionFileOld.getContent());
            InputStream calcSolutionStreamOld = new ByteArrayInputStream(calcSolutionFileOld.getContent());
            InputStream writerInstructionStreamOld = new ByteArrayInputStream(writerInstructionFileOld.getContent());

            XSSFWorkbook workbookCalcInstructionOld = new XSSFWorkbook(calcInstructionStreamOld);
            XSSFWorkbook workbookCalcSolutionOld = new XSSFWorkbook(calcSolutionStreamOld);
            XWPFDocument documentWriterInstructionOld = new XWPFDocument(writerInstructionStreamOld);

            RandomInstruction randomInstruction = CorrectionService.createInstruction(documentWriterInstructionOld, workbookCalcInstructionOld, workbookCalcSolutionOld, login);

            XSSFWorkbook workbookCalcInstruction = randomInstruction.getInstructionCalc();
            XSSFWorkbook workbookCalcSolution = randomInstruction.getSolutionCalc();
            XWPFDocument documentWriterInstruction = randomInstruction.getInstructionWriter();

            ByteArrayOutputStream byteArrayOutputStreamCalcInstruction = new ByteArrayOutputStream();
            ByteArrayOutputStream byteArrayOutputStreamCalcSolution = new ByteArrayOutputStream();
            ByteArrayOutputStream byteArrayOutputStreamWriterInstruction = new ByteArrayOutputStream();

            workbookCalcInstruction.write(byteArrayOutputStreamCalcInstruction);
            workbookCalcSolution.write(byteArrayOutputStreamCalcSolution);
            documentWriterInstruction.write(byteArrayOutputStreamWriterInstruction);

            MultipartFile fileCalcInstruction = new DecodeMultipartFile(byteArrayOutputStreamCalcInstruction.toByteArray());
            MultipartFile fileCalcSolution = new DecodeMultipartFile(byteArrayOutputStreamCalcSolution.toByteArray());
            MultipartFile fileWriterInstruction = new DecodeMultipartFile(byteArrayOutputStreamWriterInstruction.toByteArray());

            String fileNameCalcInstruction = calcInstructionFileOld.getName().substring(0, calcInstructionFileOld.getName().lastIndexOf('.'))
                + "_"
                + login
                + calcInstructionFileOld.getName().substring(calcInstructionFileOld.getName().lastIndexOf('.'));
            String fileNameCalcSolution = calcSolutionFileOld.getName().substring(0, calcSolutionFileOld.getName().lastIndexOf('.'))
                + "_"
                + login
                + calcSolutionFileOld.getName().substring(calcSolutionFileOld.getName().lastIndexOf('.'));
            String fileNameWriterInstruction = writerInstructionFileOld.getName().substring(0, writerInstructionFileOld.getName().lastIndexOf('.'))
                + "_"
                + login
                + writerInstructionFileOld.getName().substring(writerInstructionFileOld.getName().lastIndexOf('.'));

            List<Long> returningList = new ArrayList<>();

            returningList.add(fileRepository.uploadFile(fileNameCalcInstruction, calcInstructionFileOld.getContentType(), fileCalcInstruction.getBytes(), fileCalcInstruction.getSize()));
            returningList.add(fileRepository.uploadFile(fileNameCalcSolution, calcSolutionFileOld.getContentType(), fileCalcSolution.getBytes(), fileCalcSolution.getSize()));
            returningList.add(fileRepository.uploadFile(fileNameWriterInstruction, writerInstructionFileOld.getContentType(), fileWriterInstruction.getBytes(), fileWriterInstruction.getSize()));

            return returningList;
        } catch (Exception e) {
            throw new WrongCalcParametersException();
        }
    }


    /**
     * Returns a stored file.
     *
     * @param fileId the internal file id
     * @return file entity
     * @throws FileNotExistsException if the file does not exist
     */
    @Transactional(readOnly = true)
    public FileEntity getFile(long fileId) throws FileNotExistsException {
        Optional<FileEntity> file = fileRepository.findById(fileId);

        if (file.isPresent()) {
            return file.get();
        }
        throw new FileNotExistsException();
    }

    /**
     * Deletes a file from the database.
     *
     * @param fileId the internal file id
     */
    @Transactional
    public void removeFile(long fileId) {
        fileRepository.deleteById(fileId);
    }

    /**
     * Retrieves the file meta data model.
     *
     * @param fileId the file id
     * @return {@code Optional} containing the {@link FileMetaDataModelDTO}
     */
    public Optional<FileMetaDataModelDTO> getFileMetaData(long fileId) {
        try {
            return Optional.of(fileRepository.getMetaDataOfFile(fileId));
        } catch (FileNotExistsException e) {
            return Optional.empty();
        }
    }
}
