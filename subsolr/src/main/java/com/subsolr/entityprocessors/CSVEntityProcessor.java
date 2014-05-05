package com.subsolr.entityprocessors;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.entityprocessors.datasources.FileDataSource;
import com.subsolr.entityprocessors.model.Record;

/**
 * Processor for csv files
 * 
 * @author vamsiy-mac aditya
 * 
 */

public class CSVEntityProcessor implements EntityProcessor {

   private static final String CSV_EXTENSION = "csv";

   public List<Record> getRecords(FieldSetDefinition fieldSetDefinition) {
      FileDataSource fileDataSource = (FileDataSource) fieldSetDefinition.getDataSource();
      Map<String, String> fieldNameToEntityNameMap = fieldSetDefinition.getFieldNameToEntityNameMap();

      String fileName = fieldSetDefinition.getPropertiesForEntityProcessor().get("File");
      validateFile(fileName);
      Reader reader = fileDataSource.getFileReader(fileName);

      List<String[]> lines = readCSV(reader);

      List<Record> records = Lists.newArrayList();
      for (String[] line : lines) {
         Map<String, String> valueByIndexName = Maps.newHashMap();
         for (String fieldName : fieldNameToEntityNameMap.keySet()) {
            String fieldValue = line[Integer.parseInt(fieldNameToEntityNameMap.get(fieldName)) - 1];
            valueByIndexName.put(fieldName, fieldValue);
         }
         records.add(new Record(valueByIndexName));
      }
      return records;
   }

   private List<String[]> readCSV(Reader reader) {
      CSVReader csvReader = new CSVReader(reader);
      List<String[]> lines = Lists.newArrayList();
      try {
         String[] line = csvReader.readNext();
         while (line != null) {
            lines.add(line);
            line = csvReader.readNext();
         }
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } finally {
         try {
            csvReader.close();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return lines;
   }

   private void validateFile(String fileName) {
      String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
      if (extension == null) {
         throw new IllegalArgumentException("Invalid file name");
      }
      if (!CSV_EXTENSION.equals(extension)) {
         throw new IllegalArgumentException("File extension should be " + CSV_EXTENSION);
      }
   }

}
