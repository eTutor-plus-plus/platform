package at.jku.dke.etutor.service.dto.fd;

public class FDependenciesDTO {
    String [] leftSide;
    String [] rightSide;

    public FDependenciesDTO() {
    }

    public FDependenciesDTO(String[] leftSide, String[] rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public String[] getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(String[] leftSide) {
        this.leftSide = leftSide;
    }

    public String[] getRightSide() {
        return rightSide;
    }

    public void setRightSide(String[] rightSide) {
        this.rightSide = rightSide;
    }
}
