package at.jku.dke.etutor;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.pro.packaged.O;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Commandline runner for the import.
 */
@Component
public class CmdRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CmdRunner.class);
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    public CmdRunner(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moodle", "<user>",
            "pwd")) {
            // Select task groups
            StopWatch stopWatch = StopWatch.createStarted();
            List<TaskGroupDTO> taskGroups = new ArrayList<>();

            try (Statement taskGroupStatement = conn.createStatement();
                 ResultSet set =
                     taskGroupStatement.executeQuery("""
                         select DISTINCT gr.id as groupId, gr.shortdescription, inter.text from mdl_question_etutor_exerc_group gr, mdl_question_etutor_internation inter
                         where gr.id in (select exercisegroupid from mdl_question_etutor where tasktypeid = 1)
                         AND gr.id = inter.recordid AND inter.tabletype = 'e' AND length(inter.text) > 0
                         order by gr.id
                         """)) {

                while (set.next()) {
                    int id = set.getInt(1);
                    String groupDescription = set.getString(2);
                    String text = set.getString(3);

                    NewTaskGroupDTO newTaskGroupDTO = new NewTaskGroupDTO();
                    newTaskGroupDTO.setName(groupDescription);
                    newTaskGroupDTO.setDescription(text);
                    newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.SQLTypeTaskGroup.getURI());

                    taskGroups.add(new TaskGroupDTO(id, groupDescription, text,
                        createNewTaskGroup(newTaskGroupDTO, "admin")));
                }
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet set = stmt.executeQuery("""
                     select DISTINCT question.remoteid, question.exercisegroupid, inter.text, cats.name as category, mdl_q.name as qname from mdl_question_etutor question,
                     mdl_question_etutor_internation inter, mdl_question mdl_q, mdl_question_categories cats
                     where tasktypeid = 1 AND inter.tabletype = 'q' AND inter.recordid = question.questionid
                     and length(inter.text) > 0 AND question.questionid = mdl_q.id AND mdl_q.category = cats.id
                     order by question.id
                     """)) {

                while (set.next()) {
                    int remoteId = set.getInt(1);
                    int exerciseGroupId = set.getInt(2);
                    String text = set.getString(3);
                    String difficultyText = set.getString(4);
                    String questionName = set.getString(5);

                    String difficultyId;

                    if (difficultyText.trim().equalsIgnoreCase("einfach")) {
                        difficultyId = ETutorVocabulary.Easy.getURI();
                    } else if (difficultyText.trim().equalsIgnoreCase("mittel")) {
                        difficultyId = ETutorVocabulary.Medium.getURI();
                    } else {
                        difficultyId = ETutorVocabulary.Hard.getURI();
                    }

                    TaskGroupDTO taskGroup = StreamEx.of(taskGroups).findFirst(x -> x.id == exerciseGroupId).get();

                    NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
                    newTaskAssignmentDTO.setCreator("admin");
                    newTaskAssignmentDTO.setTaskDifficultyId(difficultyId);
                    newTaskAssignmentDTO.setOrganisationUnit("DKE");
                    newTaskAssignmentDTO.setHeader(questionName);
                    newTaskAssignmentDTO.setInstruction(text);
                    newTaskAssignmentDTO.setMaxPoints("10");
                    newTaskAssignmentDTO.setDiagnoseLevelWeighting("1");
                    newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.SQLTask.getURI());
                    newTaskAssignmentDTO.setTaskIdForDispatcher(String.valueOf(remoteId));
                    newTaskAssignmentDTO.setTaskGroupId(taskGroup.getRdfTaskGroup().getId());

                    insertNewTaskAssignment(newTaskAssignmentDTO, "admin");
                }
            }

            stopWatch.stop();

            String time = stopWatch.formatTime();
            log.info("Import time: {}", time);
        }
    }

    private at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO createNewTaskGroup(NewTaskGroupDTO newTaskGroupDTO, String admin) {
        ObjectMapper mapper = new ObjectMapper();
        String body = "";
        try {
            body = mapper.writeValueAsString(newTaskGroupDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }


        String token = "<insert-token>";
        String url = "https://etutor.dke.uni-linz.ac.at/etutorpp/api/task-group";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", "MOODLEID_=<insert-id>");
        headers.set("Origin", "https://etutor.dke.uni-linz.ac.at");
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        try {
            return mapper.readValue(response, at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void insertNewTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO, String admin) {
        ObjectMapper mapper = new ObjectMapper();
        String body = "";
        try {
            body = mapper.writeValueAsString(newTaskAssignmentDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }


        String token = "<insert-token>";
        String url = "https://etutor.dke.uni-linz.ac.at/etutorpp/api/tasks/assignments";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", "MOODLEID_=<insert-id>");
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }


    public static class TaskGroupDTO {
        private int id;
        private String groupDescription;
        private String text;
        private at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO rdfTaskGroup;

        public TaskGroupDTO(int id, String groupDescription, String text,
                            at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO rdfTaskGroup) {
            this.id = id;
            this.groupDescription = groupDescription;
            this.text = text;
            this.rdfTaskGroup = rdfTaskGroup;
        }

        public int getId() {
            return id;
        }

        public String getGroupDescription() {
            return groupDescription;
        }

        public String getText() {
            return text;
        }

        public at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO getRdfTaskGroup() {
            return rdfTaskGroup;
        }
    }
}
