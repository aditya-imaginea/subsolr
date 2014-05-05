package com.subsolr.contextprocessor.fieldprocessor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.TokenizerChain;
import org.apache.solr.schema.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.subsolr.contextprocessor.model.FieldTypeDefinition;
import com.subsolr.util.XMLParserUtil;

public class FieldTypeProcessor {
   public static final Logger logger = LoggerFactory.getLogger(FieldTypeProcessor.class);
   private XPath xPath;
   private String luceneVersion;
   private XMLParserUtil xmlparserutil;

   public Map<String, FieldTypeDefinition> getFieldTypeDefinitions(NodeList fieldTypeDefinitionNodes)
         throws XPathExpressionException, InstantiationException, IllegalAccessException, ClassNotFoundException,
         IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

      HashMap<String, FieldTypeDefinition> fieldTypeDefinitionsByName = Maps.<String, FieldTypeDefinition> newHashMap();
      int noOfFieldTypeDefinitons = fieldTypeDefinitionNodes.getLength();
      for (int i = 0; i < noOfFieldTypeDefinitons; i++) {
         Node fieldTypeDefinitionNode = fieldTypeDefinitionNodes.item(i);
         String fieldTypeName = xmlparserutil.getAttributeValueInNode(fieldTypeDefinitionNode, "name");
         logger.debug(String.format("processing field type name %s in fieldTypeDefNode %s", fieldTypeName,
               fieldTypeDefinitionNode.getNodeName()));
         Node similarityNode = (Node) xPath.evaluate("./similarity", fieldTypeDefinitionNode, XPathConstants.NODE);
         NodeList analyzerNodeList = (NodeList) xPath.evaluate("./analyzer", fieldTypeDefinitionNode,
               XPathConstants.NODESET);
         Class<? extends FieldType> fieldTypeClass = (Class<? extends FieldType>) Class.forName(xmlparserutil
               .getAttributeValueInNode(fieldTypeDefinitionNode, "class"));
         String positionIncrementGap = getPositionIncrementGap(fieldTypeDefinitionNode);
         FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
         fieldTypeDefinition.setName(fieldTypeName);
         fieldTypeDefinition.setFieldTypeClassName(fieldTypeClass);
         if (null != positionIncrementGap) {
            fieldTypeDefinition.setPositionIncrementGap(Integer.valueOf(positionIncrementGap));
         }
         if (null != similarityNode) {
            fieldTypeDefinition.setSimilarityClassName(Similarity.class.cast(Class.forName(
                  xmlparserutil.getAttributeValueInNode(similarityNode, "class")).newInstance()));
         }
         if (null != analyzerNodeList) {
            fieldTypeDefinition.setAnalyzer(getAnalyzers(analyzerNodeList));
         }

         fieldTypeDefinitionsByName.put(fieldTypeName, fieldTypeDefinition);

      }

      return fieldTypeDefinitionsByName;
   }

   private List<Analyzer> getAnalyzers(NodeList analyzerNodeList) throws XPathExpressionException,
         IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
         InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
      List<Analyzer> analyzers = Lists.newArrayList();
      int noOfAnalyzers = analyzerNodeList.getLength();
      for (int i = 0; i < noOfAnalyzers; i++) {
         Node analyzerNode = analyzerNodeList.item(i);
         String simpleAnalyzerClass = xmlparserutil.getAttributeValueInNode(analyzerNode, "class");
         if (null != simpleAnalyzerClass) {
            analyzers.add(getSimpleAnalyzer(simpleAnalyzerClass));
         } else {
            analyzers.add(getAnalyzer(analyzerNode));
         }
      }
      return analyzers;
   }

   private Analyzer getSimpleAnalyzer(String simpleAnalyzerClass) throws InstantiationException,
         IllegalAccessException, ClassNotFoundException, IllegalArgumentException, SecurityException,
         InvocationTargetException, NoSuchMethodException {
      return (Analyzer) Class.forName(simpleAnalyzerClass).getConstructor(Version.class)
            .newInstance(Version.valueOf(luceneVersion));
   }

   private String getPositionIncrementGap(Node fieldTypeDefinitionNode) {
      return xmlparserutil.getAttributeValueInNode(fieldTypeDefinitionNode, "positionIncrementGap");
   }

   private Analyzer getAnalyzer(Node analyzerNode) throws XPathExpressionException, InstantiationException,
         IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
         SecurityException, ClassNotFoundException {
      Node tokenizerNode = (Node) xPath.evaluate("./tokenizer", analyzerNode, XPathConstants.NODE);
      NodeList filterNodes = (NodeList) xPath.evaluate("./filter", analyzerNode, XPathConstants.NODESET);

      int totalFilters = filterNodes.getLength();
      NodeList charFilterNodes = (NodeList) xPath.evaluate("./charFilter", analyzerNode, XPathConstants.NODESET);
      TokenizerFactory tokenizer = (TokenizerFactory) Class
            .forName(xmlparserutil.getAttributeValueInNode(tokenizerNode, "class")).getConstructor(Map.class)
            .newInstance(xmlparserutil.toMap(tokenizerNode.getAttributes()));
      TokenFilterFactory[] tokenFilters = new TokenFilterFactory[totalFilters];
      for (int i = 0; i < totalFilters; i++) {
         Node filterNode = filterNodes.item(i);
         tokenFilters[i] = TokenFilterFactory.forName(xmlparserutil.getAttributeValueInNode(filterNode, "class"),
               xmlparserutil.toMap(filterNode.getAttributes()));
      }
      totalFilters = charFilterNodes.getLength();
      CharFilterFactory[] charFilters = new CharFilterFactory[totalFilters];

      for (int i = 0; i < totalFilters; i++) {
         Node filterNode = charFilterNodes.item(i);
         charFilters[i] = CharFilterFactory.forName(xmlparserutil.getAttributeValueInNode(filterNode, "class"),
               xmlparserutil.toMap(filterNode.getAttributes()));
      }

      return new TokenizerChain(charFilters, tokenizer, tokenFilters);

   }

}
