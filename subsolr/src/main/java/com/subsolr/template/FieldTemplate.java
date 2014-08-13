package com.subsolr.template;

import java.util.Map;

/**
 * Pojo for Field definition having fieldsPropeties and FieldType.
 * 
 * @author vamsiy-mac aditya
 * 
 */

public class FieldTemplate {

   String fieldName;
   FieldTypeDefinition fieldTypeDefinition;
   Map<String, String> fieldProperties;

   public FieldTemplate(String fieldName, FieldTypeDefinition fieldTypeDefinition, Map<String, String> fieldProperties) {
      this.fieldName = fieldName;
      this.fieldTypeDefinition = fieldTypeDefinition;
      this.fieldProperties = fieldProperties;

   }

   public FieldTemplate(FieldDefinitionBuilder fieldDefinitionBuilder) {
      this.fieldName = fieldDefinitionBuilder.fieldName;
      this.fieldTypeDefinition = fieldDefinitionBuilder.fieldTypeDefinition;
      this.fieldProperties = fieldDefinitionBuilder.fieldProperties;

   }

   public static class FieldDefinitionBuilder {
      public Map<String, String> fieldProperties;
      String fieldName;
      FieldTypeDefinition fieldTypeDefinition;

      public FieldDefinitionBuilder fieldName(String value) {
         this.fieldName = value;
         return this;
      }

      public FieldDefinitionBuilder fieldTypeDefinition(FieldTypeDefinition value) {
         this.fieldTypeDefinition = value;
         return this;
      }

      public FieldDefinitionBuilder properties(Map<String, String> value) {
         this.fieldProperties = value;
         return this;
      }

      public FieldTemplate build() {
         return new FieldTemplate(this);
      }

   }

   public String getFieldName() {
      return fieldName;
   }

   public FieldTypeDefinition getFieldTypeDefinition() {
      return fieldTypeDefinition;
   }

   public Map<String, String> getFieldProperties() {
      return fieldProperties;
   }

}
