package com.subsolr.config;

import com.subsolr.config.field.FieldDefinitionConfiguration;
import com.subsolr.config.field.FieldTypeConfiguration;
import com.subsolr.template.FieldTemplate;
import com.subsolr.template.FieldTypeDefinition;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

/**
 * Reads the FieldContext and generates the FieldDefinition with list with analyzers and filters and the Domain Fields
 * list
 * 
 * @author vamsiy-mac aditya
 * 
 */
public class FieldConfigurationBean extends AbstractConfigurationBean  implements InitializingBean {
   private final Resource resource;
   private final DocumentBuilder documentBuilder;

   private final FieldTypeConfiguration fieldTypeConfiguration;
   private final FieldDefinitionConfiguration fieldDefinitionConfiguration;

   private Map<String, FieldTypeDefinition> fieldTypeDefinitionsByName;
   private Map<String, FieldTemplate> fieldDefinitionsByName;

   public static final Logger logger = LoggerFactory.getLogger(FieldConfigurationBean.class);

   public FieldConfigurationBean(Resource resource, DocumentBuilder documentBuilder, XPath xPath,
         FieldTypeConfiguration fieldTypeProcessor, FieldDefinitionConfiguration fieldDefinitionConfiguration) {
      this.resource = resource;
      this.documentBuilder = documentBuilder;
      this.fieldTypeConfiguration = fieldTypeProcessor;
      this.fieldDefinitionConfiguration = fieldDefinitionConfiguration;
   }

   public Map<String, FieldTypeDefinition> getFieldTypeDefinitionsByName() {
      return fieldTypeDefinitionsByName;
   }

   public void setFieldTypeDefinitionsByName(Map<String, FieldTypeDefinition> fieldTypeDefinitionsByName) {
      this.fieldTypeDefinitionsByName = fieldTypeDefinitionsByName;
   }

   public FieldTemplate getFieldDefinitionsByName(String name) {
      return fieldDefinitionsByName.get(name);
   }

   public void setFieldDefinitionsByName(Map<String, FieldTemplate> fieldDefinitionsByName) {
      this.fieldDefinitionsByName = fieldDefinitionsByName;
   }

   @Override
   public void afterPropertiesSet() throws Exception {
      Document fieldConfigDocument = documentBuilder.parse(resource.getFile());
      fieldTypeDefinitionsByName = fieldTypeConfiguration.getFieldTypeDefinitions(fieldConfigDocument
            .getElementsByTagName("field_type"));
      fieldDefinitionsByName = fieldDefinitionConfiguration.getFieldDefinitions(
            fieldConfigDocument.getElementsByTagName("field"), fieldTypeDefinitionsByName);
   }

}
