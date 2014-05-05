package com.subsolr.contextprocessor.fieldprocessor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.subsolr.contextprocessor.model.FieldDefinition;
import com.subsolr.contextprocessor.model.FieldTypeDefinition;
import com.subsolr.util.XMLParserUtil;

public class FieldDefinitionProcessor {
   public static final Logger logger = LoggerFactory.getLogger(FieldDefinitionProcessor.class);
   private XMLParserUtil xmlParserUtil;

   public FieldDefinitionProcessor(XMLParserUtil xmlParserUtil) {
      this.xmlParserUtil = xmlParserUtil;
   }

   public Map<String, FieldDefinition> getFieldDefinitions(NodeList fieldDefinitionNodes,
         Map<String, FieldTypeDefinition> fieldTypeDefinitionsByName) {
      HashMap<String, FieldDefinition> fieldDefinitionsByName = Maps.<String, FieldDefinition> newHashMap();
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

         FieldDefinition fieldDefinition = new FieldDefinition.FieldDefinitionBuilder()
               .fieldName(fieldName)
               .fieldTypeDefinition(
                     fieldTypeDefinitionsByName.get(xmlParserUtil.getAttributeValueInNode(fieldDefinitionNode, "type")))
               .properties(fieldPropeties).build();
         fieldDefinitionsByName.put(fieldName, fieldDefinition);
      }
      return fieldDefinitionsByName;
   }

}
