package com.securemarts.domain.catalog.service;

import com.securemarts.domain.catalog.entity.Collection;
import com.securemarts.domain.catalog.entity.CollectionRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluates smart collection rules and returns matching product IDs for a store.
 */
@Component
@Slf4j
public class CollectionRuleEvaluator {

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Returns product IDs that match all or any of the collection's rules (by conditions_operator).
     */
    public List<Long> evaluate(Collection collection, Long storeId) {
        if (collection.getRules() == null || collection.getRules().isEmpty()) {
            return List.of();
        }
        String operator = collection.getConditionsOperator() != null && "any".equalsIgnoreCase(collection.getConditionsOperator()) ? "any" : "all";
        List<Set<Long>> perRule = new ArrayList<>();
        for (CollectionRule rule : collection.getRules()) {
            Set<Long> ids = evaluateRule(rule.getField(), rule.getOperator(), rule.getValue(), storeId);
            perRule.add(ids);
        }
        if (perRule.isEmpty()) return List.of();
        Set<Long> result = "any".equals(operator)
                ? perRule.stream().flatMap(Set::stream).collect(Collectors.toSet())
                : new HashSet<>(perRule.get(0));
        if ("all".equals(operator)) {
            for (int i = 1; i < perRule.size(); i++) {
                result.retainAll(perRule.get(i));
            }
        }
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    private Set<Long> evaluateRule(String field, String op, String value, Long storeId) {
        String normalizedField = field != null ? field.trim().toLowerCase() : "";
        String normalizedOp = op != null ? op.trim().toLowerCase() : "equals";

        if ("title".equals(normalizedField)) {
            return productIdsFromProductColumn(storeId, "title", normalizedOp, value, "VARCHAR");
        }
        if ("product_type".equals(normalizedField)) {
            return productIdsFromProductColumn(storeId, "product_type", normalizedOp, value, "VARCHAR");
        }
        if ("vendor".equals(normalizedField)) {
            return productIdsFromProductColumn(storeId, "vendor", normalizedOp, value, "VARCHAR");
        }
        if ("tag".equals(normalizedField)) {
            return productIdsFromTag(storeId, normalizedOp, value);
        }
        if ("price".equals(normalizedField)) {
            return productIdsFromVariantColumn(storeId, "price_amount", normalizedOp, value);
        }
        if ("compare_at_price".equals(normalizedField)) {
            return productIdsFromVariantColumn(storeId, "compare_at_amount", normalizedOp, value);
        }
        if ("weight".equals(normalizedField)) {
            return productIdsFromVariantColumn(storeId, "weight", normalizedOp, value);
        }
        if ("variant_title".equals(normalizedField)) {
            return productIdsFromVariantColumn(storeId, "title", normalizedOp, value);
        }
        if ("inventory_stock".equals(normalizedField)) {
            return productIdsFromInventoryStock(storeId, normalizedOp, value);
        }
        log.warn("Unknown collection rule field: {}", field);
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    private Set<Long> productIdsFromProductColumn(Long storeId, String column, String op, String value, String type) {
        String sql = "SELECT DISTINCT p.id FROM products p WHERE p.store_id = :storeId AND p.deleted_at IS NULL AND ";
        sql += stringCondition("p." + column, op, value);
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("storeId", storeId);
        if (value != null && !value.isBlank()) q.setParameter("val", value.trim());
        return toLongSet((List<Number>) q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private Set<Long> productIdsFromTag(Long storeId, String op, String value) {
        if (value == null || value.isBlank()) return Set.of();
        String sql = "SELECT DISTINCT p.id FROM products p " +
                "JOIN product_tags pt ON pt.product_id = p.id " +
                "JOIN tags t ON t.id = pt.tag_id " +
                "WHERE p.store_id = :storeId AND p.deleted_at IS NULL AND ";
        sql += stringCondition("t.name", op, value);
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("storeId", storeId);
        q.setParameter("val", value.trim());
        return toLongSet((List<Number>) q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private Set<Long> productIdsFromVariantColumn(Long storeId, String column, String op, String value) {
        String sql = "SELECT DISTINCT p.id FROM products p " +
                "JOIN product_variants pv ON pv.product_id = p.id " +
                "WHERE p.store_id = :storeId AND p.deleted_at IS NULL AND ";
        boolean isNumeric = "price_amount".equals(column) || "compare_at_amount".equals(column) || "weight".equals(column);
        if (isNumeric) {
            BigDecimal num = parseDecimal(value);
            sql += numericCondition("pv." + column, op, num);
        } else {
            sql += stringCondition("pv." + column, op, value);
        }
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("storeId", storeId);
        if (isNumeric) {
            q.setParameter("numVal", parseDecimal(value));
        } else if (value != null && !value.isBlank()) {
            q.setParameter("val", value.trim());
        }
        return toLongSet((List<Number>) q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private Set<Long> productIdsFromInventoryStock(Long storeId, String op, String value) {
        BigDecimal num = parseDecimal(value);
        String sql = "SELECT p.id FROM products p " +
                "JOIN product_variants pv ON pv.product_id = p.id " +
                "JOIN inventory_items ii ON ii.product_variant_id = pv.id AND ii.store_id = :storeId " +
                "JOIN inventory_levels il ON il.inventory_item_id = ii.id " +
                "WHERE p.store_id = :storeId AND p.deleted_at IS NULL " +
                "GROUP BY p.id HAVING COALESCE(SUM(il.quantity_available), 0) " + numericOp(op) + " :numVal";
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("storeId", storeId);
        q.setParameter("numVal", num != null ? num.intValue() : 0);
        return toLongSet((List<Number>) q.getResultList());
    }

    private static Set<Long> toLongSet(List<Number> list) {
        Set<Long> set = new HashSet<>();
        for (Number n : list) set.add(n != null ? n.longValue() : 0L);
        return set;
    }

    private String stringCondition(String column, String op, String value) {
        if (value == null || value.isBlank()) return "1=0";
        String castCol = "CAST(COALESCE(" + column + ", '') AS VARCHAR)";
        switch (op) {
            case "not_equals":
                return castCol + " != :val";
            case "contains":
                return "LOWER(" + castCol + ") LIKE LOWER('%' || :val || '%')";
            case "starts_with":
                return "LOWER(" + castCol + ") LIKE LOWER(:val || '%')";
            case "ends_with":
                return "LOWER(" + castCol + ") LIKE LOWER('%' || :val)";
            default:
                return "LOWER(" + castCol + ") = LOWER(:val)";
        }
    }

    private String numericCondition(String column, String op, BigDecimal num) {
        if (num == null) return "1=0";
        switch (op) {
            case "not_equals":
                return "COALESCE(" + column + ", 0) != :numVal";
            case "greater_than":
                return "COALESCE(" + column + ", 0) > :numVal";
            case "less_than":
                return "COALESCE(" + column + ", 0) < :numVal";
            default:
                return "COALESCE(" + column + ", 0) = :numVal";
        }
    }

    private String numericOp(String op) {
        if (op == null) return "=";
        switch (op) {
            case "not_equals": return "!=";
            case "greater_than": return ">";
            case "less_than": return "<";
            default: return "=";
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
