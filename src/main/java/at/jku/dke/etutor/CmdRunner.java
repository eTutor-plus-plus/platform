package at.jku.dke.etutor;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.web.rest.vm.LoginVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
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
    private final ApplicationProperties properties;
    private final String cookieId;
    private final String taskGroupEndpoint;
    private final String taskAssignmentEndpoint;
    private final String origin;
    private final String creator;
    private final String maxPoints;
    private final String diagnoseLevelWeighting;
    private final String organisationUnit;
    private final String taskAssignmentType;

    private String authorizationToken;

    public CmdRunner(ApplicationProperties properties) {
        this.properties = properties;
        this.cookieId = properties.getCmd_runner().getCookie_id();
        this.creator = properties.getCmd_runner().getCreator();
        this.maxPoints = properties.getCmd_runner().getMax_points();
        this.diagnoseLevelWeighting = properties.getCmd_runner().getDiagnose_level_weighting();
        this.organisationUnit = properties.getCmd_runner().getOrganisation_unit();
        this.taskAssignmentType = properties.getCmd_runner().getTask_assignment_type();
        this.taskGroupEndpoint = "https://etutor.dke.uni-linz.ac.at/etutorpp/api/task-group";
        this.taskAssignmentEndpoint = "https://etutor.dke.uni-linz.ac.at/etutorpp/api/tasks/assignments";
        this.origin = "https://etutor.dke.uni-linz.ac.at";
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        authenticate();
        Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();

        try (Connection conn = DriverManager.getConnection(properties.getCmd_runner().getUrl(), properties.getCmd_runner().getUser(),
            properties.getCmd_runner().getPassword())) {

            // Select task groups from moodle db
            StopWatch stopWatch = StopWatch.createStarted();
            List<TaskGroupDTO> taskGroups = new ArrayList<>();

            try (Statement taskGroupStatement = conn.createStatement();
                 ResultSet set =
                     taskGroupStatement.executeQuery("""
                         select DISTINCT gr.id as groupId, gr.shortdescription, inter.text from mdl_question_etutor_exerc_group gr, mdl_question_etutor_internation inter
                         where gr.id in (select exercisegroupid from mdl_question_etutor where tasktypeid = 12)
                         AND gr.id = inter.recordid AND inter.tabletype = 'e' AND length(inter.text) > 0
                         order by gr.id
                         """)) {

                // add taskgroups via etutor++ endpoint
                while (set.next()) {
                    int id = set.getInt(1);
                    String groupDescription = set.getString(2);
                    String text = set.getString(3);

                    NewTaskGroupDTO newTaskGroupDTO = new NewTaskGroupDTO();
                    newTaskGroupDTO.setName(groupDescription);
                    newTaskGroupDTO.setDescription(text);
                    newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.NoTypeTaskGroup.getURI()); // Do not change

                    taskGroups.add(new TaskGroupDTO(id, groupDescription, text,
                        createNewTaskGroup(newTaskGroupDTO)));
                }
            }

            // Select tasks from mooodle db
            try (Statement stmt = conn.createStatement();
                 ResultSet set = stmt.executeQuery("""
                     select DISTINCT question.remoteid, question.exercisegroupid, inter.text, cats.name as category, mdl_q.name as qname from mdl_question_etutor question,
                     mdl_question_etutor_internation inter, mdl_question mdl_q, mdl_question_categories cats
                     where tasktypeid = 12 AND inter.tabletype = 'q' AND inter.recordid = question.questionid
                     and length(inter.text) > 0 AND question.questionid = mdl_q.id AND mdl_q.category = cats.id
                     order by question.id
                     """)) {

                // Add tasks via etutor++ endpoint
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
                    newTaskAssignmentDTO.setCreator(creator);
                    newTaskAssignmentDTO.setTaskDifficultyId(difficultyId);
                    newTaskAssignmentDTO.setOrganisationUnit(organisationUnit);
                    newTaskAssignmentDTO.setHeader(questionName);
                    newTaskAssignmentDTO.setInstruction(text);
                    newTaskAssignmentDTO.setMaxPoints(maxPoints);
                    newTaskAssignmentDTO.setDiagnoseLevelWeighting(diagnoseLevelWeighting);
                    newTaskAssignmentDTO.setTaskAssignmentTypeId(taskAssignmentType);
                    newTaskAssignmentDTO.setTaskIdForDispatcher(String.valueOf(remoteId));
                    newTaskAssignmentDTO.setTaskGroupId(taskGroup.getRdfTaskGroup().getId());

                    insertNewTaskAssignment(newTaskAssignmentDTO);
                }
            }

            stopWatch.stop();

            String time = stopWatch.formatTime();
            log.info("Import time: {}", time);
        }
    }

    /**
     * Authentication method which sets the authorization token
     * @throws JsonProcessingException on marshalling error
     */
    private void authenticate() throws JsonProcessingException {
        LoginVM login = new LoginVM();
        login.setUsername(properties.getCmd_runner().getEtutor_login());
        login.setPassword(properties.getCmd_runner().getEtutor_pw());
        login.setRememberMe(false);

        ObjectMapper mapper = new ObjectMapper();
        String body = "";
        body = mapper.writeValueAsString(login);


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", cookieId);
        headers.set("Origin", origin);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        HttpEntity<String> response = restTemplate.exchange("https://etutor.dke.uni-linz.ac.at/etutorpp/api/authenticate", HttpMethod.POST, entity, String.class);
        var authHeader = response.getHeaders().get("Authorization").get(0);
        authHeader = authHeader.substring(authHeader.indexOf("Bearer")+7).trim();
        this.authorizationToken = authHeader;
    }

    /**
     * Adds a new task group via the endpoint
     * @param newTaskGroupDTO the task group to add
     * @return the returned task group
     */
    private at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO createNewTaskGroup(NewTaskGroupDTO newTaskGroupDTO) {
        ObjectMapper mapper = new ObjectMapper();
        String body = "";
        try {
            body = mapper.writeValueAsString(newTaskGroupDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", cookieId);
        headers.set("Origin", origin);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        String response = restTemplate.exchange(taskGroupEndpoint, HttpMethod.POST, entity, String.class).getBody();
        try {
            return mapper.readValue(response, at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds a new task assignment via the endpoint
     * @param newTaskAssignmentDTO the task assignment to add
     */
    private void insertNewTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        ObjectMapper mapper = new ObjectMapper();
        String body = "";
        try {
            body = mapper.writeValueAsString(newTaskAssignmentDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizationToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", cookieId);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(taskAssignmentEndpoint, HttpMethod.POST, entity, String.class).getBody();
    }

    /**
     * Local representation of a task group
     */
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
