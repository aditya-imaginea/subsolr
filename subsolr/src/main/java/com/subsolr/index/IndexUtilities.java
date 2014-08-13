package com.subsolr.index;

import com.subsolr.config.FieldConfigurationBean;
import static com.subsolr.index.IndexBuilder.calcProps;
import static com.subsolr.index.IndexBuilder.logger;
import com.subsolr.model.Record;
import com.subsolr.template.FieldTemplate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;

public final class IndexUtilities {

    
    public static List<Document> getDocument(List<Record> records, String docName, FieldConfigurationBean fieldConfiguration) throws Exception {
        List<Document> docs = new ArrayList<Document>();
        for (Record record : records) {
            Document doc = new Document();
            if(record.getName().equals("city")) {
                System.out.println("City");
            }
            Map<String, String> valueByFieldName = record.getValueByFieldName();
            for (Map.Entry<String, String> fieldDefEntry : valueByFieldName.entrySet()) {
                if (null != fieldDefEntry.getValue()) {
                    FieldTemplate fieldDefinition = fieldConfiguration
                            .getFieldDefinitionsByName(fieldDefEntry.getKey());
                    if(fieldDefinition==null) {
                        System.out.println(fieldDefEntry.getKey());
                        continue;
                    }
                    logger.debug("processing fieldDefinition -  " + fieldDefinition.getFieldName());
                    Class<? extends FieldType> fieldTypeClassName = fieldDefinition.getFieldTypeDefinition()
                            .getFieldTypeClassName();
                    FieldType fieldType = fieldTypeClassName.newInstance();
                    List<Analyzer> analyzer = fieldDefinition.getFieldTypeDefinition().getAnalyzer();
                    if (!analyzer.isEmpty()) {
                        fieldType.setAnalyzer(analyzer.get(0));
                        fieldType.setIsExplicitAnalyzer(true);
                    }
                    SchemaField schemaField = new SchemaField(fieldDefinition.getFieldName(), fieldType, calcProps(
                            fieldDefinition.getFieldName(), fieldType, fieldDefinition.getFieldProperties()), "");
                    IndexableField field = schemaField.createField(fieldDefEntry.getValue(), 1.0f);
                    
                    doc.add(field);
                    
                }
                
            }
            docs.add(doc);
        }
        return docs;
    }

    public static IndexWriter getIndexWriterForDocument(String documentName, Version luceneVersion,
            String luceneDirectoryPrefix) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer(luceneVersion);
        Directory index = FSDirectory.open(new File(luceneDirectoryPrefix + File.separator + documentName));
        IndexWriterConfig config = new IndexWriterConfig(luceneVersion, analyzer);
        IndexWriter indexWriter = new IndexWriter(index, config);
        return indexWriter;
    }
}
