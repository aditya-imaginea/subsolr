package com.subsolr.index;

import com.subsolr.config.DocumentConfigurationBean;
import com.subsolr.config.FieldConfigurationBean;
import com.subsolr.model.Record;
import com.subsolr.template.DocumentTemplate;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;
import org.apache.solr.schema.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Index Builder for all document definitions defined in documnet config xml.
 *
 * @author vamsiy-mac aditya
 */
public class IndexBuilder implements InitializingBean {

    private Version luceneVersion = null;
    private String luceneDirectoryPrefix = null;
    private DocumentConfigurationBean documentConfiguration = null;
    private FieldConfigurationBean fieldConfiguration = null;
    public static final Logger logger = LoggerFactory.getLogger(IndexBuilder.class);

    static final String[] propertyNames = {"indexed", "tokenized", "stored", "IndexProperties.BINARY", "omitNorms",
        "omitTermFreqAndPositions", "termVectors", "termPositions", "termOffsets", "multiValued", "sortMissingFirst",
        "sortMissingLast", "required", "omitPositions", "storeOffsetsWithPositions", "docValues"};

    public IndexBuilder(Version luceneVersion, String luceneDirectory,
            DocumentConfigurationBean documentContextProcessor, FieldConfigurationBean fieldConfiguration) {
        this.luceneDirectoryPrefix = luceneDirectory;
        this.documentConfiguration = documentContextProcessor;
        this.fieldConfiguration = fieldConfiguration;
        this.luceneVersion = luceneVersion;
    }
    
    
    
    public void writeDocument(List<Document> docs,String name) throws Exception {
        System.out.println("Writing document "+name);
        try (IndexWriter writer = IndexUtilities.getIndexWriterForDocument(name, luceneVersion, luceneDirectoryPrefix)) {
           for(Document doc : docs) {
                writer.addDocument(doc);
           }
            writer.commit();
        }
    }


    static int parseProperties(Map<String, ?> properties, boolean which, boolean failOnError) {
        int props = 0;
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                continue;
            }
            boolean boolVal = val instanceof Boolean ? (Boolean) val : Boolean.parseBoolean(val.toString());
            if (boolVal == which) {
                props |= propertyNameToInt(entry.getKey(), failOnError);
            }
        }
        return props;
    }

    static int propertyNameToInt(String name, boolean failOnError) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals(name)) {
                return 1 << i;
            }
        }
        if (failOnError && !"default".equals(name)) {
            throw new IllegalArgumentException("Invalid field property: " + name);
        } else {
            return 0;
        }
    }

    static int calcProps(String name, FieldType ft, Map<String, ?> props) {
        int trueProps = parseProperties(props, true, true);
        int falseProps = parseProperties(props, false, true);

        int p = 0;

        //
        // If any properties were explicitly turned off, then turn off other
        // properties
        // that depend on that.
        //
        if (on(falseProps, IndexConstants.STORED)) {
            int pp = IndexConstants.STORED | IndexConstants.BINARY;
            if (on(pp, trueProps)) {
                throw new RuntimeException("SchemaField: " + name + " conflicting stored field options:" + props);
            }
            p &= ~pp;
        }

        if (on(falseProps, IndexConstants.INDEXED)) {
            int pp = (IndexConstants.INDEXED | IndexConstants.STORE_TERMVECTORS | IndexConstants.STORE_TERMPOSITIONS | IndexConstants.STORE_TERMOFFSETS);
            if (on(pp, trueProps)) {
                throw new RuntimeException("SchemaField: " + name
                        + " conflicting 'true' field options for non-indexed field:" + props);
            }
            p &= ~pp;
        }

        if (on(falseProps, IndexConstants.INDEXED) && on(falseProps, IndexConstants.DOC_VALUES)) {
            int pp = (IndexConstants.SORT_MISSING_FIRST | IndexConstants.SORT_MISSING_LAST);
            if (on(pp, trueProps)) {
                throw new RuntimeException("SchemaField: " + name
                        + " conflicting 'true' field options for non-indexed/non-docValues field:" + props);
            }
            p &= ~pp;
        }

        if (on(falseProps, IndexConstants.INDEXED)) {
            int pp = (IndexConstants.OMIT_NORMS | IndexConstants.OMIT_TF_POSITIONS | IndexConstants.OMIT_POSITIONS);
            if (on(pp, falseProps)) {
                throw new RuntimeException("SchemaField: " + name
                        + " conflicting 'false' field options for non-indexed field:" + props);
            }
            p &= ~pp;

        }

        if (on(trueProps, IndexConstants.OMIT_TF_POSITIONS)) {
            int pp = (IndexConstants.OMIT_POSITIONS | IndexConstants.OMIT_TF_POSITIONS);
            if (on(pp, falseProps)) {
                throw new RuntimeException("SchemaField: " + name + " conflicting tf and position field options:" + props);
            }
            p &= ~pp;
        }

        if (on(falseProps, IndexConstants.STORE_TERMVECTORS)) {
            int pp = (IndexConstants.STORE_TERMVECTORS | IndexConstants.STORE_TERMPOSITIONS | IndexConstants.STORE_TERMOFFSETS);
            if (on(pp, trueProps)) {
                throw new RuntimeException("SchemaField: " + name + " conflicting termvector field options:" + props);
            }
            p &= ~pp;
        }

        // override sort flags
        if (on(trueProps, IndexConstants.SORT_MISSING_FIRST)) {
            p &= ~IndexConstants.SORT_MISSING_LAST;
        }

        if (on(trueProps, IndexConstants.SORT_MISSING_LAST)) {
            p &= ~IndexConstants.SORT_MISSING_FIRST;
        }

        p &= ~falseProps;
        p |= trueProps;
        return p;
    }

    static boolean on(int bitfield, int props) {
        return (bitfield & props) != 0;
    }

    static boolean off(int bitfield, int props) {
        return (bitfield & props) == 0;
    }

    public void rebuildIndexes() throws IOException {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, DocumentTemplate> documentDefinitions = documentConfiguration.getExtractor().getDocumentTemplatesByName();
        
        for (Entry<String, DocumentTemplate> documentEntryDefinition : documentDefinitions.entrySet()) {
            String doucmentName = documentEntryDefinition.getKey();
            DocumentTemplate documentTemplate = documentEntryDefinition.getValue();
            List<Record> recordsToBeIndexed = documentTemplate.getRecordsToBeIndexed();
            
            writeDocument(IndexUtilities.getDocument(recordsToBeIndexed, doucmentName, fieldConfiguration),doucmentName);
        }

    }

}
