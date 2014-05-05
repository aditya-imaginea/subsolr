package com.subsolr.contextprocessor.model;

import static com.googlecode.cqengine.query.QueryFactory.contains;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.resultset.ResultSet;
import com.subsolr.entityprocessors.model.Record;
import com.subsolr.util.DynamicIndexer;
import com.subsolr.util.PojoGenerator;

/**
 * Pojo for Document definition having fields,fieldsets, feildsetMappings and mapping rules among field sets
 * 
 * @author vamsiy-mac aditya
 * 
 */

public class DocumentDefinition {
   private String documentName;
   private Map<String, FieldSetDefinition> fieldSets;
   LinkedHashMap<String, String> mappingRules;
   Map<String, Set<String>> attributes = Maps.newHashMap(); // for joins

   public String getDocumentName() {
      return documentName;
   }

   public void setDocumentName(String documentName) {
      this.documentName = documentName;
   }

   public Map<String, FieldSetDefinition> getFieldSets() {
      return fieldSets;
   }

   public void setFieldSets(Map<String, FieldSetDefinition> map) {
      this.fieldSets = map;
   }

   public List<Record> getRecordsToBeIndexed() throws NotFoundException, CannotCompileException,
         IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
         InvocationTargetException, NoSuchMethodException {
      Map<String, List<Record>> recordsByFieldSet = Maps.newHashMap();
      for (Map.Entry<String, FieldSetDefinition> fieldSetEntry : fieldSets.entrySet()) {
         recordsByFieldSet.put(fieldSetEntry.getKey(),
               fieldSetEntry.getValue().getEntityProcessor().getRecords(fieldSetEntry.getValue()));
      }

      return combinedFieldSets(recordsByFieldSet);

   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   private List<Record> combinedFieldSets(Map<String, List<Record>> recordsByFieldSet) throws NotFoundException,
         CannotCompileException, IllegalArgumentException, SecurityException, InstantiationException,
         IllegalAccessException, InvocationTargetException, NoSuchMethodException {
      List<Record> records = Lists.newArrayList();
      for (Map.Entry<String, String> mappingRuleEntry : mappingRules.entrySet()) {
         records = Lists.newArrayList();
         String mappingRule = mappingRuleEntry.getValue();
         attributes.put(mappingRuleEntry.getKey(), Sets.<String> newHashSet());
         final String[] fieldSetConditions = mappingRule.split("=");

         Class fieldsetDataClassLeft = generateClassFile(fieldSetConditions[0].split("#")[0].trim());
         Class fieldsetDataClassRight = generateClassFile(fieldSetConditions[1].split("#")[0].trim());

         List<Record> recordsOfLeftOp = getRecords(recordsByFieldSet, fieldSetConditions[0].split("#")[0].trim());
         List fieldsetDataLeft = populateData(fieldsetDataClassLeft, recordsOfLeftOp);
         List<Record> recordsOfRightOp = getRecords(recordsByFieldSet, fieldSetConditions[1].split("#")[0].trim());
         List fieldsetDataRight = populateData(fieldsetDataClassRight, recordsOfRightOp);

         Map fieldsetDataAtributesLeft = DynamicIndexer.generateAttributesForPojo(fieldsetDataClassLeft);
         Map fieldsetDataAtributesRight = DynamicIndexer.generateAttributesForPojo(fieldsetDataClassRight);
         IndexedCollection autoIndexedCollectionLeft = DynamicIndexer
               .newAutoIndexedCollection(fieldsetDataAtributesLeft.values());

         autoIndexedCollectionLeft.addAll(fieldsetDataLeft);

         IndexedCollection autoIndexedCollectionRight = DynamicIndexer
               .newAutoIndexedCollection(fieldsetDataAtributesRight.values());
         autoIndexedCollectionRight.addAll(fieldsetDataRight);

         SimpleAttribute<Object, String> simpleAttribute = new SimpleAttribute<Object, String>(
               fieldSetConditions[1].split(".")[1].trim()) {
            @Override
            public String getValue(final Object object) {
               try {
                  return (String) object.getClass().getMethod("get" + fieldSetConditions[1].split("#")[1].trim())
                        .invoke(object, null);
               } catch (Exception e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
               return null;
            }
         };

         for (Record record : recordsOfLeftOp) {
            String attributeValue = record.getValueByFieldName().get(fieldSetConditions[1].split("#")[1].trim());
            ResultSet resultSet = autoIndexedCollectionRight.retrieve(contains(simpleAttribute, attributeValue));

            if (resultSet.isNotEmpty()) {
               Iterator iterator = resultSet.iterator();
               while (iterator.hasNext()) {
                  Object next = iterator.next();
                  createCombinedRecord(records, record, next, fieldSetConditions[1].split("#")[0].trim(),
                        mappingRuleEntry.getKey());
               }
            } else {
               records.add(record);
            }
         }

         recordsByFieldSet.put(mappingRuleEntry.getKey(), records);

      }

      return records;
   }

   private List<Record> getRecords(Map<String, List<Record>> recordsByFieldSet, final String fieldSetCondition) {
      FieldSetDefinition fieldSetDefinition = fieldSets.get(fieldSetCondition);
      List<Record> records = null;
      if (null != fieldSetDefinition) {
         records = recordsByFieldSet.get(fieldSetDefinition.getName());
      } else {
         records = recordsByFieldSet.get(fieldSetCondition);
      }

      return records;
   }

   private Class generateClassFile(String fieldSetName) throws NotFoundException, CannotCompileException {
      String className = null;
      Set<String> attributeSet = null;

      FieldSetDefinition fieldSetDefinition = fieldSets.get(fieldSetName);
      if (null != fieldSetDefinition) {
         className = fieldSetDefinition.getName();
         attributeSet = fieldSetDefinition.getFieldNameToEntityNameMap().keySet();
      } else {
         className = fieldSetName;
         attributeSet = attributes.get(fieldSetName);
      }

      return PojoGenerator.generate(className, attributeSet);
   }

   private void createCombinedRecord(List<Record> records, Record record, Object next, String string, String mappingName) {

      Map<String, String> map = Maps.newHashMap();
      map.putAll(record.getValueByFieldName());
      for (String field : fieldSets.get(string).getFieldNameToEntityNameMap().keySet()) {
         String value = null;
         try {
            value = (String) next.getClass().getMethod("get" + field).invoke(next, null);
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         map.put(field, value);

      }
      attributes.get(mappingName).addAll(map.keySet());

      records.add(new Record(map));

   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   private List populateData(Class fieldsetDataClass, List<Record> records) throws InstantiationException,
         IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException,
         NoSuchMethodException {
      List recordsLists = Lists.newArrayList();

      for (Record record : records) {
         record.getValueByFieldName();
         Object obj = fieldsetDataClass.newInstance();
         for (Map.Entry<String, String> recordEntry : record.getValueByFieldName().entrySet()) {
            fieldsetDataClass.getMethod("set" + recordEntry.getKey(), String.class).invoke(obj, recordEntry.getValue());
         }
         recordsLists.add(obj);

      }

      return recordsLists;

   }

   public void setMappingRules(LinkedHashMap<String, String> mappingRules) {
      this.mappingRules = mappingRules;

   }

}
