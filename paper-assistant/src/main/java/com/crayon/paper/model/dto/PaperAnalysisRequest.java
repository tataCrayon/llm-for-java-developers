package com.crayon.paper.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 论文分析请求DTO
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Data
public class PaperAnalysisRequest {

    /**
     * 论文标题（必填）
     */
    @NotBlank(message = "论文标题不能为空")
    private String title;

    /**
     * 论文作者列表
     */
    private List<String> authors;

    /**
     * 论文摘要
     */
    private String abstractText;

    /**
     * 论文链接
     */
    private String url;

    /**
     * 发布日期
     */
    private String publishedDate;

    /**
     * 论文分类/领域
     */
    private String category;

    /**
     * 分析重点（可选）
     * 如：["技术创新点", "实验方法", "应用场景"]
     */
    private List<String> analysisAspects;
}