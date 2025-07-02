package com.foroescolar.model;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


import java.util.*;

public class UpdatedEntities {

    public static <T> T update(T entity, T updatedEntity) {
        BeanUtils.copyProperties(updatedEntity, entity, getNullPropertiesNames(updatedEntity));
        return (T) entity;
    }

    private static String[] getNullPropertiesNames(Object updateEntity) {
        final BeanWrapper src = new BeanWrapperImpl(updateEntity);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }



}
