package at.jku.dke.etutor.service;

import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;

/**
 * Class that daily closes assigned exercise sheets that are past the deadline.
 */
@Component
public class ExerciseSheetClosingScheduler {
    private final ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;
    private final CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    public ExerciseSheetClosingScheduler(ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService,
                                         CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService){
       this.exerciseSheetSPARQLEndpointService = exerciseSheetSPARQLEndpointService;
       this.courseInstanceSPARQLEndpointService = courseInstanceSPARQLEndpointService;
    }

    /**
     * Closes all exercise sheets that are assigned to course-instances,
     * configured to close automatically and past the deadline.
     * @throws ParseException -
     */
    @Scheduled(cron = "0 5 0 * * ?", zone = "UTC")
    public void closeOverdueExerciseSheets() throws ParseException {
        List<ExerciseSheetDTO> sheetsWithDeadline = exerciseSheetSPARQLEndpointService.getExerciseSheetsWithDeadline();
        List<ExerciseSheetDTO> sheetsToClose = sheetsWithDeadline.stream()
            .filter(sheet -> Instant.now().isAfter(sheet.getDeadline()))
            .toList();
        for(var sheet : sheetsToClose){
            courseInstanceSPARQLEndpointService.closeExerciseSheetIfAssigned(sheet.getId());
        }
    }
}
