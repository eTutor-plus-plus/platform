package at.jku.dke.etutor.service.dto.student;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * DTO class (record) for a student's task list entry.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record StudentTaskListInfoDTO(int orderNo, String taskId, boolean graded, boolean goalCompleted,
                                     String taskHeader, boolean submitted) {
}
