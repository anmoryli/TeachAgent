package com.anmory.teachagent.mapper;

import com.anmory.teachagent.model.Material;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:55
 */


@Mapper
public interface MaterialMapper {
    @Select("select * from Material")
    List<Material> selectAll();

    @Insert("insert into Material (title, file_path, material_type) values (#{title}, #{filePath}, #{materialType})")
    int insert(String title, String filePath, String materialType);

    @Delete("delete from Material where material_id = #{id}")
    int deleteById(int id);
}
