package com.subsolr.datasource.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.subsolr.model.Record;
import com.subsolr.template.FieldSetDefinition;
import com.subsolr.util.XMLParserUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

public class LocalFileDataSource extends FileDataSource {

    private static final String CSV_EXTENSION = "csv";
    private String dataSourceName;
    private String basePath;
    private String fileName;

    public LocalFileDataSource() {

    }

    public LocalFileDataSource(Node dataSource, XMLParserUtil xmlParserUtil, XPath xPath)
            throws XPathExpressionException {
        dataSourceName = xmlParserUtil.getAttributeValueInNode(dataSource, "id");
        Node basePathNode = (Node) xPath.evaluate("./local_path", dataSource, XPathConstants.NODE);
        Node fileNameNode = (Node) xPath.evaluate("./file_name", dataSource, XPathConstants.NODE);
        setDataSource(dataSourceName, basePathNode.getTextContent(), fileNameNode.getTextContent());
    }

    private void setDataSource(String dataSourceName, String basePath, String fileName) {
        this.dataSourceName = dataSourceName;
        this.basePath = basePath;
        this.fileName = fileName;
    }

    @Override
    public FileReader getFileReader(String fileName) {

        File file = new File(getBasePath(), fileName).getAbsoluteFile();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fileReader;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

    @Override
    public List<Record> getRecords(FieldSetDefinition fieldSetDefinition) {
        FileDataSource fileDataSource = (FileDataSource) fieldSetDefinition.getDataSource();
        Map<String, String> fieldNameToEntityNameMap = fieldSetDefinition.getFieldNameToEntityNameMap();
        int autoKey=1;
        validateFile(fileName);
        Reader reader = fileDataSource.getFileReader(fileName);
        List<String[]> lines = readCSV(reader);
        List<Record> records = Lists.newArrayList();
        for (String[] line : lines) {
            Map<String, String> valueByIndexName = Maps.newHashMap();
            for (String fieldName : fieldNameToEntityNameMap.keySet()) {
                String fieldValue = line[Integer.parseInt(fieldNameToEntityNameMap.get(fieldName)) - 1];
                valueByIndexName.put(fieldName, fieldValue);
            }
            Record rec = new Record(valueByIndexName);
            rec.setAutoGenKey(fieldSetDefinition.getName()+autoKey);
            autoKey++;
            rec.setFieldSet(fieldSetDefinition.getName());
            rec.setName(fieldSetDefinition.getName());
            records.add(rec);
        }
        if(fieldSetDefinition.getTransformer()==null) {
          return records;
      } else {
          return fieldSetDefinition.getTransformer().transform(records, new HashMap<>());
      }
    }

    private List<String[]> readCSV(Reader reader) {
        Scanner csvReader = new Scanner(reader);
        List<String[]> lines = Lists.newArrayList();
        try {
            String[] line = csvReader.nextLine().split(",");
            while (line != null) {
                lines.add(line);
                line = csvReader.nextLine().split(",");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            csvReader.close();
        } 
        return lines;
    }

    private void validateFile(String fileName) {

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        if (extension == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
        if (!CSV_EXTENSION.equals(extension)) {
            throw new IllegalArgumentException("File extension should be " + CSV_EXTENSION);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

}
