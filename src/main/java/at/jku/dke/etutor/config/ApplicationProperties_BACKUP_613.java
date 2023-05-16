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
    private final Dispatcher dispatcher = new Dispatcher();

    private final Apriori apriori = new Apriori();

    public Apriori getApriori() {
		return apriori;
	}

	/**
     * Getter for the field <code>fuseki</code>
     *
     * @return a {@link ApplicationProperties.Fuseki} object
     */
    public Fuseki getFuseki() {
        return fuseki;
    }

    /**
     * Getter for the field <code>dispatcher</code>
     *
     * @return a {@link ApplicationProperties.Dispatcher} object
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public Apriori getApriori() {
        return apriori;
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

    public static class Apriori {
        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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

        private String baseUrl;


        public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

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
<<<<<<< HEAD
    
    
    public static class Apriori{
    	
    	private String baseUrl="http://localhost:8085";
    	private String key;
    	
		public String getBaseUrl() {
			return baseUrl;
		}
		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
    	
    	

    }
=======
>>>>>>> 61c549e1a5486141cb3556bae671ea1eb8ca9822
}
