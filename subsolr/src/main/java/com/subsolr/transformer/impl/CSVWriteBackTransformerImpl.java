/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.subsolr.transformer.impl;

import com.subsolr.model.Record;
import com.subsolr.transformer.ModelTransformer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sushantk
 */
public class CSVWriteBackTransformerImpl implements ModelTransformer{

    
    private Map<String, PrintWriter> writerMap = new HashMap<>();
    
    private void cleanUp() {
        for(String key : writerMap.keySet()) {
            writerMap.get(key).close();
        }
    }
    
    
    private PrintWriter getWriter(String key) throws FileNotFoundException {
        if(writerMap.containsKey(key)) {
            return writerMap.get(key);
        } else {
            PrintWriter pw = new PrintWriter(new File(key+".csv"));
            writerMap.put(key, pw);
            return pw;
        }
    }
    
    private void writeRecord(Record rd) throws FileNotFoundException {
        PrintWriter pw = getWriter(rd.getName());
        for(String key : rd.getValueByFieldName().keySet()) {
             pw.print(rd.getValueByFieldName().get(key)+",");
        }
        pw.println();
    }
    
    @Override
    public List<Record> transform(List<Record> records, Map<String, Object> params) {
        System.out.println("---------- CSV WriteBack -------");
        try {
            for(Record rd : records) {
                writeRecord(rd);
            }
            cleanUp();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return records;
    }
    
}
