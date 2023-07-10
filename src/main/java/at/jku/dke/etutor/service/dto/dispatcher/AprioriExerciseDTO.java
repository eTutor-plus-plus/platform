package at.jku.dke.etutor.service.dto.dispatcher;

public class AprioriExerciseDTO {
	
	private String aprioriDatasetId;

	public AprioriExerciseDTO() {
		
	}
	
	public AprioriExerciseDTO(String aprioriDatasetId) {
		this.aprioriDatasetId = aprioriDatasetId;
	}

	public String getAprioriDatasetId() {
		return aprioriDatasetId;
	}

	public void setAprioriDatasetId(String aprioriDatasetId) {
		this.aprioriDatasetId = aprioriDatasetId;
	}
	
	
	
}
