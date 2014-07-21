package com.subsolr.contextprocessor;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.subsolr.contextprocessor.model.DocumentDefinition;
import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.datasource.DataSource;
import com.subsolr.datasource.DataSourceFactory;
import com.subsolr.entityprocessors.EntityProcessor;
import com.subsolr.util.XMLParserUtil;

/**
 * Reads the Document context and creates the document definition list. and required config for document
 * 
 * @author vamsiy-mac aditya
 */

public class DocumentContextProcessor implements InitializingBean {

   private final XPath xPath;
   private XMLParserUtil xmlParserUtil;
   private final DocumentBuilder documentBuilder;
   private final Resource resource;
   private final DataSourceFactory dataSourceFactory;
   private Map<String, DocumentDefinition> documentDefinitionsByName;
   Map<String, DataSource> dataSourceByName;

   public DocumentContextProcessor(Resource resource, XPath xPath, DocumentBuilder documentBuilder,
         XMLParserUtil xmlParserUtil,DataSourceFactory dataSourceFactory) {
      this.xPath = xPath;
      this.documentBuilder = documentBuilder;
      this.resource = resource;
      this.xmlParserUtil = xmlParserUtil;
      this.dataSourceFactory = dataSourceFactory;
   }

   public Map<String, DocumentDefinition> getDocumentDefinitions() {
      return documentDefinitionsByName;
   }

   DocumentDefinition getDocumentDefinitionByName(String documentName) {
      return documentDefinitionsByName.get(documentName);
   }

   public void afterPropertiesSet() throws Exception {
      Document documentTypeConfigDocument = documentBuilder.parse(resource.getFile());
      setDataSources(documentTypeConfigDocument.getElementsByTagName("DataSource"));
      setDocumentDefinitions(documentTypeConfigDocument.getElementsByTagName("document"));
   }

   private void setDataSources(NodeList dataSources) throws XPathExpressionException {
      int noOfSQLDataSources = dataSources.getLength();
      dataSourceByName = Maps.newHashMap();
      for (int i = 0; i < noOfSQLDataSources; i++) {   
         DataSource dataSource = dataSourceFactory.getDataSource(dataSources.item(i));
         dataSourceByName.put(dataSource.getDataSourceName(), dataSource);
      }

   }

   private void setDocumentDefinitions(NodeList documentDefinitionNodeList) throws XPathExpressionException,
         InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException,
         SecurityException, InvocationTargetException, NoSuchMethodException {
      int noOfDocumentDefinitions = documentDefinitionNodeList.getLength();
      documentDefinitionsByName = Maps.<String, DocumentDefinition> newHashMap();
      DocumentDefinition documentDefinition = null;
      for (int i = 0; i < noOfDocumentDefinitions; i++) {
         Node documentDefinitionNode = documentDefinitionNodeList.item(i);
         String documentName = xmlParserUtil.getAttributeValueInNode(documentDefinitionNode, "name");
         NodeList fieldsetDefinitions = (NodeList) xPath.evaluate("./fieldset", documentDefinitionNode,
               XPathConstants.NODESET);
         documentDefinition = new DocumentDefinition();
         documentDefinition.setDocumentName(documentName);
         documentDefinition.setFieldSets(extractFieldSetDefintions(fieldsetDefinitions));
         documentDefinition.setMappingRules(extractMappingRules(documentDefinitionNode));
         documentDefinitionsByName.put(documentName, documentDefinition);
      }

   }

   private LinkedHashMap<String, String> extractMappingRules(Node documentDefinitionNode)
         throws XPathExpressionException {

      LinkedHashMap<String, String> mappings = Maps.newLinkedHashMap();
      NodeList mappingsNodeList = (NodeList) xPath.evaluate("./mappings/mapping", documentDefinitionNode,
            XPathConstants.NODESET);
      int noOfMappings = mappingsNodeList.getLength();
      for (int i = 0; i < noOfMappings; i++) {
         Node mapping = mappingsNodeList.item(i);
         mappings.put(xmlParserUtil.getAttributeValueInNode(mapping, "name"), mapping.getTextContent());
      }
      return mappings;

   }

   private Map<String, FieldSetDefinition> extractFieldSetDefintions(NodeList fieldsetDefinitionNodeList)
         throws XPathExpressionException, InstantiationException, IllegalAccessException, ClassNotFoundException,
         IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
      int noOfFieldSetsInDoc = fieldsetDefinitionNodeList.getLength();
      Map<String, FieldSetDefinition> fieldSetsByName = Maps.newHashMap();

      for (int i = 0; i < noOfFieldSetsInDoc; i++) {
         FieldSetDefinition fieldSetDefinition = new FieldSetDefinition();
         Map<String, String> propertiesForEntityProcessor = Maps.newHashMap();
         Node fieldSetNode = fieldsetDefinitionNodeList.item(i);
         String fieldSetName = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "name");
         String sourceId = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "sourceId");
         String entityProcessorClass = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "EntityProcessor");
         fieldSetDefinition.setDataSource(dataSourceByName.get(sourceId));
         Class<? extends EntityProcessor> entityProcessor = (Class<? extends EntityProcessor>) Class
               .forName(entityProcessorClass);
         fieldSetDefinition.setEntityProcessor(entityProcessor.newInstance());
         Node queryNode = (Node) xPath.evaluate("./query/statement", fieldSetNode, XPathConstants.NODE);
         Node fileNode = (Node) xPath.evaluate("./fileName", fieldSetNode, XPathConstants.NODE);
         if (null != queryNode) {
            propertiesForEntityProcessor.put("SQLQuery", queryNode.getTextContent());
         }
         if (null != fileNode) {
            propertiesForEntityProcessor.put("File", fileNode.getTextContent());
         }
         fieldSetDefinition.setPropertiesForEntityProcessor(propertiesForEntityProcessor);

         NodeList fieldMappingNodes = (NodeList) xPath.evaluate("./field", fieldSetNode, XPathConstants.NODESET);
         Map<String, String> fieldToColumnMapping = extractFieldMappings(fieldMappingNodes);
         fieldSetDefinition.setFieldNameToEntityNameMap(fieldToColumnMapping);
         fieldSetDefinition.setName(fieldSetName);
         fieldSetsByName.put(fieldSetName, fieldSetDefinition);
      }

      return fieldSetsByName;

   }

   private Map<String, String> extractFieldMappings(NodeList fieldMappings) {
      int noOfFields = fieldMappings.getLength();
      Map<String, String> fieldToColumnMapping = Maps.newHashMap();
      for (int i = 0; i < noOfFields; i++) {
         Node fieldNode = fieldMappings.item(i);
         String colName = xmlParserUtil.getAttributeValueInNode(fieldNode, "column_name");
         String fieldName = xmlParserUtil.getAttributeValueInNode(fieldNode, "field_map_name");
         fieldToColumnMapping.put(fieldName, colName);

      }
      return fieldToColumnMapping;

   }
}
