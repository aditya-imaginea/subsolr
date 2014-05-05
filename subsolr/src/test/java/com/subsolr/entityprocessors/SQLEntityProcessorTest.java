package com.subsolr.entityprocessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.entityprocessors.datasources.SQLDataSource;
import com.subsolr.entityprocessors.model.Record;

@RunWith(MockitoJUnitRunner.class)
public class SQLEntityProcessorTest {

   private SQLEntityProcessor dbEntityProcessor;

   private FieldSetDefinition fieldSetDefinition;

   @Before
   public void setup() {
      String sql = "SELECT emp_no as EMP_ID,  first_name as EMP_NAME, gender as EMP_GENDER, "
            + "hire_date as EMP_HIRE_DATE FROM  employees";
      dbEntityProcessor = new SQLEntityProcessor();

      fieldSetDefinition = new FieldSetDefinition();

      String driver = "com.mysql.jdbc.Driver";
      String url = "jdbc:mysql://localhost:3306/subsolr";
      String userId = "root";
      String password = "root";
      SQLDataSource mockedDataSource = new SQLDataSource(driver, url, userId, password);
      fieldSetDefinition.setDataSource(mockedDataSource);

      Map<String, String> map = new HashMap<String, String>();
      map.put("EMP_ID", "EMP_ID");
      map.put("EMP_NAME", "EMP_NAME");
      map.put("EMP_GENDER", "EMP_GENDER");
      map.put("EMP_HIRE_DATE", "EMP_HIRE_DATE");
      fieldSetDefinition.setFieldNameToEntityNameMap(map);

      Map<String, String> properties = Maps.newHashMap();
      properties.put("SQLQuery", sql);
      fieldSetDefinition.setPropertiesForEntityProcessor(properties);
   }

   @Test
   public void testGetRecords() {

      List<Record> records = dbEntityProcessor.getRecords(fieldSetDefinition);

      // test number of records found.
      Assert.assertTrue(records.size() == 10);

      // test if record has 4 specified fields
      Assert.assertTrue(records.get(0).getValueByFieldName().size() == 4);
   }

}
