package com.ecommerce.project.exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -111389364138776172L;
    
	String resourceName;
    String field;
    String fieldName;
    Long fieldId;

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String resourceName, String field, String fieldName) {
        super("%s not found with %s : %s".formatted(resourceName, field, fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }
    
    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        super("%s not found with %s : %d".formatted(resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldId = fieldId;
    }


}
