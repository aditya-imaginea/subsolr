package com.subsolr.entityprocessors.datasources;

/**
 * POJO for SQL Data sources
 * 
 * @author vamsiy-mac aditya
 */
public class SQLDataSource implements DataSource {

   public SQLDataSource(String driver, String url, String userId, String password) {
      this.driver = driver;
      this.url = url;
      this.userId = userId;
      this.password = password;
   }

   private String driver;
   private String url;
   private String userId;
   private String password;

   public String getDriver() {
      return driver;
   }

   public void setDriver(String driver) {
      this.driver = driver;
   }

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

}
