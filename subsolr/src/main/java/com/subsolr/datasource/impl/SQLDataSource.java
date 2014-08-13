package com.subsolr.datasource.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.subsolr.datasource.DataSource;
import com.subsolr.model.Record;
import com.subsolr.template.FieldSetDefinition;
import com.subsolr.util.XMLParserUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.w3c.dom.Node;

/**
 * POJO for SQL Data sources
 *
 * @author vamsiy-mac aditya
 */
public class SQLDataSource implements DataSource {

    public static final Logger logger = LoggerFactory.getLogger(SQLDataSource.class);
    private int autoKeyCount=1;
    private String dataSourceName;
    private String driver;
    private String url;
    private String userId;
    private String password;

    public SQLDataSource(Node dataSource, XMLParserUtil xmlParserUtil, XPath xPath) throws XPathExpressionException {

        String sourceName = xmlParserUtil.getAttributeValueInNode(dataSource, "id");
        Node hostNode = (Node) xPath.evaluate("./host", dataSource, XPathConstants.NODE);
        Node userNameNode = (Node) xPath.evaluate("./userid", dataSource, XPathConstants.NODE);
        Node passwordNode = (Node) xPath.evaluate("./password", dataSource, XPathConstants.NODE);
        Node driverNode = (Node) xPath.evaluate("./driver", dataSource, XPathConstants.NODE);
        setDataSource(sourceName, driverNode.getTextContent(), hostNode.getTextContent(),
                userNameNode.getTextContent(), passwordNode.getTextContent());
    }

    private void setDataSource(String dataSourceName, String driver, String url, String userId, String password) {
        this.setDataSourceName(dataSourceName);
        this.driver = driver;
        this.url = url;
        this.userId = userId;
        this.password = password;

    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    private void resetAutoKey() {
        autoKeyCount=1;
    }

    @Override
    public List<Record> getRecords(FieldSetDefinition fieldSetDefinition) {
        SQLDataSource sqlDataSource = (SQLDataSource) fieldSetDefinition.getDataSource();
        final List<Record> records = Lists.newArrayList();
        final Map<String, String> fieldNameToEntityNameMap = fieldSetDefinition.getFieldNameToEntityNameMap();
        JdbcTemplate jdbcTemplate = getJdbcTempate(sqlDataSource);
        resetAutoKey();
        jdbcTemplate.query(fieldSetDefinition.getPropertiesForEntityProcessor().get("SQLQuery"),
                new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        Map<String, String> valueByIndexName = Maps.newHashMap();
                        for (String fieldName : fieldNameToEntityNameMap.keySet()) {
                            String fieldValue = rs.getString(fieldNameToEntityNameMap.get(fieldName));
                            if (fieldValue == null) {
                                fieldValue = " ";
                            }
                            valueByIndexName.put(fieldName, fieldValue);
                        }
                        Record rec = new Record(valueByIndexName);
                        rec.setAutoGenKey(fieldSetDefinition.getName()+autoKeyCount);
                        autoKeyCount++;
                        rec.setFieldSet(fieldSetDefinition.getName());
                        rec.setName(fieldSetDefinition.getName());
                        records.add(rec);
                    }
                });
        if (fieldSetDefinition.getTransformer() == null) {
            return records;
        } else {
            return fieldSetDefinition.getTransformer().transform(records, new HashMap<>());
        }
    }

    private JdbcTemplate getJdbcTempate(SQLDataSource sqlDataSource) {
        JdbcTemplate jdbcTemplate = null;
        try {
            //Class.forName(sqlDataSource.getDriver());
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(sqlDataSource.getDriver());
            dataSource.setUsername(sqlDataSource.getUserId());
            dataSource.setPassword(sqlDataSource.getPassword());
            dataSource.setUrl(sqlDataSource.getUrl());
            jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception e) {
            logger.error("Exception occurred while getting connection" + e);
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return jdbcTemplate;
    }

}
