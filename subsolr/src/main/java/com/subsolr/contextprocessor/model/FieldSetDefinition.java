package com.subsolr.contextprocessor.model;

import java.util.Map;

import com.subsolr.datasource.DataSource;
import com.subsolr.entityprocessors.EntityProcessor;

/**
 * Pojo for Fieldset definition having fields entity processor mappings
 * 
 * @author vamsiy-mac aditya
 * 
 */
public class FieldSetDefinition {

   private Map<String, String> fieldNameToEntityNameMap;
   private DataSource dataSource;
   private EntityProcessor entityProcessor;
   private Map<String, String> propertiesForEntityProcessor;
   private String name;

   public DataSource getDataSource() {
      return dataSource;
   }

   public void setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
   }

   public EntityProcessor getEntityProcessor() {
      return entityProcessor;
   }

   public void setEntityProcessor(EntityProcessor entityProcessor) {
      this.entityProcessor = entityProcessor;
   }

   public Map<String, String> getFieldNameToEntityNameMap() {
      return fieldNameToEntityNameMap;
   }

   public void setFieldNameToEntityNameMap(Map<String, String> fieldNameToEntityNameMap) {
      this.fieldNameToEntityNameMap = fieldNameToEntityNameMap;
   }

   public Map<String, String> getPropertiesForEntityProcessor() {
      return propertiesForEntityProcessor;
   }

   public void setPropertiesForEntityProcessor(Map<String, String> propertiesForEntityProcessor) {
      this.propertiesForEntityProcessor = propertiesForEntityProcessor;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

}
