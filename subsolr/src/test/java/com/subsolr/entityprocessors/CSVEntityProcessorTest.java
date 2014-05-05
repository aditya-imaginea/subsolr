package com.subsolr.entityprocessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Maps;
import com.subsolr.contextprocessor.FieldContextProcessor;
import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.entityprocessors.datasources.FileDataSource;
import com.subsolr.entityprocessors.datasources.LocalFileDataSource;
import com.subsolr.entityprocessors.model.Record;

public class CSVEntityProcessorTest {

   private CSVEntityProcessor csvEntityProcessor;

   private FieldSetDefinition fieldSetDefinition;

   @Mock
   private FieldContextProcessor mockedFieldContextProcessor;

   @Before
   public void setUp() throws Exception {

      fieldSetDefinition = new FieldSetDefinition();

      FileDataSource mockedDataSource = new LocalFileDataSource("data");
      fieldSetDefinition.setDataSource(mockedDataSource);

      Map<String, String> map = new HashMap<String, String>();
      map.put("EMP_ID", "1");
      map.put("EMP_NAME", "2");
      map.put("EMP_GENDER", "3");
      map.put("EMP_HIRE_DATE", "4");
      fieldSetDefinition.setFieldNameToEntityNameMap(map);

      Map<String, String> properties = Maps.newHashMap();
      String fileName = "employee.csv";
      properties.put("File", fileName);
      fieldSetDefinition.setPropertiesForEntityProcessor(properties);

      csvEntityProcessor = new CSVEntityProcessor();
   }

   @Test
   public void testGetRecords() {

      List<Record> records = csvEntityProcessor.getRecords(fieldSetDefinition);

      // test number of records found.
      Assert.assertTrue(records.size() == 5);

      // test if record has 4 specified fields
      Assert.assertTrue(records.get(0).getValueByFieldName().size() == 4);
   }

}
