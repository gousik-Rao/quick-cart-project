package com.ecommerce.project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("male"), FEMALE("female");
    final String name;

    @JsonValue
    public String toLower() {
    	return this.name().toLowerCase();
    }
    
    @JsonCreator
    public static Gender fromString(String value) {
    	return Gender.valueOf(value.toUpperCase());
    }
    
    public String getName() {
        return name;
    }

    Gender(String name) {
        this.name = name;
    }
}
