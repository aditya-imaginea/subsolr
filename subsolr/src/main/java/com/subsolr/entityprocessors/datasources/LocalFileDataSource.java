package com.subsolr.entityprocessors.datasources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class LocalFileDataSource extends FileDataSource {

   private String basePath;

   public LocalFileDataSource() {
      this.basePath = null;
   }

   public LocalFileDataSource(String basePath) {
      this.basePath = basePath;
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

}
