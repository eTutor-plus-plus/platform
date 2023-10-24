package at.jku.dke.etutor.service.dto.fd;

public class FDSolve {
    private Long id;
    private String solution;

    public FDSolve() {
    }

    public FDSolve(Long id, String solution) {
        this.id = id;
        this.solution = solution;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public String toString() {
        return "FDSolve{" +
            "id=" + id +
            ", solution='" + solution + '\'' +
            '}';
    }
}
