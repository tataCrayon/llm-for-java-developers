package com.crayon.paper.controller;

import com.crayon.paper.model.ApiResponse;
import com.crayon.paper.model.dto.LearningPlanRequest;
import com.crayon.paper.model.dto.PaperAnalysisRequest;
import com.crayon.paper.model.dto.PaperComparisonRequest;
import com.crayon.paper.service.LLMPaperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * LLM论文研究API控制器
 * 提供论文搜索、分析、对比等REST API接口。
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Slf4j
@RestController
@RequestMapping("/papers")
@RequiredArgsConstructor
public class LLMPaperController {


    private final LLMPaperService paperService;

    /**
     * 搜索LLM相关论文
     * 根据查询关键词搜索相关论文并提供学习导向的分析
     *
     * @param query 搜索查询词
     * @return 搜索结果和分析
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<String>> searchPapers(
            @RequestParam String query) {
        return paperService.searchPapers(query);
    }

    /**
     * 深度分析特定论文
     * 对单篇论文进行详细的技术分析和学习指导
     *
     * @param request 论文分析请求
     * @return 深度分析结果
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<String>> analyzePaper(
            @Valid @RequestBody PaperAnalysisRequest request) {
        return paperService.analyzePaper(request);
    }

    /**
     * 比较多篇论文
     * 对多篇相关论文进行对比分析，帮助理解技术演进
     *
     * @param request 论文比较请求
     * @return 对比分析结果
     */
    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<String>> comparePapers(
            @Valid @RequestBody PaperComparisonRequest request) {
        return paperService.comparePapers(request);
    }

    /**
     * 生成个性化学习计划
     * 根据用户背景和目标生成定制化的学习路径
     *
     * @param request 学习计划请求
     * @return 个性化学习计划
     */
    @PostMapping("/plan/Learning/get")
    public ResponseEntity<ApiResponse<String>> generateLearningPlan(
            @Valid @RequestBody LearningPlanRequest request) {
        return paperService.generateLearningPlan(request);
    }

    /**
     * 获取热门研究方向
     * 返回当前LLM领域的热门研究方向和推荐
     *
     * @return 热门研究方向信息
     */
    @GetMapping("/hot/topics/get")
    public ResponseEntity<ApiResponse<String>> getHotTopics() {
        return paperService.getHotTopics();
    }

    /**
     * 获取学习资源推荐
     * 根据用户水平推荐合适的学习资源
     *
     * @param level 学习水平：beginner, intermediate, advanced
     * @return 推荐的学习资源
     */
    @GetMapping("/resources/get")
    public ResponseEntity<ApiResponse<String>> getLearningResources(
            @RequestParam(defaultValue = "beginner") String level) {
        return paperService.getLearningResources(level);
    }

    /**
     * 健康检查接口
     * 用于检查服务状态
     *
     * @return 服务状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("LLM论文研究助手服务运行正常", "健康检查通过")
        );
    }

}