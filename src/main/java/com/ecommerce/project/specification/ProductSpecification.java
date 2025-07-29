package com.ecommerce.project.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ecommerce.project.model.Product;

import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

    public static Specification<Product> productNameLikeAny(List<String> names) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String name : names) {
                predicates.add(builder.like(
                    builder.lower(root.get("productName")),
                    "%" + name.toLowerCase() + "%"
                ));
            }
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
