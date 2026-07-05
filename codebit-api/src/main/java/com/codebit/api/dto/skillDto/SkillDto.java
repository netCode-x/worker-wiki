package com.codebit.api.dto.skillDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SkillDto {
    private Long id;
    private String name;
    private String category;
    private Integer proficiency;
    private BigDecimal yearsOfExperience;
    private String icon;
    private String color;
    private Integer sortOrder;
}