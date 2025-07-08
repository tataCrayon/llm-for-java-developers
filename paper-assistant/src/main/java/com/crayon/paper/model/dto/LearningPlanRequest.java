package com.crayon.paper.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 学习计划生成请求对象
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Data
public class LearningPlanRequest {

    /**
     * 用户背景描述
     * 如："有Python基础的机器学习初学者"、"深度学习工程师"等
     */
    @NotBlank(message = "用户背景不能为空")
    private String userBackground;

    /**
     * 学习目标
     * 如："掌握Transformer架构"、"理解大语言模型训练过程"等
     */
    @NotBlank(message = "学习目标不能为空")
    private String learningGoals;

    /**
     * 可投入的学习时间（小时/周）
     */
    private Integer weeklyHours;

    /**
     * 期望完成时间（周）
     */
    private Integer targetWeeks;

    /**
     * 特别感兴趣的研究方向
     */
    private List<String> interestedTopics;

    /**
     * 学习偏好
     * 如：["理论深入", "实践导向", "论文阅读", "代码实现"]
     */
    private List<String> learningPreferences;
}