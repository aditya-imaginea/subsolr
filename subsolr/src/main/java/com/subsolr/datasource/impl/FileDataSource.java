package com.subsolr.datasource.impl;

import com.subsolr.datasource.DataSource;
import java.io.Reader;

/**
 * POJO for File Data sources
 * 
 * @author vamsiy-mac aditya
 */
public abstract class FileDataSource implements DataSource {

   public abstract Reader getFileReader(String fileName);

}
