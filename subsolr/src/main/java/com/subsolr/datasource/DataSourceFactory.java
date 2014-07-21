package com.subsolr.datasource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.subsolr.util.XMLParserUtil;

public class DataSourceFactory {

   private final XMLParserUtil xmlParserUtil;
   private final XPath xpath;
   
   public DataSourceFactory(XMLParserUtil xmlParserUtil,XPath xpath) {
      this.xmlParserUtil = xmlParserUtil;
      this.xpath = xpath;
   }

   public DataSource getDataSource(Node dataSource) throws XPathExpressionException {
      DataSource ds;
      switch (xmlParserUtil.getAttributeValueInNode(dataSource, "type")) {
      case "sql":
         ds = new SQLDataSource(dataSource,xmlParserUtil,xpath);
         break;
      case "ftp":
         ds = new FtpDataSource(dataSource,xmlParserUtil,xpath);
         break;
      case "file":
         ds = new LocalFileDataSource(dataSource,xmlParserUtil,xpath);
         break;
      default:
         // defaulting to SQL datasource
         ds = new SQLDataSource(dataSource,xmlParserUtil,xpath);
         break;
      }
      return ds;
   }
}
