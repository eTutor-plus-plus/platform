package at.jku.dke.etutor.service.dto.courseinstance.taskassignment;

/**
 * Represents information about the achieved points and max points for an individual task
 */
public class TaskPointEntryDTO {
    /**
     * The matriculation number
     */
    private String matriculationNo;
    /**
     * The maximum points
     */
    private double maxPoints;
    /**
     * The achieved points
     */
    private double points;
    /**
     * The task´s header
     */
    private String taskHeader;
    /**
     * The task´s UUID
     */
    private String taskUUID;

    /**
     * Empty constructor
     */
    public TaskPointEntryDTO(){

    }

    /**
     * The constructor
     * @param matriculationNo the matriculation number
     * @param maxPoints the max points
     * @param points the achieved points
     * @param taskHeader the task´s header
     * @param taskUUID the tasks´s UUID
     */
    public TaskPointEntryDTO(String matriculationNo, double maxPoints, double points, String taskHeader, String taskUUID){
        this.matriculationNo=matriculationNo;
        this.maxPoints=maxPoints;
        this.points=points;
        this.taskHeader=taskHeader;
        this.taskUUID=taskUUID;
    }

    /**
     * Returns the max points
     * @return the max points
     */
    public double getMaxPoints() {
        return maxPoints;
    }

    /**
     * Sets the max points
     * @param maxPoints the max points
     */
    public void setMaxPoints(double maxPoints) {
        this.maxPoints = maxPoints;
    }

    /**
     * Returns the achieved points
     * @return the achieved points
     */
    public double getPoints() {
        return points;
    }

    /**
     * Sets the achieved points
     * @param points the achieved points
     */
    public void setPoints(double points) {
        this.points = points;
    }

    /**
     * Returns the task´s header
     * @return the task´s header
     */
    public String getTaskHeader() {
        return taskHeader;
    }

    /**
     * Sets the task´s header
     * @param taskHeader the task´s header
     */
    public void setTaskHeader(String taskHeader) {
        this.taskHeader = taskHeader;
    }

    /**
     * Returns the task´s UUID
     * @return the task´s UUID
     */
    public String getTaskUUID() {
        return taskUUID;
    }

    /**
     * Sets the task´s UUID
     * @param taskUUID the task´s UUID
     */
    public void setTaskUUID(String taskUUID) {
        this.taskUUID = taskUUID;
    }

    /**
     * Returns the student´s matriculation number
     * @return the matriculation number
     */
    public String getMatriculationNo() {
        return matriculationNo;
    }

    /**
     * Sets the student´s matriculation number
     * @param matriculationNo the matriculation number
     */
    public void setMatriculationNo(String matriculationNo) {
        this.matriculationNo = matriculationNo;
    }
}
