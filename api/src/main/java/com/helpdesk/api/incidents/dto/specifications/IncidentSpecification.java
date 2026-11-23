package com.helpdesk.api.incidents.dto.specifications;

import com.helpdesk.api.incidents.entities.Incident;
import com.helpdesk.api.incidents.entities.enums.IncidentPriority;
import com.helpdesk.api.incidents.entities.enums.IncidentStatus;
import org.springframework.data.jpa.domain.Specification;

public class IncidentSpecification {
    public static Specification<Incident> hasNameLike(String keyword) {
        return (root, query, cb) -> {
            if(keyword==null || keyword.isEmpty()){
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%"+keyword.toLowerCase()+"%");
        };
    }

    public static Specification<Incident> hasStatus(IncidentStatus status) {
        return (root, query, cb) -> {
            if(status==null){
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }
    public static Specification<Incident> hasPriority(IncidentPriority priority) {
        return(root, query, cb)->{
            if(priority==null){
                return null;
            }

            return cb.equal(root.get("priority"), priority);
        };
    }
}
