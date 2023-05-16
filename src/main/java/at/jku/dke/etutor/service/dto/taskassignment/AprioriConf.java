package at.jku.dke.etutor.service.dto.taskassignment;

/**
 * DTO class for apriori configuration for front end
 *
 */
public class AprioriConf {

	String baseUrl;
	
	String key;

	public AprioriConf(String baseUrl, String key) {
		super();
		this.baseUrl = baseUrl;
		this.key = key;
	}

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

	@Override
	public String toString() {
		return "AprioriConf [baseUrl=" + baseUrl + ", key=" + key + "]";
	}
	
	
}
