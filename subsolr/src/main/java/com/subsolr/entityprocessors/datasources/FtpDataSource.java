package com.subsolr.entityprocessors.datasources;

import java.io.FileReader;

/**
 * POJO for FTP Data sources
 * 
 * @author vamsiy-mac aditya
 */
public class FtpDataSource extends FileDataSource {

   private String host;
   private String userid;
   private String password;

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

}
