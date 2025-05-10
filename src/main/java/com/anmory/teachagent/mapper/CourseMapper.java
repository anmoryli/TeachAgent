package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Anmory/李梦杰
 * @description TODO
 * @date 2025-05-10 上午11:01
 */

@Mapper
public interface CourseMapper {
    @Insert("insert into Course (course_name, discipline, description) values (#{courseName}, #{discipline}, #{description})")
    int insert(String courseName, String discipline, String description);

    @Select("select * from Course")
    List<Course> selectAll();

    @Select("select * from Course where course_name = #{courseName}")
    Course selectByName(String courseName);

    @Delete("delete from Course where course_name = #{courseName}")
    int deleteByName(String courseName);

    @Update("update Course set course_name = #{courseName}, discipline = #{discipline}, description = #{description} where course_id = #{courseId}")
    int update(int courseId, String courseName, String discipline, String description);
}
