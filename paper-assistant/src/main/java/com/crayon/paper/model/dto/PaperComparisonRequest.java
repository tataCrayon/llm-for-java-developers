package com.crayon.paper.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 论文比较请求DTO
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Data
public class PaperComparisonRequest {

    /**
     * 要比较的论文列表（至少2篇）
     */
    @NotEmpty(message = "至少需要提供2篇论文进行比较")
    private List<PaperAnalysisRequest> papers;

    /**
     * 比较维度（可选）
     * 如：["方法创新性", "实验效果", "计算复杂度", "应用前景"]
     */
    private List<String> comparisonAspects;

    /**
     * 比较目的描述（可选）
     */
    private String comparisonPurpose;
}