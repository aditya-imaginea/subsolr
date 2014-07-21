package com.subsolr.datasource;

import java.io.FileReader;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.datasource.model.Record;
import com.subsolr.util.XMLParserUtil;

/**
 * POJO for FTP Data sources
 * 
 * @author vamsiy-mac aditya
 */
public class FtpDataSource extends FileDataSource {
   private String dataSourceName;
   private String host;
   private String userid;
   private String password;

   public FtpDataSource(Node dataSource, XMLParserUtil xmlParserUtil, XPath xpath) {
      // TODO Auto-generated constructor stub
   }

   public String getHost() {
      return host;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public String getUserid() {
      return userid;
   }

   public void setUserid(String userid) {
      this.userid = userid;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public FileReader getFileReader(String fileName) {
      // TODO
      return null;
   }


   @Override 
   public String getDataSourceName() {
      return dataSourceName;
   }

   public void setDataSourceName(String dataSourceName) {
      this.dataSourceName = dataSourceName;
   }

   @Override
   public List<Record> getRecords(FieldSetDefinition fieldSetDefinition) {
      // TODO Auto-generated method stub
      return null;
   }

}
