package com.abcmall.smai.tool;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SearchPolicyTool {

    /** 内存向量数据库 */
    @Resource
    SimpleVectorStore vectorStore;

    /**
     * RAG Top-K 参数
     */
    @Value("${abcmall.smai.rag-top-k:5}")
    private int ragTopK;

    @Tool(description = "检索商城政策知识库（RAG向量检索）。用户询问退货政策、运费规则、保修条款、发票政策、积分奖励、支付方式等商城政策时调用。返回相关的政策文档片段和来源文件名。")
    public List<Document> searchPolicyDocs(String query) {
        if (query == null || query.isEmpty()) return Collections.emptyList();
        StepTracker.addStep("searchPolicyDocs", "正在检索政策知识库【" + query + "】...");
        try {
            List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(ragTopK).build());
            if (docs == null || docs.isEmpty()) {
                StepTracker.addStep("searchPolicyDocs", "知识库中未找到相关内容");
                return Collections.emptyList();
            }
            StepTracker.addStep("searchPolicyDocs", "检索到 " + docs.size() + " 条相关文档片段");
            return docs;
        } catch (Exception e) {
            log.warn("RAG检索失败: {}", e.getMessage());
            StepTracker.addStep("searchPolicyDocs", "知识库检索失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}