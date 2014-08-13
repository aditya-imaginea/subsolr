/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.subsolr.transformer.impl;

import com.subsolr.model.Record;
import com.subsolr.transformer.ModelTransformer;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sushantk
 */
public class DefaultTransformerImpl implements ModelTransformer {

    private Map<String, Object> keys ;
    @Override
    public List<Record> transform(List<Record> records,Map<String,Object> params) {
        return records;
    }

    @Override
    public void setKeys(Map<String, Object> params) {
        this.keys = params;
    }

    @Override
    public Map<String, Object> getKeys() {
        return keys;
    }
}
