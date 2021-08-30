package at.jku.dke.etutor.service.dto.courseinstance.taskassignment;

public class TaskPointEntryDTO {
    private double maxPoints;
    private double points;
    private String taskHeader;
    private String taskUrl;


    public TaskPointEntryDTO(){

    }

    public TaskPointEntryDTO(double maxPoints, double points, String taskHeader, String taskUrl){
        this.maxPoints=maxPoints;
        this.points=points;
        this.taskHeader=taskHeader;
        this.taskUrl=taskUrl;
    }

    public double getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(double maxPoints) {
        this.maxPoints = maxPoints;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public String getTaskHeader() {
        return taskHeader;
    }

    public void setTaskHeader(String taskHeader) {
        this.taskHeader = taskHeader;
    }

    public String getTaskUrl() {
        return taskUrl;
    }

    public void setTaskUrl(String taskUUID) {
        this.taskUrl = taskUUID;
    }
}
