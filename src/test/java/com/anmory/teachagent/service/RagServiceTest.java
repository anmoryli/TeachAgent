package com.anmory.teachagent.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CrossOrigin
class RagServiceTest {
    @Autowired
    RagService ragService;

    @Test
    void getRelevant() throws IOException {
        System.out.println(ragService.getRelevant("TensorFlow.js 环境配置"));
    }
}