package com.anmory.teachagent.model;

import lombok.Data;

import java.util.Date;

/**
 * 知识库文档模型
 * 用于存储和管理课程知识库的文档内容
 */
@Data
public class KnowledgeDocument {
    private int documentId;
    private String title;
    private String content;
    private String filePath;
    private String chapterSection;
    private String keywords;
    private int courseId;
    private byte[] vectorEmbedding;
    private Date createTime;
    private Date updatedTime;
}
