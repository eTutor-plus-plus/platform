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

    /**
     * Getter for the field <code>fuseki</code>
     *
     * @return a {@link ApplicationProperties.Fuseki} object
     */
    public Fuseki getFuseki() {
        return fuseki;
    }

    /**
     * Configuration class for the fuseki connection.
     */
    public static class Fuseki {

        private String baseUrl = "http://localhost:3030";
        private String inferenceDatasetName = "etutorpp-database-inference";
        private String originalDatasetName = "etutorpp-database";

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

        /**
         * Returns the inference dataset's name
         *
         * @return the inference dataset's name
         */
        public String getInferenceDatasetName() {
            return inferenceDatasetName;
        }

        /**
         * Sets the inference dataset's name
         *
         * @param inferenceDatasetName the name to set
         */
        public void setInferenceDatasetName(String inferenceDatasetName) {
            this.inferenceDatasetName = inferenceDatasetName;
        }

        /**
         * Returns the original dataset's name.
         *
         * @return the original datset's name
         */
        public String getOriginalDatasetName() {
            return originalDatasetName;
        }

        /**
         * Sets the original dataset's name.
         *
         * @param originalDatasetName the name to set
         */
        public void setOriginalDatasetName(String originalDatasetName) {
            this.originalDatasetName = originalDatasetName;
        }
    }
}
