package com.subsolr.datasource;

import java.util.List;

import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.datasource.model.Record;

public interface DataSource {
   String getDataSourceName();
   // returns <fieldsetname , list of data records>
   List<Record> getRecords(FieldSetDefinition fieldSetDefinition);
}
