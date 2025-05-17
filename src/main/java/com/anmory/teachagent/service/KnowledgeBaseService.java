package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.KnowledgeDocumentMapper;
import com.anmory.teachagent.model.KnowledgeDocument;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.datatype.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库服务
 * 负责管理知识库文档和向量检索
 */
@Service
@Slf4j
public class KnowledgeBaseService {
    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    
    @Value("${knowledge-base.base-path}")
    private String basePath;
    
    @Value("${knowledge-base.course-materials}")
    private String courseMaterialsPath;
    
    @Value("${knowledge-base.vector-dimension}")
    private int vectorDimension;
    
    private MilvusServiceClient milvusClient;
    private static final String COLLECTION_NAME = "knowledge_documents";
    
    /**
     * 初始化Milvus客户端和集合
     */
    @PostConstruct
    public void init() {
        try {
            ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost("localhost")
                .withPort(19530)
                .build();
            milvusClient = new MilvusServiceClient(connectParam);
            
            createCollectionIfNotExists();
            
            log.info("Milvus客户端初始化成功");
        } catch (Exception e) {
            log.error("Milvus客户端初始化失败", e);
        }
    }
    
    /**
     * 创建Milvus集合（如果不存在）
     */
    private void createCollectionIfNotExists() {
        try {
            FieldType idField = FieldType.newBuilder()
                .withName("document_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
            
            FieldType vectorField = FieldType.newBuilder()
                .withName("vector")
                .withDataType(DataType.FloatVector)
                .withDimension(vectorDimension)
                .build();
            
            FieldType courseIdField = FieldType.newBuilder()
                .withName("course_id")
                .withDataType(DataType.Int64)
                .build();
            
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldTypes(Arrays.asList(idField, vectorField, courseIdField))
                .build();
            
            milvusClient.createCollection(createCollectionParam);
            
            CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("vector")
                .withIndexType(io.milvus.param.IndexType.IVF_FLAT)
                .withMetricType(io.milvus.param.MetricType.L2)
                .withExtraParam("{\"nlist\":1024}")
                .build();
            
            milvusClient.createIndex(createIndexParam);
            
            log.info("Milvus集合和索引创建成功");
        } catch (Exception e) {
            log.error("Milvus集合创建失败", e);
        }
    }
    
    /**
     * 导入知识库文档
     * @param courseId 课程ID
     * @throws IOException 文件读取异常
     */
    public void importDocuments(int courseId) throws IOException {
        File directory = new File(courseMaterialsPath);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    String content;
                    String fileName = file.getName();
                    
                    if (fileName.toLowerCase().endsWith(".pdf")) {
                        content = extractTextFromPdf(file);
                    } 
                    else if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                        content = new String(Files.readAllBytes(file.toPath()));
                    } else {
                        log.info("跳过不支持的文件类型: {}", fileName);
                        continue;
                    }
                    
                    KnowledgeDocument document = new KnowledgeDocument();
                    document.setTitle(fileName);
                    document.setContent(content);
                    document.setFilePath(file.getAbsolutePath());
                    document.setCourseId(courseId);
                    
                    if (fileName.contains("-")) {
                        document.setChapterSection(fileName.substring(0, fileName.indexOf("-")));
                    }
                    
                    document.setVectorEmbedding(generateEmbedding(content));
                    
                    knowledgeDocumentMapper.insert(document);
                    
                    log.info("导入文档: {}", fileName);
                }
            }
        } else {
            log.error("课程材料目录不存在: {}", courseMaterialsPath);
            throw new IOException("课程材料目录不存在: " + courseMaterialsPath);
        }
    }
    
    /**
     * 从PDF文件中提取文本
     * @param file PDF文件
     * @return 提取的文本内容
     */
    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * 生成文本嵌入向量
     * 当前使用随机向量作为占位符，将由另一团队成员提供实际实现
     * 
     * @param text 文本内容
     * @return 嵌入向量
     */
    private byte[] generateEmbedding(String text) {
        log.info("生成文本嵌入向量（占位实现）：文本长度 = {}", text.length());
        byte[] embedding = new byte[vectorDimension * 4]; // 每个float占4字节
        new Random().nextBytes(embedding);
        return embedding;
    }
    
    /**
     * 根据查询检索相关文档
     * @param query 查询文本
     * @param courseId 课程ID
     * @param limit 返回结果数量限制
     * @return 相关文档列表
     */
    public List<KnowledgeDocument> searchRelevantDocuments(String query, int courseId, int limit) {
        try {
            byte[] queryEmbedding = generateEmbedding(query);
            
            return knowledgeDocumentMapper.selectByCourseId(courseId).stream()
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("检索相关文档失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取文档内容
     * @param documentId 文档ID
     * @return 文档内容
     */
    public String getDocumentContent(int documentId) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectByDocumentId(documentId);
        return document != null ? document.getContent() : "";
    }
    
    /**
     * 为AI服务准备知识库上下文
     * 根据查询从知识库中检索相关内容，并格式化为可用于增强AI提示的文本
     * 
     * @param query 查询文本
     * @param courseId 课程ID
     * @param limit 返回结果数量限制
     * @return 格式化的知识库上下文
     */
    public String getKnowledgeContext(String query, int courseId, int limit) {
        List<KnowledgeDocument> relevantDocs = searchRelevantDocuments(query, courseId, limit);
        if (relevantDocs.isEmpty()) {
            return "";
        }
        
        return relevantDocs.stream()
            .map(doc -> "### " + doc.getTitle() + "\n" + doc.getContent())
            .collect(Collectors.joining("\n\n"));
    }
}
