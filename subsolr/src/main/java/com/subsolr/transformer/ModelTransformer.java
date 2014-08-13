/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.subsolr.transformer;

import com.subsolr.model.Record;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Transformer service implementation for list of records.
 * @author sushantk
 */
public interface ModelTransformer {
    
    default public Map<String,Object> getKeys() {
        return new HashMap<>();
    }
    default public void setKeys(Map<String,Object> params) {
        
    }
    List<Record> transform(List<Record> records,Map<String,Object> params);
    
}
