package at.jku.dke.etutor.service;

import at.jku.dke.etutor.IntegrationTest;
import at.jku.dke.etutor.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
        var id = studentService.generatePdfExerciseSheet("admin", null);
    }

}
