package at.jku.dke.etutor.service.dto.fd;

import java.util.List;

public class FDTaskSolve {
    private String id;
    private String type;
    private String solution;
    private List<FDSolve> closureSolutions;
    private List<FDSolve> normalFormSolutions;
    private int maxPoints;

    public FDTaskSolve() {
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public List<FDSolve> getClosureSolutions() {
        return closureSolutions;
    }

    public void setClosureSolutions(List<FDSolve> closureSolutions) {
        this.closureSolutions = closureSolutions;
    }

    public List<FDSolve> getNormalFormSolutions() {
        return normalFormSolutions;
    }

    public void setNormalFormSolutions(List<FDSolve> normalFormSolutions) {
        this.normalFormSolutions = normalFormSolutions;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    @Override
    public String toString() {
        return "FDTaskSolve{" +
            "id='" + id + '\'' +
            ", type='" + type + '\'' +
            ", solution='" + solution + '\'' +
            ", closureSolutions=" + closureSolutions +
            ", normalFormSolutions=" + normalFormSolutions +
            ", maxPoints="+ maxPoints +
            '}';
    }
}


