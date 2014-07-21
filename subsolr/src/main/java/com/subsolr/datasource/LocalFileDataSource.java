package com.subsolr.datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.datasource.model.Record;
import com.subsolr.util.XMLParserUtil;

public class LocalFileDataSource extends FileDataSource {
   private String dataSourceName;
   private String basePath;
   private String fileName;

   public LocalFileDataSource() {

   }

   public LocalFileDataSource(Node dataSource, XMLParserUtil xmlParserUtil, XPath xPath)
         throws XPathExpressionException {
      String dataSourceName = xmlParserUtil.getAttributeValueInNode(dataSource, "id");
      Node basePath = (Node) xPath.evaluate("./local_path", dataSource, XPathConstants.NODE);
      Node fileName = (Node) xPath.evaluate("./file_name", dataSource, XPathConstants.NODE);

      setDataSource(dataSourceName, basePath.getTextContent(), fileName.getTextContent());
   }

   private void setDataSource(String dataSourceName, String basePath, String fileName) {
      this.dataSourceName = dataSourceName;
      this.basePath = basePath;
      this.fileName = fileName;

   }

   public FileReader getFileReader(String fileName) {

      File file = new File(getBasePath(), fileName).getAbsoluteFile();
      FileReader fileReader = null;
      try {
         fileReader = new FileReader(file);

      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return fileReader;
   }

   public String getBasePath() {
      return basePath;
   }

   public void setBasePath(String basePath) {
      this.basePath = basePath;
   }

   @Override
   public String getDataSourceName() {
      return dataSourceName;
   }

   @Override
   public List<Record> getRecords(FieldSetDefinition fieldSetDefinition) {
      // TODO Auto-generated method stub
      return null;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public void setDataSourceName(String dataSourceName) {
      this.dataSourceName = dataSourceName;
   }

}
