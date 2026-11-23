package com.helpdesk.api.core.models;

import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class GenericSpec<T>{
    public static <T> Specification<T> equal(String field, Object value){
        return (root, query, cb) -> value == null ? null : cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> like(String field, String value){
        return(root, query, cb) -> (value ==null || value.isBlank()) ? null : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }
        public static <T> Specification<T> equalNested(String relation, String field, Object value){
        return (root, query, cb) -> value == null ? null : cb.equal(root.get(relation).get(field), value);
    }
    public static <T, Y extends Comparable<? super Y>> Specification<T> between(String field, Y start, Y end) {
        return (root, query, cb) -> {
            if(start == null && end == null) return null;
            if(start != null && end != null) return cb.between(root.get(field), start, end);
            return start != null ? cb.greaterThanOrEqualTo(root.get(field), start) :
                    cb.lessThanOrEqualTo(root.get(field), end);
        };
    }

    public static <T> Specification<T> in(String field, Collection<?> values){
        return (root, query, cb) -> (values == null || values.isEmpty()) ? null :root.get(field).in(values);
    }

}
