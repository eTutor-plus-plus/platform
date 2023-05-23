package at.jku.dke.etutor.service.dto.fd;

import java.util.Set;

public class FDExerciseDTO {
    Long id;
    Set<FDependenciesDTO> dependencies;


    public FDExerciseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<FDependenciesDTO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<FDependenciesDTO> dependencies) {
        this.dependencies = dependencies;
    }
}
