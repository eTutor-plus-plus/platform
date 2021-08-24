package at.jku.dke.etutor.helper;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

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
        this(DatasetFactory.createTxnMem());
    }

    /**
     * Constructor.
     *
     * @param dataset the dataset for the tests
     */
    public LocalRDFConnectionFactory(Dataset dataset) {
        createLuceneDataset(dataset);
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
        createLuceneDataset(DatasetFactory.createTxnMem());
    }

    /**
     * Returns whether a hashtag replacement is needed or not.
     *
     * @return {@code true} if a hashtag replacement is needed, otherwise {@code false}
     */
    @Override
    public boolean needsHashtagReplacement() {
        return false;
    }

    /**
     * Writes the local RDF dataset's default graph to the given file (for debug purposes only).
     *
     * @param path the path - must not be {@code null}
     * @throws IOException if an I/O related error occurs
     */
    @Deprecated
    public void writeDefaultGraph(File path) throws IOException {
        Objects.requireNonNull(path);

        try (OutputStream os = new FileOutputStream(path)) {
            RDFDataMgr.write(os, dataset.getDefaultModel(), RDFFormat.TURTLE);
        }
    }

    /**
     * Creates a lucene dataset around the given dataset.
     *
     * @param dataset the dataset which should be wrapped
     */
    private void createLuceneDataset(Dataset dataset) {
        @SuppressWarnings("deprecation")
        Directory luceneDirectory = new RAMDirectory();

        EntityDefinition entDef = new EntityDefinition("uri", "text", ETutorVocabulary.hasTaskHeader);
        entDef.setPrimaryPredicate(RDFS.label);
        entDef.setPrimaryPredicate(ETutorVocabulary.hasTaskGroupName);
        TextIndexConfig textIndexConfig = new TextIndexConfig(entDef);
        Analyzer analyzer = Util.getLocalizedAnalyzer("de");
        textIndexConfig.setAnalyzer(analyzer);
        textIndexConfig.setValueStored(true);

        this.dataset = TextDatasetFactory.createLucene(dataset, luceneDirectory, textIndexConfig);
    }
}
