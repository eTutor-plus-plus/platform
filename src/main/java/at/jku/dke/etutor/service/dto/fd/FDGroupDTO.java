package at.jku.dke.etutor.service.dto.fd;

import java.util.Set;

public class FDGroupDTO {
    Long id;
    Set<FDependenciesDTO> functionalDependencies;


    public FDGroupDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<FDependenciesDTO> getFunctionalDependencies() {
        return functionalDependencies;
    }

    public void setFunctionalDependencies(Set<FDependenciesDTO> functionalDependencies) {
        this.functionalDependencies = functionalDependencies;
    }
}
