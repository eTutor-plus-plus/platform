package at.jku.dke.etutor.service.dto.student;

/**
 * DTO class (record) for a student's task list entry.
 */
public record StudentTaskListInfoDTO(int orderNo, String taskId, boolean graded, boolean goalCompleted,
                                     String taskHeader, boolean submitted) {
}
