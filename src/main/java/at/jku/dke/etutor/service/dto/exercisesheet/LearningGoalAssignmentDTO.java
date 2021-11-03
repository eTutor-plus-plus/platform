package at.jku.dke.etutor.service.dto.exercisesheet;

import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * DTO class which represents a learning goal assignment.
 *
 * @author fne
 */
public class LearningGoalAssignmentDTO {
    @NotNull
    private LearningGoalDisplayDTO learningGoal;
    @Min(1)
    private int priority;

    /**
     * Constructor.
     */
    public LearningGoalAssignmentDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param learningGoal the associated learning goal
     * @param priority     the associated priority
     */
    public LearningGoalAssignmentDTO(LearningGoalDisplayDTO learningGoal, int priority) {
        this.learningGoal = learningGoal;
        this.priority = priority;
    }

    /**
     * Returns the associated learning goal.
     *
     * @return the associated learning goal
     */
    public LearningGoalDisplayDTO getLearningGoal() {
        return learningGoal;
    }

    /**
     * Sets the associated learning goal.
     *
     * @param learningGoal the associated learning goal to set
     */
    public void setLearningGoal(LearningGoalDisplayDTO learningGoal) {
        this.learningGoal = learningGoal;
    }

    /**
     * Returns the priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     *
     * @param priority the priority to set (must not be less than 1)
     */
    public void setPriority(int priority) {
        if (priority < 1) {
            throw new IllegalArgumentException("The priority must not be less than 1");
        }

        this.priority = priority;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param   o   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     * @see     #hashCode()
     * @see     java.util.HashMap
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LearningGoalAssignmentDTO that = (LearningGoalAssignmentDTO) o;

        return new EqualsBuilder().append(priority, that.priority).append(learningGoal, that.learningGoal).isEquals();
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link java.lang.Object#equals(java.lang.Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @implSpec
     * As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     *
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.lang.System#identityHashCode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(learningGoal).append(priority).toHashCode();
    }
}
