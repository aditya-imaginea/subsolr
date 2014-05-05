package com.subsolr.entityprocessors;

import java.util.List;

import com.subsolr.contextprocessor.model.FieldSetDefinition;
import com.subsolr.entityprocessors.model.Record;

/**
 * Entity processor for generating records defined in Fieldset Definition
 * 
 * @author vamsiy-mac aditya
 */

public interface EntityProcessor {
   List<Record> getRecords(FieldSetDefinition fieldSetDefinition);
}
