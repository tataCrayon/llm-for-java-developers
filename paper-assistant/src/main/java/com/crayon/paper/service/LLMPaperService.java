package com.crayon.paper.service;

import com.crayon.paper.agent.LLMPaperAgentService;
import com.crayon.paper.model.ApiResponse;
import com.crayon.paper.model.dto.LearningPlanRequest;
import com.crayon.paper.model.dto.PaperAnalysisRequest;
import com.crayon.paper.model.dto.PaperComparisonRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * LLM论文研究服务类
 * 封装论文搜索、分析、对比、学习计划等业务逻辑。
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LLMPaperService {


    private final LLMPaperAgentService agentService;

    public ResponseEntity<ApiResponse<String>> searchPapers(String query) {
        try {
            String result = agentService.searchAndAnalyzePapers(query);

            return ResponseEntity.ok(
                    ApiResponse.success(result, "论文搜索完成")
            );

        } catch (Exception e) {
            log.error("搜索论文时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("搜索失败: " + e.getMessage())
            );
        }
    }

    public ResponseEntity<ApiResponse<String>> analyzePaper(PaperAnalysisRequest request) {
        log.info("收到论文深度分析请求: {}", request.getTitle());
        try {
            String paperInfo = formatPaperInfo(request);
            String result = agentService.analyzePaperInDepth(paperInfo);

            return ResponseEntity.ok(
                    ApiResponse.success(result, "论文分析完成")
            );

        } catch (Exception e) {
            log.error("分析论文时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("分析失败: " + e.getMessage())
            );
        }
    }

    public ResponseEntity<ApiResponse<String>> comparePapers(PaperComparisonRequest request) {

        log.info("收到论文对比请求，论文数量: {}", request.getPapers().size());

        try {
            String papersInfo = formatPapersForComparison(request);
            String result = agentService.comparePapers(papersInfo);

            return ResponseEntity.ok(
                    ApiResponse.success(result, "论文对比完成")
            );

        } catch (Exception e) {
            log.error("对比论文时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("对比失败: " + e.getMessage())
            );
        }
    }

    public ResponseEntity<ApiResponse<String>> generateLearningPlan(LearningPlanRequest request) {

        log.info("收到学习计划生成请求，用户背景: {}", request.getUserBackground());

        try {
            String result = agentService.generatePersonalizedLearningPlan(
                    request.getUserBackground(),
                    request.getLearningGoals()
            );

            return ResponseEntity.ok(
                    ApiResponse.success(result, "学习计划生成完成")
            );

        } catch (Exception e) {
            log.error("生成学习计划时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("生成失败: " + e.getMessage())
            );
        }
    }

    public ResponseEntity<ApiResponse<String>> getHotTopics() {

        log.info("获取热门研究方向");

        try {
            String result = agentService.getHotResearchTopics();

            return ResponseEntity.ok(
                    ApiResponse.success(result, "热门方向获取成功")
            );

        } catch (Exception e) {
            log.error("获取热门方向时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("获取失败: " + e.getMessage())
            );
        }
    }

    public ResponseEntity<ApiResponse<String>> getLearningResources(String level) {

        log.info("获取学习资源推荐，水平: {}", level);

        try {
            String result = agentService.recommendLearningResources(level);

            return ResponseEntity.ok(
                    ApiResponse.success(result, "资源推荐获取成功")
            );

        } catch (Exception e) {
            log.error("获取学习资源时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("获取失败: " + e.getMessage())
            );
        }
    }


    /**
     * 格式化论文信息用于分析
     *
     * @param request 论文分析请求
     * @return 格式化的论文信息字符串
     */
    private String formatPaperInfo(PaperAnalysisRequest request) {
        StringBuilder info = new StringBuilder();
        info.append("论文标题: ").append(request.getTitle()).append("\n");

        if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
            info.append("作者: ").append(String.join(", ", request.getAuthors())).append("\n");
        }

        if (request.getAbstractText() != null && !request.getAbstractText().isEmpty()) {
            info.append("摘要: ").append(request.getAbstractText()).append("\n");
        }

        if (request.getUrl() != null && !request.getUrl().isEmpty()) {
            info.append("链接: ").append(request.getUrl()).append("\n");
        }

        if (request.getPublishedDate() != null && !request.getPublishedDate().isEmpty()) {
            info.append("发布日期: ").append(request.getPublishedDate()).append("\n");
        }

        return info.toString();
    }

    /**
     * 格式化多篇论文信息用于比较
     *
     * @param request 论文比较请求
     * @return 格式化的论文比较信息
     */
    private String formatPapersForComparison(PaperComparisonRequest request) {
        StringBuilder info = new StringBuilder();
        info.append("要比较的论文列表:\n\n");

        for (int i = 0; i < request.getPapers().size(); i++) {
            PaperAnalysisRequest paper = request.getPapers().get(i);
            info.append(String.format("=== 论文 %d ===\n", i + 1));
            info.append(formatPaperInfo(paper));
            info.append("\n");
        }

        if (request.getComparisonAspects() != null && !request.getComparisonAspects().isEmpty()) {
            info.append("重点比较方面: ").append(String.join(", ", request.getComparisonAspects()));
        }

        return info.toString();
    }
}
