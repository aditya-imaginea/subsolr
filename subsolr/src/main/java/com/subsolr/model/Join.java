/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.subsolr.model;

/**
 *
 * @author sushantk
 */
public class Join {
    
    private String leftFieldSet;
    private String leftKey;
    private String rightFieldSet;
    private String rightKey;
    private String id;
    
    
    
    
    
    @Override
    public String toString() {
        return "["+leftFieldSet+"->"+leftKey+"] =>["+rightFieldSet+"->"+rightKey+"]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
    

    public String getLeftFieldSet() {
        return leftFieldSet;
    }

    public void setLeftFieldSet(String leftFieldSet) {
        this.leftFieldSet = leftFieldSet;
    }

    public String getLeftKey() {
        return leftKey;
    }

    public void setLeftKey(String leftKey) {
        this.leftKey = leftKey;
    }

    public String getRightFieldSet() {
        return rightFieldSet;
    }

    public void setRightFieldSet(String rightFieldSet) {
        this.rightFieldSet = rightFieldSet;
    }

    public String getRightKey() {
        return rightKey;
    }

    public void setRightKey(String rightKey) {
        this.rightKey = rightKey;
    }
    
    
    
}
