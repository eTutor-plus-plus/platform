package at.jku.dke.etutor.service.tasktypes.implementation;

import at.jku.dke.etutor.calc.service.CorrectionService;
import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.repository.FileRepository;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class CalcService implements TaskTypeService {
    private final FileRepository fileRepository;
    public CalcService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }


    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException {
        if (!newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.CalcTask.toString())) return;

        FileEntity calcInstructionFile = fileRepository.findById((long) newTaskAssignmentDTO.getCalcInstructionFileId()).orElse(new FileEntity());
        FileEntity calcSolutionFile = fileRepository.findById((long) newTaskAssignmentDTO.getCalcSolutionFileId()).orElse(new FileEntity());
        FileEntity writerInstructionFile = fileRepository.findById((long) newTaskAssignmentDTO.getWriterInstructionFileId()).orElse(new FileEntity());
        InputStream calcInstructionStream = new ByteArrayInputStream(calcInstructionFile.getContent());
        InputStream calcSolutionStream = new ByteArrayInputStream(calcSolutionFile.getContent());
        InputStream writerInstructionStream = new ByteArrayInputStream(writerInstructionFile.getContent());
        System.out.println();

        try {
            System.out.println();
            XSSFWorkbook workbookCalcInstruction = new XSSFWorkbook(calcInstructionStream);
            XSSFWorkbook workbookCalcSolution = new XSSFWorkbook(calcSolutionStream);
            XWPFDocument documentWriterInstruction = new XWPFDocument(writerInstructionStream);
            System.out.println();

            CorrectionService.createInstruction(documentWriterInstruction, workbookCalcInstruction, workbookCalcSolution, "asdf");
            System.out.println();
        }catch (Exception e) {
            throw new MissingParameterException(e.getMessage());
        }
    }

    @Override
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        try {
            createTask(taskAssignmentDTO);
        } catch (NotAValidTaskGroupException e) {
            throw new MissingParameterException(e.getMessage());
        }
    }

    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        // no functionality provided
    }
}
