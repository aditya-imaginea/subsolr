package com.subsolr.index;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.subsolr.contextprocessor.DocumentContextProcessor;
import com.subsolr.contextprocessor.FieldContextProcessor;
import com.subsolr.contextprocessor.model.DocumentDefinition;
import com.subsolr.contextprocessor.model.FieldDefinition;
import com.subsolr.entityprocessors.model.Record;

/**
 * Index Builder for all document definitions defined in documnet config xml
 * 
 * @author vamsiy-mac aditya
 */
public class IndexBuilder implements InitializingBean {

   private Version luceneVersion = null;
   private String luceneDirectoryPrefix = null;
   private DocumentContextProcessor documentContextProcessor = null;
   private FieldContextProcessor fieldContextProcessor = null;
   public static final Logger logger = LoggerFactory.getLogger(IndexBuilder.class);

   static final String[] propertyNames = { "indexed", "tokenized", "stored", "IndexProperties.BINARY", "omitNorms",
         "omitTermFreqAndPositions", "termVectors", "termPositions", "termOffsets", "multiValued", "sortMissingFirst",
         "sortMissingLast", "required", "omitPositions", "storeOffsetsWithPositions", "docValues" };

   public IndexBuilder(Version luceneVersion, String luceneDirectory,
         DocumentContextProcessor documentContextProcessor, FieldContextProcessor fieldContextProcessor) {
      this.luceneDirectoryPrefix = luceneDirectory;
      this.documentContextProcessor = documentContextProcessor;
      this.fieldContextProcessor = fieldContextProcessor;
      this.luceneVersion = luceneVersion;
   }

   public void indexRecordsForDocument(List<Record> recordLists, String documentName) throws IOException,
         IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException,
         SecurityException, NoSuchMethodException {
      IndexWriter writer = IndexUtilities.getIndexWriterForDocument(documentName, luceneVersion, luceneDirectoryPrefix);
      logger.debug("processing documentName -  " + documentName);

      for (Record record : recordLists) {
         Document doc = new Document();
         Map<String, String> valueByFieldName = record.getValueByFieldName();
         for (Entry<String, String> fieldDefEntry : valueByFieldName.entrySet()) {
            if (null != fieldDefEntry.getValue()) {
               FieldDefinition fieldDefinition = fieldContextProcessor
                     .getFieldDefinitionsByName(fieldDefEntry.getKey());
               logger.debug("processing fieldDefinition -  " + fieldDefinition.getFieldName());
               Class<? extends FieldType> fieldTypeClassName = fieldDefinition.getFieldTypeDefinition()
                     .getFieldTypeClassName();
               FieldType fieldType = fieldTypeClassName.newInstance();
               List<Analyzer> analyzer = fieldDefinition.getFieldTypeDefinition().getAnalyzer();
               if (analyzer.size() != 0) {
                  fieldType.setAnalyzer(analyzer.get(0));
                  fieldType.setIsExplicitAnalyzer(true);
               }
               SchemaField schemaField = new SchemaField(fieldDefinition.getFieldName(), fieldType, calcProps(
                     fieldDefinition.getFieldName(), fieldType, fieldDefinition.getFieldProperties()), "");
               IndexableField field = schemaField.createField(fieldDefEntry.getValue(), 1.0f);
               doc.add(field);
            }

         }
         writer.addDocument(doc);
         writer.commit();
         logger.debug("processing done for  documentName -  " + documentName);

      }
      writer.close();

   }

   static int parseProperties(Map<String, ?> properties, boolean which, boolean failOnError) {
      int props = 0;
      for (Map.Entry<String, ?> entry : properties.entrySet()) {
         Object val = entry.getValue();
         if (val == null)
            continue;
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
      if (on(falseProps, IndexProperties.STORED)) {
         int pp = IndexProperties.STORED | IndexProperties.BINARY;
         if (on(pp, trueProps)) {
            throw new RuntimeException("SchemaField: " + name + " conflicting stored field options:" + props);
         }
         p &= ~pp;
      }

      if (on(falseProps, IndexProperties.INDEXED)) {
         int pp = (IndexProperties.INDEXED | IndexProperties.STORE_TERMVECTORS | IndexProperties.STORE_TERMPOSITIONS | IndexProperties.STORE_TERMOFFSETS);
         if (on(pp, trueProps)) {
            throw new RuntimeException("SchemaField: " + name
                  + " conflicting 'true' field options for non-indexed field:" + props);
         }
         p &= ~pp;
      }

      if (on(falseProps, IndexProperties.INDEXED) && on(falseProps, IndexProperties.DOC_VALUES)) {
         int pp = (IndexProperties.SORT_MISSING_FIRST | IndexProperties.SORT_MISSING_LAST);
         if (on(pp, trueProps)) {
            throw new RuntimeException("SchemaField: " + name
                  + " conflicting 'true' field options for non-indexed/non-docValues field:" + props);
         }
         p &= ~pp;
      }

      if (on(falseProps, IndexProperties.INDEXED)) {
         int pp = (IndexProperties.OMIT_NORMS | IndexProperties.OMIT_TF_POSITIONS | IndexProperties.OMIT_POSITIONS);
         if (on(pp, falseProps)) {
            throw new RuntimeException("SchemaField: " + name
                  + " conflicting 'false' field options for non-indexed field:" + props);
         }
         p &= ~pp;

      }

      if (on(trueProps, IndexProperties.OMIT_TF_POSITIONS)) {
         int pp = (IndexProperties.OMIT_POSITIONS | IndexProperties.OMIT_TF_POSITIONS);
         if (on(pp, falseProps)) {
            throw new RuntimeException("SchemaField: " + name + " conflicting tf and position field options:" + props);
         }
         p &= ~pp;
      }

      if (on(falseProps, IndexProperties.STORE_TERMVECTORS)) {
         int pp = (IndexProperties.STORE_TERMVECTORS | IndexProperties.STORE_TERMPOSITIONS | IndexProperties.STORE_TERMOFFSETS);
         if (on(pp, trueProps)) {
            throw new RuntimeException("SchemaField: " + name + " conflicting termvector field options:" + props);
         }
         p &= ~pp;
      }

      // override sort flags
      if (on(trueProps, IndexProperties.SORT_MISSING_FIRST)) {
         p &= ~IndexProperties.SORT_MISSING_LAST;
      }

      if (on(trueProps, IndexProperties.SORT_MISSING_LAST)) {
         p &= ~IndexProperties.SORT_MISSING_FIRST;
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

   public void afterPropertiesSet() throws Exception {
      Map<String, DocumentDefinition> documentDefinitions = documentContextProcessor.getDocumentDefinitions();
      for (Entry<String, DocumentDefinition> documentEntryDefinition : documentDefinitions.entrySet()) {
         String doucmentName = documentEntryDefinition.getKey();
         DocumentDefinition documentDefinition = documentEntryDefinition.getValue();
         List<Record> recordsToBeIndexed = documentDefinition.getRecordsToBeIndexed();
         indexRecordsForDocument(recordsToBeIndexed, doucmentName);

      }

   }

}
