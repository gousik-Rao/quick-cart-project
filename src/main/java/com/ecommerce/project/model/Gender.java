package com.ecommerce.project.model;

public enum Gender {
    MALE("male"), FEMALE("female");
    final String name;

    public String getName() {
        return name;
    }

    Gender(String name) {
        this.name = name;
    }
}
