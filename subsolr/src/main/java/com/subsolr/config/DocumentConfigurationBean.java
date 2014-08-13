package com.subsolr.config;

import com.subsolr.datasource.DataSourceFactory;
import com.subsolr.template.DocumentTemplate;
import com.subsolr.util.XMLParserUtil;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Reads the Document context and creates the document definition list. and required config for document
 * 
 * @author vamsiy-mac aditya
 */

public class DocumentConfigurationBean extends AbstractConfigurationBean implements InitializingBean {

   private final XPath xPath;
   private final XMLParserUtil xmlParserUtil;
   private final DocumentBuilder documentBuilder;
   private final Resource resource;
   private final DataSourceFactory dataSourceFactory;
   private Map<String, DocumentTemplate> documentTemplatesByName;
   
   private ConfigurationExtractor configurationExtractor;
   
   
    public DocumentConfigurationBean(Resource resource, XPath xPath, DocumentBuilder documentBuilder,
         XMLParserUtil xmlParserUtil,DataSourceFactory dataSourceFactory) {
      this.xPath = xPath;
      this.documentBuilder = documentBuilder;
      this.resource = resource;
      this.xmlParserUtil = xmlParserUtil;
      this.dataSourceFactory = dataSourceFactory;
   }

    public ConfigurationExtractor getExtractor() {
        return configurationExtractor;
    }
    
   public Map<String, DocumentTemplate> getDocumentTemplates() {
      return documentTemplatesByName;
   }

   @Override
   public void afterPropertiesSet() throws Exception {
      this.configurationExtractor = new ConfigurationExtractor(this.xmlParserUtil, this);
      this.configurationExtractor.init(this.resource, this.documentBuilder, this.dataSourceFactory, this.xPath);
   }
   

}
