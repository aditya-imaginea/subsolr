/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.subsolr.transformer.impl;

import com.subsolr.model.Record;
import com.subsolr.transformer.ModelTransformer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sushantk
 */
public class DeDupTransformerImpl implements ModelTransformer {
    
    private Map<String, Object> keys;

    /**
     *
     * @param records
     * @param params
     * @return
     */
    @Override
    public List<Record> transform(List<Record> records, Map<String, Object> params) {
        Map<String,Record> dedupMap = new HashMap<>();
        if(keys==null || keys.isEmpty()) {
            return records;
        }
        String[] uKeys = (String[]) keys.get("keys");
        final String uKey=uKeys[0];
        records.stream().forEach((rd) -> {
            dedupMap.put(rd.getValueByFieldName().get(uKey), rd);
        });
        return new ArrayList<>(dedupMap.values());
    }

    @Override
    public Map<String, Object> getKeys() {
        return keys;
    }

    @Override
    public void setKeys(Map<String, Object> params) {
        this.keys = params;
    }
    
}
