package at.jku.dke.etutor.domain.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * RDF vocabulary classes.
 */
public final class ETutorVocabulary {

    private static final String CLASS_GOAL = "Goal";
    private static final String CLASS_SUB_GOAL = "SubGoal";

    private static final String PROP_IS_PRIVATE = "isPrivate";
    private static final String PROP_DEPENDS_ON = "dependsOn";
    private static final String PROP_HAS_DESCRIPTION = "hasDescription";
    private static final String PROP_HAS_CHANGE_DATE = "hasChangeDate";
    private static final String PROP_HAS_OWNER = "hasOwner";
    private static final String PROP_HAS_SUB_GOAL = "hasSubGoal";

    /**
     * The namespace of the vocabulary
     */
    public static final String URI = "http://www.dke.uni-linz.ac.at/etutorpp/";

    private static final Model m = ModelFactory.createDefaultModel();

    /**
     * The hasDescription property.
     */
    public static final Property hasDescription = m.createProperty(URI + PROP_HAS_DESCRIPTION);
    /**
     * The hasChangeDate property.
     */
    public static final Property hasChangeDate = m.createProperty(URI + PROP_HAS_CHANGE_DATE);
    /**
     * The hasOwner property.
     */
    public static final Property hasOwner = m.createProperty(URI + PROP_HAS_OWNER);
    /**
     * The isPrivate property.
     */
    public static final Property isPrivate = m.createProperty(URI + PROP_IS_PRIVATE);
    /**
     * The dependsOn property.
     */
    public static final Property dependsOn = m.createProperty(URI + PROP_DEPENDS_ON);
    /**
     * The hasSubGoal property.
     */
    public static final Property hasSubGoal = m.createProperty(URI + PROP_HAS_SUB_GOAL);

    /**
     * The goal resource.
     */
    public static final Resource Goal = m.createResource(URI + CLASS_GOAL);
    /**
     * The sub goal resource.
     */
    public static final Resource SubGoal = m.createResource(URI + CLASS_SUB_GOAL);

    /**
     * Creates an individual goal resource.
     *
     * @param userLogin the login of the user
     * @param goalName  the rdf encoded goal name
     * @return the individual goal resource
     */
    public static final Resource createUserGoalResource(String userLogin, String goalName) {
        return createUserGoalResourceOfModel(userLogin, goalName, m);
    }

    /**
     * Creates an individual goal resource from a given model..
     *
     * @param userLogin the login of the user
     * @param goalName  the rdf encoded goal name
     * @param model     the base model of the resource
     * @return the individual goal resource
     */
    public static final Resource createUserGoalResourceOfModel(String userLogin, String goalName, Model model) {
        return model.createResource(URI + userLogin + "/" + CLASS_GOAL + "#" + goalName);
    }
}
