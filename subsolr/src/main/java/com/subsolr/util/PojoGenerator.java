package com.subsolr.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * POJO creator to create runtime class definition based on class name and properties
 * 
 * @author vamsiy-mac aditya
 */
public class PojoGenerator {

   public static Class generate(String className, Set<String> properties) throws NotFoundException,
         CannotCompileException {

      ClassPool pool = ClassPool.getDefault();
      CtClass cc = pool.makeClass(className);

      cc.addInterface(resolveCtClass(Serializable.class));

      for (String fieldName : properties) {

         cc.addField(new CtField(resolveCtClass(String.class), fieldName, cc));

         cc.addMethod(generateGetter(cc, fieldName, String.class));

         cc.addMethod(generateSetter(cc, fieldName, String.class));
      }

      return cc.toClass();
   }

   private static CtMethod generateGetter(CtClass declaringClass, String fieldName, Class fieldClass)
         throws CannotCompileException {

      String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

      StringBuffer sb = new StringBuffer();
      sb.append("public ").append(fieldClass.getName()).append(" ").append(getterName).append("(){")
            .append("return this.").append(fieldName).append(";").append("}");
      return CtMethod.make(sb.toString(), declaringClass);
   }

   private static CtMethod generateSetter(CtClass declaringClass, String fieldName, Class fieldClass)
         throws CannotCompileException {

      String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

      StringBuffer sb = new StringBuffer();
      sb.append("public void ").append(setterName).append("(").append(fieldClass.getName()).append(" ")
            .append(fieldName).append(")").append("{").append("this.").append(fieldName).append("=").append(fieldName)
            .append(";").append("}");
      return CtMethod.make(sb.toString(), declaringClass);
   }

   private static CtClass resolveCtClass(Class clazz) throws NotFoundException {
      ClassPool pool = ClassPool.getDefault();
      return pool.get(clazz.getName());
   }
}
