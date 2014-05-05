package com.subsolr.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.collect.Maps;

public class XMLParserUtil {
   private String luceneVersion;

   public XMLParserUtil(String luceneVersion) {
      this.luceneVersion = luceneVersion;
   }

   public static final Logger logger = LoggerFactory.getLogger(XMLParserUtil.class);

   public String getAttributeValueInNode(Node fieldDefinitionNode, String attributeName) {
      logger.debug(String.format("looking for attribute name %s in fieldDefNode %s", attributeName,
            fieldDefinitionNode.getNodeName()));
      Node attributeNode = fieldDefinitionNode.getAttributes().getNamedItem(attributeName);
      logger.debug("attributeNode " + attributeNode);
      String attributeValue = null == attributeNode ? null : attributeNode.getNodeValue();
      logger.debug("attributeValue " + attributeValue);
      return attributeValue;
   }

   public Map<String, String> toMap(NamedNodeMap attributes) {
      int noOfAttributes = attributes.getLength();
      Map<String, String> attributesMap = Maps.<String, String> newHashMap();

      attributesMap.put("luceneMatchVersion", luceneVersion);
      for (int i = 0; i < noOfAttributes; i++) {
         attributesMap.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
      }
      return attributesMap;
   }

}
