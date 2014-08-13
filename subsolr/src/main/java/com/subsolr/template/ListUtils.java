/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.subsolr.template;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sushantk
 */
public class ListUtils {
    
    private List<Integer> intersect(List<Integer> list1,List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        result.addAll(list1);
        result.retainAll(list2);
        return result;
    }
    
    public List<Integer> joinLists(List<Integer> list1,List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        result.addAll(list1);
        result.addAll(list2);
        List<Integer> intersect = intersect(list1, list2);
        result.removeAll(intersect);
        result.addAll(intersect);
        return result;
        
    }
    
    public static void main(String[] args) {
        List<Integer> a1 = new ArrayList<>();
        List<Integer> a2 = new ArrayList<>();
        for(int i=1;i<=10;i++) {
            a1.add(i);
        }
        for(int i=6;i<=15;i++) {
            a2.add(i);
        }
        
        ListUtils lu = new ListUtils();
        List<Integer> result = lu.joinLists(a1, a2);
        result.stream().forEach((x) -> {
            System.out.println(x);
        });
        
        
        
    }
    
}
