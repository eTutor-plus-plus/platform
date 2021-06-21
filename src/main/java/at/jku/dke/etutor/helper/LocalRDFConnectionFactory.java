package at.jku.dke.etutor.helper;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

/**
 * Class which is used to created local RDF connections to an in-memory
 * dataset. For testing purposes only.
 *
 * @author fne
 */
public class LocalRDFConnectionFactory implements RDFConnectionFactory {

    private Dataset inferenceDataset;
    private Dataset originalDataset;

    /**
     * Constructor.
     */
    public LocalRDFConnectionFactory() {
        createLuceneDataset();
    }

    /**
     * Returns the newly created RDF connection.
     *
     * @return the newly created RDF connection
     */
    @Override
    public RDFConnection getRDFConnection() {
        return org.apache.jena.rdfconnection.RDFConnectionFactory.connect(inferenceDataset);
    }

    /**
     * Returns the rdf connection which is used to access
     * the original dataset (without inference)
     *
     * @return the connection
     */
    @Override
    public RDFConnection getRDFConnectionToOriginalDataset() {
        return org.apache.jena.rdfconnection.RDFConnectionFactory.connect(originalDataset);
    }

    /**
     * Clears the dataset (only works in embedded mode).
     */
    @Override
    public void clearDataset() {
        createLuceneDataset();
    }

    /**
     * Creates a lucene dataset around the given dataset.
     */
    private void createLuceneDataset() {
        if (this.originalDataset != null) {
            this.originalDataset.close();
        }
        if (this.inferenceDataset != null) {
            this.inferenceDataset.close();
        }

        @SuppressWarnings("deprecation")
        Directory luceneDirectory = new RAMDirectory();

        List<Rule> ruleList;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
            getClass().getResourceAsStream("inference_rules.rules"))))) {
            ruleList = Rule.parseRules(Rule.rulesParserFromReader(reader));
        } catch (IOException e) {
            throw new IllegalArgumentException("Must not happen!");
        }

        GenericRuleReasoner genericRuleReasoner = new GenericRuleReasoner(ruleList);

        EntityDefinition entDef = new EntityDefinition("uri", "text", ETutorVocabulary.hasTaskHeader);
        entDef.setPrimaryPredicate(RDFS.label);
        TextIndexConfig textIndexConfig = new TextIndexConfig(entDef);
        Analyzer analyzer = Util.getLocalizedAnalyzer("de");
        textIndexConfig.setAnalyzer(analyzer);
        textIndexConfig.setValueStored(true);
        Model baseModel = ModelFactory.createDefaultModel();
        this.originalDataset = DatasetFactory.create(baseModel);
        Dataset dataset = DatasetFactory.create(ModelFactory.createInfModel(genericRuleReasoner, ModelFactory.createDefaultModel()));
        this.inferenceDataset = TextDatasetFactory.createLucene(dataset, luceneDirectory, textIndexConfig);
    }
}
