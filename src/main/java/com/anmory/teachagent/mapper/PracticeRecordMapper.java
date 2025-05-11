package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.PracticeRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-11 上午10:59
 */

@Mapper
public interface PracticeRecordMapper {
    @Insert("insert into PracticeRecord (student_id, question_id, submitted_answer, is_correct, error_analysis) values " +
            "(#{studentId}, #{questionId}, #{submittedAnswer}, #{isCorrect}, #{errorAnalysis})")
    int insert(int studentId, int questionId, String submittedAnswer, int isCorrect, String errorAnalysis);

    @Select("select * from PracticeRecord")
    List<PracticeRecord> selectAll();

    @Select("select * from PracticeRecord where student_id = #{studentId}")
    List<PracticeRecord> selectByStudentId(int studentId);

    @Select("select * from PracticeRecord where question_id = #{questionId}")
    List<PracticeRecord> selectByQuestionId(int questionId);

    @Select("select * from PracticeRecord where student_id = #{studentId} and question_id = #{questionId}")
    List<PracticeRecord> selectByStudentIdAndQuestionId(int studentId, int questionId);

    @Select("select * from PracticeRecord where student_id = #{studentId} and question_id = #{questionId} order by id desc limit 1")
    PracticeRecord selectLatestRecord(int studentId, int questionId);
}
