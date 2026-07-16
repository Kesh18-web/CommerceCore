package com.commercecore.catalog.service;

import com.commercecore.catalog.entity.Attribute;

import java.util.List;

public interface AttributeService {
    Attribute createAttribute(String name, String dataType);
    Attribute updateAttribute(Long id, String newName, String newDataType);
    List<Attribute> getAllAttributes();
    Attribute getAttributeByName(String name);
    void deleteAttribute(Long id);
}
