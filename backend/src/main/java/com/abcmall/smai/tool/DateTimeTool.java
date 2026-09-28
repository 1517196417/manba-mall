package com.abcmall.smai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTool {
    @Tool(description = "获取当前系统时间，返回格式为 yyyy-MM-dd HH:mm:ss")
    public String getCurrentDateTime() {
        System.out.println("【工具调用 getCurrentDateTime】");
        StepTracker.addStep("getCurrentDateTime", "正在获取当前系统时间...");
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}