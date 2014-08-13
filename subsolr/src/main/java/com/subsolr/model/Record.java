package com.subsolr.model;

import java.util.Map;
import java.util.Objects;

/**
 * POJO for records processed by entity processors or field set mapping rules
 * 
 * @author vamsiy-mac aditya
 */
public class Record {

   private Map<String, String> valueByFieldName;
   
   private String name;
   private String fieldSet;
   private String autoGenKey;

    public String getAutoGenKey() {
        return autoGenKey;
    }

    public void setAutoGenKey(String autoGenKey) {
        this.autoGenKey = autoGenKey;
    }
   
    public String getFieldSet() {
        return fieldSet;
    }

    public void setFieldSet(String fieldSet) {
        this.fieldSet = fieldSet;
    }
   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
   @Override
    public String toString() {
        return name+" <"+autoGenKey+">";
    }
   
    
   @Override
    public boolean equals(Object o) {
        if(o instanceof Record) {
            Record rd = (Record)o;
            if(rd.getAutoGenKey().equals(this.autoGenKey)) {
                return true;
            }
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.fieldSet);
        hash = 37 * hash + Objects.hashCode(this.autoGenKey);
        return hash;
    }
   

   public Record(Map<String, String> valueByIndexName) {
      this.valueByFieldName = valueByIndexName;
   }

   public Map<String, String> getValueByFieldName() {
      return valueByFieldName;
   }

   public void setValueByFieldName(Map<String, String> valueByIndexName) {
      this.valueByFieldName = valueByIndexName;
   }
   
   
   
   public static Record copy(Record record) {
       Record rd = new Record(record.getValueByFieldName());
       rd.setAutoGenKey(record.getAutoGenKey());
       return rd;
   }

}
