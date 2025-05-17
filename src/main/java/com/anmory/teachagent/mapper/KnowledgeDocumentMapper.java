package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.KnowledgeDocument;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识库文档数据访问接口
 * 提供对知识库文档的CRUD操作
 */
@Mapper
public interface KnowledgeDocumentMapper {
    @Insert("INSERT INTO knowledge_document (title, content, file_path, chapter_section, keywords, course_id, vector_embedding) " +
            "VALUES (#{title}, #{content}, #{filePath}, #{chapterSection}, #{keywords}, #{courseId}, #{vectorEmbedding})")
    @Options(useGeneratedKeys = true, keyProperty = "documentId")
    int insert(KnowledgeDocument document);
    
    @Select("SELECT * FROM knowledge_document WHERE course_id = #{courseId}")
    List<KnowledgeDocument> selectByCourseId(int courseId);
    
    @Select("SELECT * FROM knowledge_document WHERE document_id = #{documentId}")
    KnowledgeDocument selectByDocumentId(int documentId);
    
    @Select("SELECT * FROM knowledge_document WHERE chapter_section LIKE CONCAT('%', #{chapterSection}, '%')")
    List<KnowledgeDocument> selectByChapterSection(String chapterSection);
    
    @Update("UPDATE knowledge_document SET vector_embedding = #{vectorEmbedding} WHERE document_id = #{documentId}")
    int updateVectorEmbedding(int documentId, byte[] vectorEmbedding);
    
    @Delete("DELETE FROM knowledge_document WHERE document_id = #{documentId}")
    int deleteByDocumentId(int documentId);
}
