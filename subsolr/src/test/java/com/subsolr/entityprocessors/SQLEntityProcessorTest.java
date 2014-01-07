package com.subsolr.entityprocessors;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.subsolr.contextprocessor.FieldContextProcessor;
import com.subsolr.contextprocessor.model.FieldDefinition;
import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.entityprocessors.datasources.SQLDataSource;
import com.subsolr.entityprocessors.model.Record;

@RunWith(MockitoJUnitRunner.class)
public class SQLEntityProcessorTest {

    private SQLEntityProcessor sqlEntityProcessor;

    private FieldSetDefinition fieldSetDefinition;

    @Mock
    private FieldContextProcessor mockedFieldContextProcessor;

    @Before
    public void setup() {
        String sql = "SELECT emp_no as EMP_ID,  first_name as EMP_NAME, gender as EMP_GENDER, "
                + "hire_date as EMP_HIRE_DATE FROM  employees";
        sqlEntityProcessor = new SQLEntityProcessor();
        sqlEntityProcessor.setQuery(sql);

        fieldSetDefinition = new FieldSetDefinition();

        SQLDataSource mockedDataSource = new SQLDataSource();
        mockedDataSource.setDriver("com.mysql.jdbc.Driver");
        mockedDataSource.setUrl("jdbc:mysql://localhost:3306/subsolr");
        mockedDataSource.setUserId("root");
        mockedDataSource.setPassword("root");
        fieldSetDefinition.setDataSource(mockedDataSource);

        Map<String, String> map = new HashMap<String, String>();
        map.put("EMP_ID", "EMP_ID");
        map.put("EMP_NAME", "EMP_NAME");
        map.put("EMP_GENDER", "EMP_GENDER");
        map.put("EMP_HIRE_DATE", "EMP_HIRE_DATE");
        fieldSetDefinition.setFieldName2DataSourceMap(map);

        sqlEntityProcessor.setFieldSetDefinition(fieldSetDefinition);
        sqlEntityProcessor.setFieldContextProcessor(mockedFieldContextProcessor);
    }

    @Test
    public void testGetRecords() {

        mockFieldContext();
        List<Record> records = sqlEntityProcessor.getRecords();
        
        // test number of records found.
        Assert.assertTrue(records.size() == 10);
        
        // test if record has 4 specified fields
        Assert.assertTrue(records.get(0).getValueByIndexName().size() == 4);
    }

    private void mockFieldContext() {

        when(mockedFieldContextProcessor.getFieldDefinitionsByName("EMP_ID"))
                .thenReturn(new FieldDefinition("EMP_ID", null, true, true, true, false));
        when(mockedFieldContextProcessor.getFieldDefinitionsByName("EMP_NAME")).thenReturn(
                new FieldDefinition("EMP_NAME", null, true, true, true, false));
        when(mockedFieldContextProcessor.getFieldDefinitionsByName("EMP_GENDER")).thenReturn(
                new FieldDefinition("EMP_GENDER", null, true, true, true, false));
        when(mockedFieldContextProcessor.getFieldDefinitionsByName("EMP_HIRE_DATE")).thenReturn(
                new FieldDefinition("EMP_HIRE_DATE", null, true, true, true, false));
    }
}
