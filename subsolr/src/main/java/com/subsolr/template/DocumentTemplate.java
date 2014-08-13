package com.subsolr.template;

import com.subsolr.config.ConfigurationExtractor;
import com.subsolr.model.Join;
import com.subsolr.model.Record;
import com.subsolr.transformer.ModelTransformer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pojo for Document definition having fields,fieldsets, feildsetMappings and
 * mapping rules among field sets
 *
 * @author vamsiy-mac aditya
 *
 */
public class DocumentTemplate {

    List<Record> records = new ArrayList<>();
    private String documentName;
    private Map<String, FieldSetDefinition> fieldSets;
    private final Map<String, Set<String>> attributes = new HashMap<>(); // for joins
    private ModelTransformer transformer;
    
    private ConfigurationExtractor extractor;

    public void setExtractor(ConfigurationExtractor extractor) {
        this.extractor = extractor;
    }

    public ModelTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(ModelTransformer transformer) {
        this.transformer = transformer;
    }

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

    public List<Record> getRecordsToBeIndexed() {
        Map<String, List<Record>> recordsByFieldSet = new HashMap<>();
        fieldSets.entrySet().stream().forEach((fieldSetEntry) -> {
            recordsByFieldSet.put(fieldSetEntry.getKey(),
                    fieldSetEntry.getValue().getDataSource().getRecords(fieldSetEntry.getValue()));
        });
        List<Record> combined= combinedFieldSets(recordsByFieldSet);
        if (this.transformer == null) {
            return combined;
        } else {
            return this.transformer.transform(combined, new HashMap<>());
        }
    }

    private List<Record> getMatchingRecords(List<Record> records, String key, String value) {
        if(records==null) {
            return new ArrayList<>();
        }
        List<Record> result = new ArrayList<>();
        records.stream().filter((rd) -> (rd.getValueByFieldName().get(key).equals(value))).forEach((rd) -> {
            result.add(rd);
        });
        return result;
       
    }

    private List<Record> combinedFieldSets(Map<String, List<Record>> recordsByFieldSet) {
        Map<String, Join> joins = this.extractor.getJoins();
        if (recordsByFieldSet.size() == 1 || joins.isEmpty()) {
            return recordsByFieldSet.get(recordsByFieldSet.keySet().iterator().next());
        }
        
        joins.entrySet().stream().map((Map.Entry<String, Join> mappingRuleEntry) -> {
            Join join = mappingRuleEntry.getValue();
            
            attributes.put(mappingRuleEntry.getKey(), new HashSet<>());
            String joinFieldOnLeft = join.getLeftKey();
            String joinFieldOnRight = join.getRightKey();
            List<Record> recordsOfLeftOp = getRecords(recordsByFieldSet, join.getLeftFieldSet());
            List<Record> recordsOfRightOp = getRecords(recordsByFieldSet, join.getRightFieldSet());
            recordsOfLeftOp.stream().forEach((record) -> {
                String attributeValue = record.getValueByFieldName().get(joinFieldOnLeft);
                List<Record> result = getMatchingRecords(recordsOfRightOp, joinFieldOnRight, attributeValue);
                if (!result.isEmpty()) {
                    result.stream().forEach((rm) -> {
                        createCombinedRecord(records, record, rm, join.getRightFieldSet(),
                                mappingRuleEntry.getKey());
                    });
                } else {
                    records.add(record);
                }
            });
            return mappingRuleEntry;
        }).forEach((mappingRuleEntry) -> {
            if(!records.isEmpty() &&records.size()>0) {
                List<Record> newList= new ArrayList<>();
                newList.addAll(records);
            recordsByFieldSet.put(mappingRuleEntry.getValue().getLeftFieldSet(), newList);
            records.clear();
            }
        });
        List<Record> recordsList = new ArrayList<>();
        recordsByFieldSet.keySet().stream().forEach((s) -> {
            recordsList.addAll(recordsByFieldSet.get(s));
        });
        return recordsList;
        
        
    }

    private List<Record> getRecords(Map<String, List<Record>> recordsByFieldSet, final String fieldSetCondition) {
        List<Record> localRecords = recordsByFieldSet.get(fieldSetCondition);
        return localRecords;
    }

    private void createCombinedRecord(List<Record> records, Record recordOnLeft, Record recordOnRight, String string, String mappingName) {
        Map<String, String> map = new HashMap<>();
        map.putAll(recordOnLeft.getValueByFieldName());
        fieldSets.get(string).getFieldNameToEntityNameMap().keySet().stream().forEach((String field) -> {
            String value =recordOnRight.getValueByFieldName().get(field);
            map.put(field, value);
        });
        attributes.get(mappingName).addAll(map.keySet());
        Record rec= new Record(map);
        rec.setAutoGenKey(recordOnLeft.getAutoGenKey());
        rec.setName(recordOnLeft.getName()+"+"+recordOnRight.getName());
        records.add(rec);
    }

}
