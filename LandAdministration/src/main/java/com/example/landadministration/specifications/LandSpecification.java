package com.example.landadministration.specifications;

import com.example.landadministration.entities.Land;
import org.springframework.data.jpa.domain.Specification;

public class LandSpecification {

    public static Specification<Land> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("location"),"%" + location);
        };
    }

    public static Specification<Land> hasCountry(String country) {
        return (root, query, cb) ->
                cb.like(root.get("location"), "%" + country);
    }


    public static Specification<Land> hasUsageType(String usageType) {
        return (root, query, criteriaBuilder) -> {
            if (usageType == null || usageType.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("usage_type"), usageType);
        };
    }

    public static Specification<Land> hasOwnerName(String ownerName) {
        return (root, query, criteriaBuilder) -> {
            if (ownerName == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.concat(
                            criteriaBuilder.concat(
                                    root.join("landOwner").get("firstName"), " "
                            ),
                            root.join("landOwner").get("lastName")
                    ),
                    ownerName
            );

        };
    }
}

