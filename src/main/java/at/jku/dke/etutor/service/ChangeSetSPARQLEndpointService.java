package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ChangeSetStatementType;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.ResourceVersionDTO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Service to handle ChangeSets.
 * ChangeSets capture the delta between two versions of a resource and describe the changes to resources.
 * Also see the vocabulary description for further info (<a href="https://vocab.org/changeset/">ChangeSet vocabulary</a>).
 */
@Service
public class ChangeSetSPARQLEndpointService extends AbstractSPARQLEndpointService {
    private static final String DELETE_CHANGESET_BY_RESOURCE_URI =
    """
       PREFIX etutor:            <http://www.dke.uni-linz.ac.at/etutorpp/>
       PREFIX purl:              <http://purl.org/vocab/changeset/schema#>
       PREFIX rdf:                 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       DELETE {
        ?changeSet ?predicate ?object .
        ?object ?p1 ?o1 .
       }
       WHERE {
        ?changeSet a purl:ChangeSet ;
                    purl:subjectOfChange ?resource ;
                    ?predicate ?object .

        OPTIONAL{
            ?object a rdf:Statement;
                    ?p1 ?o1 .
        }
       }
    """;

    private static final String QRY_CONSTRUCT_RESOURCE_BY_CHANGESET =
        """
            PREFIX etutor:            <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX purl:              <http://purl.org/vocab/changeset/schema#>
            PREFIX rdf:                 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

            CONSTRUCT { ?resource ?predicate ?object .
              ?changeSet purl:createdDate ?date .
              ?changeSet purl:changeReason ?changeReason .
              ?changeSet purl:creatorName ?creator .
              }
            WHERE {
              ?changeSet a purl:ChangeSet;
                            purl:subjectOfChange ?resource;
                            ?typeOfStatement ?statement ;
                            purl:createdDate ?date .
              ?statement rdf:predicate ?predicate ;
                                rdf:object ?object .
              OPTIONAL{
                ?changeSet purl:creatorName ?creator .
              }
              OPTIONAL{
                ?changeSet purl:changeReason ?changeReason .
              }
            }
            """;
    private static final String QRY_SELECT_CURRENT_CHANGESET_OF_RESOURCE =
        """
            PREFIX purl:              <http://purl.org/vocab/changeset/schema#>
            PREFIX xsd:                 <http://www.w3.org/2001/XMLSchema#>

            SELECT ?changeSet
            WHERE {
              ?changeSet purl:subjectOfChange ?resource;
                                    purl:createdDate ?date .
              FILTER(DATATYPE(?date) = xsd:dateTime)
            }
            ORDER BY DESC(?date)
            LIMIT 1
            """;

    private static final String QRY_SELECT_CHANGESETS_OF_RESOURCE =
        """
            PREFIX purl:              <http://purl.org/vocab/changeset/schema#>
            PREFIX xsd:                 <http://www.w3.org/2001/XMLSchema#>

            SELECT ?changeSet
            WHERE {
              ?changeSet purl:subjectOfChange ?resource;
                                    purl:createdDate ?date .
              FILTER(DATATYPE(?date) = xsd:dateTime)
            }
            ORDER BY DESC(?date)
            """;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public ChangeSetSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Constructs a ChangeSet for a resource.
     * The ChangeSet only captures statements in the model, where the subject corresponds to the resourceUri.
     * This is required to ensure consistency when reconstructing resources based on ChangeSets.
     * The creation date of the ChangeSet is set to the time this method is called.
     * The preceding ChangeSet of this ChangeSet is set to the most current ChangeSet; i.e. the ChangeSet with the most current
     * creationDate.
     * Clients are encouraged to also create a changeSet for the first creation of the resource,
     * with an empty model as the before-version.
     * Also, instead of deleting all changeSets for a resource by calling {@link #deleteChangeSetsOfResource(String)}, clients can also
     * create a ChangeSet with an empty model as the after-version.
     *
     * @param resourceUri     the uri of the resource for which a ChangeSet should be created
     * @param changeReason    the reason for the modification
     * @param creator         the creator of the ChangeSet
     * @param before          the old model, can be generated with {{@link #getModelOfResource(String)}}
     * @param after           the updated model, can be generated with {@link #getModelOfResource(String)}
     */
    void insertNewChangeSet(String resourceUri, String changeReason, String creator, Model before, Model after) {
        Objects.requireNonNull(before);
        Objects.requireNonNull(after);
        Objects.requireNonNull(resourceUri);

        Instant creationDate = Instant.now();

        // Change set model
        Model changeSetModel = ModelFactory.createDefaultModel();

        Resource changeSetResource = ETutorVocabulary.createTaskAssignmentChangeSetResourceOfModel(UUID.randomUUID().toString(), changeSetModel);

        // preceding change set
        Optional<Resource> precedingChangeSetResource = getCurrentChangeSetForResource(resourceUri);
        precedingChangeSetResource.ifPresent(resource -> changeSetResource.addProperty(ETutorVocabulary.precedingChangeSet, resource));

        // subject of change resource
        Resource subjectOfChangeResource = changeSetModel.createResource(resourceUri);
        changeSetResource.addProperty(ETutorVocabulary.subjectOfChange, subjectOfChangeResource);

        // changeDate
        changeSetResource.addProperty(ETutorVocabulary.createdDate, instantToRDFString(creationDate), XSDDatatype.XSDdateTime);

        // change Person
        changeSetResource.addProperty(ETutorVocabulary.creatorName, creator);

        // reason of change
        changeSetResource.addProperty(ETutorVocabulary.changeReason, changeReason);


        // statements
        var beforeIterator = before.listStatements();
        while (beforeIterator.hasNext()) {
            var statement = beforeIterator.nextStatement();
            if(!statement.getSubject().equals(subjectOfChangeResource)) {
                continue;
            }
            changeSetResource.addProperty(ETutorVocabulary.removal, getReifiedStatement(changeSetModel, statement));
        }
        var afterIterator = after.listStatements();
        while (afterIterator.hasNext()) {
            var statement = afterIterator.nextStatement();
            if(!statement.getSubject().equals(subjectOfChangeResource)) {
                continue;
            }
            changeSetResource.addProperty(ETutorVocabulary.addition, getReifiedStatement(changeSetModel, statement));
        }
        try (RDFConnection connection = getConnection()) {
            connection.load(changeSetModel);
        }
    }

    /**
     * Get a model with all triples where the uri which is passed to the method
     * is the subject. This method can be used by clients to generate the models required by {@link #insertNewChangeSet(String, String, String, Model, Model)}.
     *
     * @param resourceUri the uri of the resource
     * @return an  {@link Optional} that contains the {@link Model} with all the triples.
     */
    Optional<Model> getModelOfResource(String resourceUri) {
        Objects.requireNonNull(resourceUri);

        ParameterizedSparqlString query = new ParameterizedSparqlString("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o .}");
        query.setIri("?s", resourceUri);
        try (RDFConnection connection = getConnection()) {
            var resultModel = connection.queryConstruct(query.asQuery());
            return Optional.of(resultModel);
        }
    }


    /**
     * Returns all versions of a resource for which ChangeSets exist; i.e. ChangeSets, where the {@link ETutorVocabulary#subjectOfChange} equals the provided URI.
     * If no ChangeSets exist, a supplier can be used to generate one version.
     * Otherwise, all versions are returned.
     *
     * @param resourceUri the URI of the resource
     * @param currentVersionSupplier if no ChangeSet exists for the resource, the resource might be in its original state; the supplier can be used to supply this version.
     *                               Also, if multiple ChangeSets exists, the version supplied by this parameter is used instead of the most current changeset.
     * @param resourceToTargetTypeMapper a mapper that maps a resource to the target type
     * @return a {@link List} of {@link ResourceVersionDTO} that are composed of the target types provided by the mapper, and meta-information about the versions
     * @param <T> the target type for the resources
     */
    public <T> List<ResourceVersionDTO<T>> getAllVersionOfResource(String resourceUri, Supplier<T> currentVersionSupplier, Function<Resource, T> resourceToTargetTypeMapper) {
        Objects.requireNonNull(resourceUri);
        Objects.requireNonNull(currentVersionSupplier);
        Objects.requireNonNull(resourceToTargetTypeMapper);

        List<ResourceVersionDTO<T>> resultList = new ArrayList<>();

        var changeSetResourceList = getAllChangeSetsForResource(resourceUri);
        if (changeSetResourceList.isEmpty()) {
            ResourceVersionDTO<T> resourceVersionDTO = new ResourceVersionDTO<>();
            resourceVersionDTO.setVersion(currentVersionSupplier.get());
            resultList.add(resourceVersionDTO);
            return resultList;
        }

        for (Resource changeSetResource : changeSetResourceList) {
            var optionalVersion = getVersionOfResourceByChangeSetUri(resourceUri, changeSetResource.getURI(), resourceToTargetTypeMapper);
            optionalVersion.ifPresent(resultList::add);
        }

        // replace first version with current version provided by supplier
        if(!resultList.isEmpty()){
            resultList.get(0).setVersion(currentVersionSupplier.get());
        }
        return resultList;
    }

    /**
     * Delete all change-sets where the {@link ETutorVocabulary#subjectOfChange} equals the passed URI.
     *
     * Alternatively, one could construct a final ChangeSet with an empty model for "after", to keep the history of the resource.
     *
     * @param resourceUri The URI of the resource for which the ChangeSets should be deleted
     */
    public void deleteChangeSetsOfResource(String resourceUri){
        ParameterizedSparqlString query = new ParameterizedSparqlString(DELETE_CHANGESET_BY_RESOURCE_URI);
        query.setIri("?resource", resourceUri);

        try(RDFConnection connection = getConnection()){
            connection.update(query.asUpdate());
        }
    }

    // private region


    /**
     * Returns the version represented by the ChangeSet for the URI which is passed to the method; i.e. the version of the resource after the ChangeSet has been applied.
     * @param resourceUri the URI of the resource
     * @param changeSetUri the URI of the change set
     * @param resourceToTargetTypeMapper a mapper to transform the resource into the target type T
     * @return an {@link Optional} containing the {@link ResourceVersionDTO}, or an empty {@link Optional}
     */
    private <T> Optional<ResourceVersionDTO<T>> getVersionOfResourceByChangeSetUri(String resourceUri, String changeSetUri, Function<Resource,T> resourceToTargetTypeMapper) {
        Objects.requireNonNull(changeSetUri);
        Objects.requireNonNull(resourceToTargetTypeMapper);
        Objects.requireNonNull(resourceUri);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_CONSTRUCT_RESOURCE_BY_CHANGESET);
        query.setIri("?changeSet", changeSetUri);
        query.setIri("?resource", resourceUri);
        query.setIri("?typeOfStatement", ChangeSetStatementType.AFTER.getProperty().getURI());

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(query.asQuery());

            if (model.isEmpty()) {
                return Optional.empty();
            }

            Resource resource = model.getResource(resourceUri);
            Resource changeSetResource = model.getResource(changeSetUri);

            var mappedBaseType = resourceToTargetTypeMapper.apply(resource);
            var version = new ResourceVersionDTO<T>();
            version.setVersion(mappedBaseType);
            version.setChangeSetId(changeSetUri);

            var changeReasonResource = changeSetResource.getProperty(ETutorVocabulary.changeReason);
            if(changeReasonResource != null){
                version.setReasonOfChange(changeReasonResource.getString());
            }
            var creatorResource = changeSetResource.getProperty(ETutorVocabulary.creatorName);
            if(creatorResource != null){
                version.setCreator(creatorResource.getString());
            }
            var creationDateResource = changeSetResource.getProperty(ETutorVocabulary.createdDate);
            if(creationDateResource != null){
                version.setCreationDate(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(creationDateResource.getString()).toInstant());
            }

            return Optional.of(version);
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all ChangeSets for a resource; i.e. all ChangeSets where the {@link ETutorVocabulary#subjectOfChange} equals
     * the URI which is passed to the method.
     *
     * @param resourceUri the URI of the resource
     * @return a {@link List} of {@link Resource} representing the ChangeSets.
     */
    private List<Resource> getAllChangeSetsForResource(String resourceUri) {
        Objects.requireNonNull(resourceUri);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_SELECT_CHANGESETS_OF_RESOURCE);
        query.setIri("?resource", resourceUri);
        List<Resource> result = new ArrayList<>();
        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    result.add(solution.getResource("?changeSet"));
                }
                return result;
            }
        }
    }

    /**
     * Utility method that receives a statement and creates a reified resource that represents the statement,
     * where the subject, predicate and object of the statement present are
     * linked to the resource of type rdf:statement with rdf:subject, rdf:predicate and rdf:object.
     *
     * @param model     the model
     * @param statement the statement for reification
     * @return the reified resource
     */
    private Resource getReifiedStatement(Model model, Statement statement) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(statement);

        Resource resource = model.createResource();
        resource.addProperty(RDF.type, RDF.Statement);
        resource.addProperty(RDF.subject, statement.getSubject());
        resource.addProperty(RDF.predicate, statement.getPredicate());
        resource.addProperty(RDF.object, statement.getObject());
        return resource;
    }

    /**
     * Returns the most recent ChangeSet for the URI which is passed to the method.
     *
     * @param resourceUri the URI of the resource
     * @return an {@link Optional} containing the resource representing the ChangeSet as {@link Resource} , or an empty {@link Optional}.
     */
    private Optional<Resource> getCurrentChangeSetForResource(String resourceUri) {
        Objects.requireNonNull(resourceUri);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_SELECT_CURRENT_CHANGESET_OF_RESOURCE);
        query.setIri("?resource", resourceUri);
        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    return Optional.of(solution.getResource("?changeSet"));
                }

                return Optional.empty();
            }
        }
    }
}
