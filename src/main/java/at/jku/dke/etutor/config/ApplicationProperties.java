package at.jku.dke.etutor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Etutor Plus Plus.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Fuseki fuseki = new Fuseki();
    private final CmdRunnerConfig cmd_runner = new CmdRunnerConfig();
    private final Dispatcher dispatcher = new Dispatcher();
    private final BpmnDispatcher bpmnDispatcher = new BpmnDispatcher();

    /**
     * Getter for the field <code>fuseki</code>
     *
     * @return a {@link ApplicationProperties.Fuseki} object
     */
    public Fuseki getFuseki() {
        return fuseki;
    }

    public CmdRunnerConfig getCmd_runner() {
        return cmd_runner;
    }

    /**
     * Getter for the field <code>dispatcher</code>
     *
     * @return a {@link ApplicationProperties.Dispatcher} object
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Getter for the field <code>dispatcher</code>
     *
     * @return a {@link ApplicationProperties.Dispatcher} object
     */
    public BpmnDispatcher getBpmnDispatcher() {
        return bpmnDispatcher;
    }

    /**
     * Configuration class for the fuseki connection.
     */
    public static class Fuseki {

        private String baseUrl = "http://localhost:3030/etutorpp-database";

        /**
         * Returns the base url for the fuseki connection
         *
         * @return the base url for the fuseki connection
         */
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * Sets the base url for the fuseki connection.
         *
         * @param baseUrl the base url to set
         */
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class CmdRunnerConfig {
        private String password;
        private String user;
        private String url;
        private String etutor_login;
        private String etutor_pw;
        private String cookie_id;
        private String creator;
        private String max_points;
        private String diagnose_level_weighting;
        private String organisation_unit;
        private String task_assignment_type;

        public String getEtutor_login() {
            return etutor_login;
        }

        public void setEtutor_login(String etutor_login) {
            this.etutor_login = etutor_login;
        }

        public String getEtutor_pw() {
            return etutor_pw;
        }

        public void setEtutor_pw(String etutor_pw) {
            this.etutor_pw = etutor_pw;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getCookie_id() {
            return cookie_id;
        }

        public void setCookie_id(String cookie_id) {
            this.cookie_id = cookie_id;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getMax_points() {
            return max_points;
        }

        public void setMax_points(String max_points) {
            this.max_points = max_points;
        }

        public String getDiagnose_level_weighting() {
            return diagnose_level_weighting;
        }

        public void setDiagnose_level_weighting(String diagnose_level_weighting) {
            this.diagnose_level_weighting = diagnose_level_weighting;
        }

        public String getOrganisation_unit() {
            return organisation_unit;
        }

        public void setOrganisation_unit(String organisation_unit) {
            this.organisation_unit = organisation_unit;
        }

        public String getTask_assignment_type() {
            return task_assignment_type;
        }

        public void setTask_assignment_type(String task_assignment_type) {
            this.task_assignment_type = task_assignment_type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
    /**
     * Configuration class for the dispatcher connection
     */
    public static class Dispatcher{
        private String url = "http://localhost:8081";
        private String xqueryXmlFileUrlPrefix;
        private String sqlTableUrlPrefix;
        private String datalogFactsUrlPrefix;

        /**
         * Returns the url for the dispatcher connection
         *
         * @return the url for the dispatcher connection
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the url for the dispatcher connection
         *
         * @param url the url for the dispatcher connection
         */
        public void setUrl(String url) {
            this.url = url;
        }


        public String getXqueryXmlFileUrlPrefix() {
            return xqueryXmlFileUrlPrefix;
        }

        public void setXqueryXmlFileUrlPrefix(String xqueryXmlFileUrlPrefix) {
            this.xqueryXmlFileUrlPrefix = xqueryXmlFileUrlPrefix;
        }

        public String getSqlTableUrlPrefix() {
            return sqlTableUrlPrefix;
        }

        public void setSqlTableUrlPrefix(String sqlTableUrlPrefix) {
            this.sqlTableUrlPrefix = sqlTableUrlPrefix;
        }

        public String getDatalogFactsUrlPrefix() {
            return datalogFactsUrlPrefix;
        }

        public void setDatalogFactsUrlPrefix(String datalogFactsUrlPrefix) {
            this.datalogFactsUrlPrefix = datalogFactsUrlPrefix;
        }
    }

    /**
     * Configuration class for the dispatcher connection
     */
    public static class BpmnDispatcher {
        private String url = "http://localhost:8084";

        /**
         * Returns the url for the dispatcher connection
         *
         * @return the url for the dispatcher connection
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the url for the dispatcher connection
         *
         * @param url the url for the dispatcher connection
         */
        public void setUrl(String url) {
            this.url = url;
        }
    }
}


