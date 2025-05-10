package com.anmory.teachagent.service;

import com.anmory.teachagent.mapper.MaterialMapper;
import com.anmory.teachagent.model.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-08 上午9:59
 */

@Service
public class MaterialService {
    @Autowired
    MaterialMapper materialMapper;

    public int insert(String title, String filePath, String materialType) {
        return materialMapper.insert(title, filePath, materialType);
    }

    public List<Material> selectAll() {
        return materialMapper.selectAll();
    }
}
