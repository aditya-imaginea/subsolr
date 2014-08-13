/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.subsolr.config;

import com.subsolr.datasource.DataSource;
import com.subsolr.datasource.DataSourceFactory;
import com.subsolr.model.Join;
import com.subsolr.template.DocumentTemplate;
import com.subsolr.template.FieldSetDefinition;
import com.subsolr.transformer.ModelTransformer;
import com.subsolr.util.XMLParserUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sushantk
 */
public class ConfigurationExtractor {

    private final XMLParserUtil xmlParserUtil;
    private Map<String, ModelTransformer> transformerByName;
    private Map<String, DataSource> dataSourcesByName;
    private final DocumentConfigurationBean documentConfiguration;
    private Map<String, DocumentTemplate> documentTemplatesByName;

    private Map<String, Join> joins;

    public Map<String, Join> getJoins() {
        return joins;
    }

    public Map<String, DocumentTemplate> getDocumentTemplatesByName() {
        return documentTemplatesByName;
    }

    public ConfigurationExtractor(XMLParserUtil util, DocumentConfigurationBean documentConfiguration) {
        this.xmlParserUtil = util;
        this.documentConfiguration = documentConfiguration;
        //extractTransformers(documentTypeConfigDocument.getElementsByTagName("Transformer"));
    }

    public void init(final Resource resource, DocumentBuilder documentBuilder, DataSourceFactory dataSourceFactory, XPath xPath) throws Exception {
        Document documentTypeConfigDocument = documentBuilder.parse(resource.getFile());
        extractJoins(documentTypeConfigDocument.getElementsByTagName("join"));
        setDataSources(documentTypeConfigDocument.getElementsByTagName("DataSource"), dataSourceFactory);
        extractTransformers(documentTypeConfigDocument.getElementsByTagName("Transformer"));
        extractDocumentTemplates(documentTypeConfigDocument.getElementsByTagName("document"), xPath);

    }

    public void extractJoins(NodeList joinNode) throws Exception {
        int noOfJoins = joinNode.getLength();
        joins = new HashMap<>();
        for (int i = 0; i < noOfJoins; i++) {
            NamedNodeMap attributes = joinNode.item(i).getAttributes();
            Join join = new Join();
            join.setId(attributes.getNamedItem("id").getTextContent());
            join.setLeftFieldSet(attributes.getNamedItem("leftFieldset").getTextContent());
            join.setRightFieldSet(attributes.getNamedItem("rightFieldset").getTextContent());
            join.setLeftKey(attributes.getNamedItem("leftKey").getTextContent());
            join.setRightKey(attributes.getNamedItem("rightKey").getTextContent());
            joins.put(join.getId(), join);
        }
    }

    public Map<String, ModelTransformer> extractTransformers(NodeList transformers) throws Exception {
        transformerByName = new HashMap<>();
        int noOfTransformers = transformers.getLength();
        transformerByName = new HashMap<>();
        for (int i = 0; i < noOfTransformers; i++) {
            NamedNodeMap attributes = transformers.item(i).getAttributes();
            ModelTransformer transformer = (ModelTransformer) Class.forName(attributes.getNamedItem("class").getTextContent()).newInstance();
            transformerByName.put(attributes.getNamedItem("id").getTextContent(), transformer);
            transformerByName.put(attributes.getNamedItem("id").getTextContent(), transformer);
        }
        return transformerByName;
    }

    private Map<String, String> extractFieldMappings(NodeList fieldMappings) {
        int noOfFields = fieldMappings.getLength();
        Map<String, String> fieldToColumnMapping = new HashMap<>();
        for (int i = 0; i < noOfFields; i++) {
            Node fieldNode = fieldMappings.item(i);
            String colName = xmlParserUtil.getAttributeValueInNode(fieldNode, "column_name");
            String fieldName = xmlParserUtil.getAttributeValueInNode(fieldNode, "field_map_name");
            fieldToColumnMapping.put(fieldName, colName);
        }
        return fieldToColumnMapping;

    }

    private Map<String, FieldSetDefinition> extractFieldSetDefintions(NodeList fieldsetDefinitionNodeList, XPath xPath)
            throws XPathExpressionException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
        int noOfFieldSetsInDoc = fieldsetDefinitionNodeList.getLength();
        Map<String, FieldSetDefinition> fieldSetsByName = new HashMap<>();

        for (int i = 0; i < noOfFieldSetsInDoc; i++) {
            FieldSetDefinition fieldSetDefinition = new FieldSetDefinition();
            Map<String, String> propertiesForEntityProcessor = new HashMap<>();
            Node fieldSetNode = fieldsetDefinitionNodeList.item(i);
            String fieldSetName = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "name");
            String sourceId = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "sourceId");
            String transformerKey = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "transformer");
            ModelTransformer trans = transformerByName.get(transformerKey);
			if(trans==null) {
                System.out.println("No transformer defined for "+fieldSetName+". Using default");
                trans = new DefaultTransformerImpl();
            }
            if (xmlParserUtil.getAttributeValueInNode(fieldSetNode, "transKeys") != null) {
                String[] transKeys = xmlParserUtil.getAttributeValueInNode(fieldSetNode, "transKeys").split(",");
                Map<String, Object> keys = new HashMap<>();
                keys.put("keys", transKeys);
                trans.setKeys(keys);
            }
            fieldSetDefinition.setDataSource(dataSourcesByName.get(sourceId));
            fieldSetDefinition.setTransformer(trans);
            Node queryNode = (Node) xPath.evaluate("./query/statement", fieldSetNode, XPathConstants.NODE);
           // Node fileNode = (Node) xPath.evaluate("./fileName", fieldSetNode, XPathConstants.NODE);
            if (null != queryNode) {
                propertiesForEntityProcessor.put("SQLQuery", queryNode.getTextContent());
            }
//            if (null != fileNode) {
//                propertiesForEntityProcessor.put("File", fileNode.getTextContent());
//            }
            fieldSetDefinition.setPropertiesForEntityProcessor(propertiesForEntityProcessor);
            NodeList fieldMappingNodes = (NodeList) xPath.evaluate("./field", fieldSetNode, XPathConstants.NODESET);
            Map<String, String> fieldToColumnMapping = extractFieldMappings(fieldMappingNodes);
            fieldSetDefinition.setFieldNameToEntityNameMap(fieldToColumnMapping);
            fieldSetDefinition.setName(fieldSetName);
            fieldSetsByName.put(fieldSetName, fieldSetDefinition);
        }
        return fieldSetsByName;

    }

    public Map<String, DocumentTemplate> extractDocumentTemplates(NodeList documentDefinitionNodeList, XPath xPath) throws XPathExpressionException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException,
            SecurityException, InvocationTargetException, NoSuchMethodException {

        int noOfDocumentDefinitions = documentDefinitionNodeList.getLength();
        documentTemplatesByName = new HashMap<>();
        DocumentTemplate documentDefinition;
        for (int i = 0; i < noOfDocumentDefinitions; i++) {
            Node documentDefinitionNode = documentDefinitionNodeList.item(i);
            String documentName = xmlParserUtil.getAttributeValueInNode(documentDefinitionNode, "name");
            NodeList fieldsetDefinitions = (NodeList) xPath.evaluate("./fieldset", documentDefinitionNode,
                    XPathConstants.NODESET);
            documentDefinition = new DocumentTemplate();
            documentDefinition.setExtractor(this.documentConfiguration.getExtractor());
            documentDefinition.setDocumentName(documentName);
            documentDefinition.setFieldSets(extractFieldSetDefintions(fieldsetDefinitions, xPath));
            documentDefinition.setTransformer(transformerByName.get(xmlParserUtil.getAttributeValueInNode(documentDefinitionNode, "transformer")));
            documentTemplatesByName.put(documentName, documentDefinition);
        }
        return documentTemplatesByName;

    }

    public Map<String, DataSource> setDataSources(NodeList dataSources, DataSourceFactory factory) throws XPathExpressionException {
        int noOfDataSources = dataSources.getLength();
        dataSourcesByName = new HashMap<>();
        dataSourcesByName = new HashMap<>();
        for (int i = 0; i < noOfDataSources; i++) {
            DataSource dataSource = factory.getDataSource(dataSources.item(i));
            dataSourcesByName.put(dataSource.getDataSourceName(), dataSource);
        }
        return dataSourcesByName;
    }
}
