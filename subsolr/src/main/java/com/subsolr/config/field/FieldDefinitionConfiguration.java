package com.subsolr.config.field;

import com.google.common.collect.Maps;
import com.subsolr.template.FieldTemplate;
import com.subsolr.template.FieldTypeDefinition;
import com.subsolr.util.XMLParserUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FieldDefinitionConfiguration {
   public static final Logger logger = LoggerFactory.getLogger(FieldDefinitionConfiguration.class);
   private final XMLParserUtil xmlParserUtil;

   public FieldDefinitionConfiguration(XMLParserUtil xmlParserUtil) {
      this.xmlParserUtil = xmlParserUtil;
   }

   public Map<String, FieldTemplate> getFieldDefinitions(NodeList fieldDefinitionNodes,
         Map<String, FieldTypeDefinition> fieldTypeDefinitionsByName) {
      HashMap<String, FieldTemplate> fieldDefinitionsByName = Maps.<String, FieldTemplate> newHashMap();
      int noOfFieldDefinitonSets = fieldDefinitionNodes.getLength();

      for (int i = 0; i < noOfFieldDefinitonSets; i++) {
         Node fieldDefinitionNode = fieldDefinitionNodes.item(i);
         String fieldName = xmlParserUtil.getAttributeValueInNode(fieldDefinitionNode, "name");
         logger.debug(String.format("processing field name %s in fieldDefNode %s", fieldName,
               fieldDefinitionNode.getNodeName()));
         Map<String, String> fieldPropeties = xmlParserUtil.toMap(fieldDefinitionNode.getAttributes());
         fieldPropeties.remove("name");
         fieldPropeties.remove("type");
         fieldPropeties.remove("luceneMatchVersion");

         FieldTemplate fieldDefinition = new FieldTemplate.FieldDefinitionBuilder()
               .fieldName(fieldName)
               .fieldTypeDefinition(
                     fieldTypeDefinitionsByName.get(xmlParserUtil.getAttributeValueInNode(fieldDefinitionNode, "type")))
               .properties(fieldPropeties).build();
         fieldDefinitionsByName.put(fieldName, fieldDefinition);
      }
      //org.apache.solr.schema.D
      return fieldDefinitionsByName;
   }

}
