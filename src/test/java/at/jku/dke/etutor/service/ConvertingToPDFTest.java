package at.jku.dke.etutor.service;

import at.jku.dke.etutor.IntegrationTest;
import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@IntegrationTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConvertingToPDFTest {
    @Autowired
    private StudentService studentService;

    @Test
    @Transactional
    void testPDFCreation() {
        var list = new ArrayList<TaskAssignmentDTO>();
        var assignment = new TaskAssignmentDTO();
        assignment.setHeader("Important task");
        assignment.setInstruction("Hic laudatio laudatio");
        assignment.setCreator("admin");
        assignment.setCreationDate(Instant.now());
        assignment.setCreator("admin");
        assignment.setOrganisationUnit("dke");
        list.add(assignment);
        var id = studentService.generatePdfExerciseSheet("", "admin", list, null);
    }

}
