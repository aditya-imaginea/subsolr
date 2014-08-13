package com.subsolr.template;

import com.subsolr.datasource.DataSource;
import com.subsolr.model.Record;
import com.subsolr.transformer.ModelTransformer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pojo for Fieldset definition having fields entity processor mappings
 *
 * @author vamsiy-mac aditya
 *
 */
public class FieldSetDefinition {

    private Map<String, String> fieldNameToEntityNameMap;
    private DataSource dataSource;
    private Map<String, String> propertiesForEntityProcessor;
    private String name;
    private ModelTransformer transformer;

    public ModelTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(ModelTransformer transformer) {
        this.transformer = transformer;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public Map<String, String> getFieldNameToEntityNameMap() {
        return fieldNameToEntityNameMap;
    }

    public void setFieldNameToEntityNameMap(Map<String, String> fieldNameToEntityNameMap) {
        this.fieldNameToEntityNameMap = fieldNameToEntityNameMap;
    }

    public Map<String, String> getPropertiesForEntityProcessor() {
        return propertiesForEntityProcessor;
    }

    public void setPropertiesForEntityProcessor(Map<String, String> propertiesForEntityProcessor) {
        this.propertiesForEntityProcessor = propertiesForEntityProcessor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Record> getRecords() {
        
        if (this.transformer == null) {
            return this.dataSource.getRecords(this);
        }
        return this.transformer.transform(this.dataSource.getRecords(this), new HashMap<>());
    }

}
