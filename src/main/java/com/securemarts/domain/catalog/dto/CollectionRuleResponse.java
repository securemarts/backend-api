package com.securemarts.domain.catalog.dto;

import com.securemarts.domain.catalog.entity.CollectionRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Collection rule")
public class CollectionRuleResponse {

    private String publicId;
    private String field;
    private String operator;
    private String value;
    private int position;

    public static CollectionRuleResponse from(CollectionRule r) {
        CollectionRuleResponse res = new CollectionRuleResponse();
        res.setPublicId(r.getPublicId());
        res.setField(r.getField());
        res.setOperator(r.getOperator());
        res.setValue(r.getValue());
        res.setPosition(r.getPosition());
        return res;
    }
}
