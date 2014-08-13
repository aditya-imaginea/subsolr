package com.subsolr.datasource;

import com.subsolr.model.Record;
import com.subsolr.template.FieldSetDefinition;
import java.util.List;

public interface DataSource {
   String getDataSourceName();
   // returns <fieldsetname , list of data records>
   List<Record> getRecords(FieldSetDefinition fieldSetDefinition);
}
