package com.subsolr.entityprocessors.model;

import java.util.Map;

/**
 * POJO for records processed by entity processors or field set mapping rules
 * 
 * @author vamsiy-mac aditya
 */
public class Record {

   private Map<String, String> valueByFieldName;

   public Record(Map<String, String> valueByIndexName) {
      this.valueByFieldName = valueByIndexName;
   }

   public Map<String, String> getValueByFieldName() {
      return valueByFieldName;
   }

   public void setValueByFieldName(Map<String, String> valueByIndexName) {
      this.valueByFieldName = valueByIndexName;
   }

}
