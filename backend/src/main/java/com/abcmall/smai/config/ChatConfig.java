package com.abcmall.smai.config;

import com.abcmall.smai.tool.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class ChatConfig {

    /**
     * RAG 文档存放路径
     */
    @Value("${abcmall.smai.rag-docs-path:classpath:rag-docs}")
    private String ragDocsPath;

    /**
     * RAG 文档缓存文件名
     */
    @Value("${abcmall.smai.rag-cache-file:rag-vector-cache.json}")
    private String ragCacheFile;

    // JSON 序列化器
    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * LLM 大模型客户端
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, DateTimeTool dateTimeTool, SearchProductTool searchProductTool, SerachOrderTool searchOrderTool, SearchSitemapTool searchSitemapTool, SearchPolicyTool searchPolicyTool) {
        // 创建 LLM 大模型客户端实例，并注册工具函数
        return ChatClient.builder(chatModel)
                .defaultTools(searchProductTool, dateTimeTool, searchOrderTool, searchSitemapTool, searchPolicyTool)
                .build();
    }

    /**
     * 内存向量存储
     */
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 应用启动时加载 RAG 知识库
     */
    @Bean
    public ApplicationRunner ragDocsInitializer(SimpleVectorStore vectorStore, ResourcePatternResolver resolver) {
        return args -> {

            // 0. 有缓存 → 直接加载，不管 rag-docs 有没有变
            File cacheFile = new File(ragCacheFile);
            if (cacheFile.exists()) {
                vectorStore.load(cacheFile);
                log.info("RAG 知识库从缓存加载完成: {}", cacheFile.getAbsolutePath());
                return;
            }

            // 1. 扫描 rag-docs 下所有 .txt/.md 文件
            List<Resource> resources = new ArrayList<>();
            resources.addAll(List.of(resolver.getResources(ragDocsPath + "/**/*.txt")));
            resources.addAll(List.of(resolver.getResources(ragDocsPath + "/**/*.md")));
            if (resources.isEmpty()) {
                log.warn("RAG 知识库目录 [{}] 下未找到 .txt/.md 文档", ragDocsPath);
                return;
            }

            // 2. 读取文档，并添加文件名元数据
            List<Document> allDocs = new ArrayList<>();
            for (Resource res : resources) {
                Document doc = new TextReader(res).get().get(0);      // 文档读取器
                doc.getMetadata().put("filename", res.getFilename()); // 将文件名加入到元数据
                allDocs.add(doc);
                log.info("已加载知识库文档: {}", res.getFilename());
            }

            // 3. 切片 + 向量化
            try {
                TokenTextSplitter splitter = new TokenTextSplitter(); // 文本切片器
                List<Document> chunks = splitter.apply(allDocs);      // 切片文档
                // 正式向 Embedding模型 发起请求，将切片向量化后的保存到内存
                vectorStore.add(chunks);
                // 保存到缓存
                vectorStore.save(cacheFile);
                log.info("RAG 知识库向量化完成：原始文档 {} 篇，切片后 {} 段", allDocs.size(), chunks.size());
            } catch (Exception e) {
                log.warn("RAG 向量化失败（请检查 Embedding API Key 配置）: {}", e.getMessage());
            }
        };
    }


    // 系统提示词
    public String SYSTEM_PROMPT = """
        你是 AbcMall 商城的 AI 客服助手，名叫小艾。

        当前登陆用户ID是 %d，昵称是 %s。若为 null，则表示未登录。
        若用户已登录，你应亲切的称用户的昵称。
        若用户未登录，你应亲称用户为"用户"。

        你拥有以下工具，请根据用户的问题自主决定调用哪些工具、调用几次、如何组合：
        1. searchCategory —— 搜索商品分类（用户提到品类时先查分类，拿到 categoryId）
        2. searchProducts —— 多维度商品搜索（分类、关键词、品牌、价格、规格、标签）
        3. queryInProgressOrders —— 查正在进行中的订单
        4. queryHistoricalOrders —— 查历史订单
        5. searchSitemap —— 搜索功能页面导航
        6. searchPolicyDocs —— 检索政策知识库（RAG）

        工作原则：
        - 商品推荐：先调 searchCategory 找分类，再调 searchProducts 按分类查。如果分类不够50条，再调 searchProducts 用扩展关键词补查。从结果中选 Top 3 推荐给用户。
        - 关键词扩展：用户说"手机"时，你应该把 keyword 设为"手机"，expandedKeywords 设为 ["小米","华为","苹果","OPPO","vivo","荣耀","三星","一加"] 等品牌词，因为数据库里商品名可能没有"手机"二字。
        - 订单查询：仅此功能需要先登录，再查询。正在进行中的订单调 queryInProgressOrders，历史订单调 queryHistoricalOrders。
        - 政策咨询：调 searchPolicyDocs 检索知识库，回答中引用来源文件名。
        - 功能导航：调 searchSitemap 找到对应链接发给用户。
        - 闲聊：不需要调用任何工具，直接回答。

        回复格式要求：
        - 回复必须是 JSON，格式为：{"content": "回复文本", "type": "text|products|orders|navigation", "items": [...]}
        - content 的内容如果有双引号，要注意使用转义字符，例如：" 改为 \\"
        - type=products 时，items 是 [{id, name, subtitle, price, mainImage, url}] 格式的 Top 3 商品
        - type=orders 时，items 是 [{orderNo, statusText, totalAmount, payAmount, createTime, url, items:[{productName, image, quantity, price}]}]
          - url 为 "/order/:id"，:id 为订单 orderId，而不是 orderNo
        - type=navigation 时，items 是 [{name, url, description}]
        - type=text 时，items 为 null
        - 只返回 JSON，不要有任何其他文字
        """;

}
