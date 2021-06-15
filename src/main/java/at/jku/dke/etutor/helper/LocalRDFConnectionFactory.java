package at.jku.dke.etutor.helper;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Class which is used to created local RDF connections to an in-memory
 * dataset. For testing purposes only.
 *
 * @author fne
 */
public class LocalRDFConnectionFactory implements RDFConnectionFactory {

    private Dataset dataset;

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
        return org.apache.jena.rdfconnection.RDFConnectionFactory.connect(dataset);
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
        @SuppressWarnings("deprecation")
        Directory luceneDirectory = new RAMDirectory();

        List<Rule> ruleList;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("inference_rules.rules")))) {
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
        Dataset dataset = DatasetFactory.create(ModelFactory.createInfModel(genericRuleReasoner, ModelFactory.createDefaultModel()));
        this.dataset = TextDatasetFactory.createLucene(dataset, luceneDirectory, textIndexConfig);
    }
}
