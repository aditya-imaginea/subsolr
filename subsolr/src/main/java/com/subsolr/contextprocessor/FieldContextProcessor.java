package com.subsolr.contextprocessor;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

import com.subsolr.contextprocessor.fieldprocessor.FieldDefinitionProcessor;
import com.subsolr.contextprocessor.fieldprocessor.FieldTypeProcessor;
import com.subsolr.contextprocessor.model.FieldDefinition;
import com.subsolr.contextprocessor.model.FieldTypeDefinition;

/**
 * Reads the FieldContext and generates the FieldDefinition with list with analyzers and filters and the Domain Fields
 * list
 * 
 * @author vamsiy-mac aditya
 * 
 */
public class FieldContextProcessor implements InitializingBean {
   private Resource resource;
   private DocumentBuilder documentBuilder;

   private FieldTypeProcessor fieldTypeProcessor;
   private FieldDefinitionProcessor fieldDefinitionProcessor;

   private Map<String, FieldTypeDefinition> fieldTypeDefinitionsByName;
   private Map<String, FieldDefinition> fieldDefinitionsByName;

   public static final Logger logger = LoggerFactory.getLogger(FieldContextProcessor.class);

   public FieldContextProcessor(Resource resource, DocumentBuilder documentBuilder, XPath xPath,
         FieldTypeProcessor fieldTypeProcessor, FieldDefinitionProcessor fieldDefinitionProcessor) {
      this.resource = resource;
      this.documentBuilder = documentBuilder;
      this.fieldTypeProcessor = fieldTypeProcessor;
      this.fieldDefinitionProcessor = fieldDefinitionProcessor;
   }

   public Map<String, FieldTypeDefinition> getFieldTypeDefinitionsByName() {
      return fieldTypeDefinitionsByName;
   }

   public void setFieldTypeDefinitionsByName(Map<String, FieldTypeDefinition> fieldTypeDefinitionsByName) {
      this.fieldTypeDefinitionsByName = fieldTypeDefinitionsByName;
   }

   public FieldDefinition getFieldDefinitionsByName(String name) {
      return fieldDefinitionsByName.get(name);
   }

   public void setFieldDefinitionsByName(Map<String, FieldDefinition> fieldDefinitionsByName) {
      this.fieldDefinitionsByName = fieldDefinitionsByName;
   }

   public void afterPropertiesSet() throws Exception {
      Document fieldConfigDocument = documentBuilder.parse(resource.getFile());
      fieldTypeDefinitionsByName = fieldTypeProcessor.getFieldTypeDefinitions(fieldConfigDocument
            .getElementsByTagName("field_type"));
      fieldDefinitionsByName = fieldDefinitionProcessor.getFieldDefinitions(
            fieldConfigDocument.getElementsByTagName("field"), fieldTypeDefinitionsByName);
   }

}
