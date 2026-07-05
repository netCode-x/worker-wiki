package com.codebit.api.dto.skillDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 技能公开响应DTO（用于前端展示）
 */
@Data
@Builder
public class SkillPublicDto {
    /**
     * 技能名称
     */
    private String name;

    /**
     * 技能分类（后端、前端、数据库、DevOps等）
     */
    private String category;

    /**
     * 熟练度（0-100）
     */
    private Integer proficiency;

    /**
     * 经验年限
     */
    private BigDecimal yearsOfExperience;

    /**
     * 图标（emoji或图标类名）
     */
    private String icon;

    /**
     * 颜色标识
     */
    private String color;
}