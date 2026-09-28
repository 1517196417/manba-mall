package com.abcmall.smai.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 工具调用过程追踪器
 * 支持 SSE 实时推送：每次工具调用时通过回调通知前端
 */
public class StepTracker {

    private static final ThreadLocal<List<AgentStep>> STEPS = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Consumer<AgentStep>> CALLBACK = new ThreadLocal<>();

    // Agent 步骤
    public record AgentStep(String toolName, String description, long timestamp) {}

    // 开始 Agent
    public static void start() {
        STEPS.set(new ArrayList<>());
    }

    // 开始 Agent 并设置回调
    public static void start(Consumer<AgentStep> callback) {
        STEPS.set(new ArrayList<>());
        CALLBACK.set(callback);
    }

    // 添加 Agent 步骤
    public static void addStep(String toolName, String description) {
        AgentStep step = new AgentStep(toolName, description, System.currentTimeMillis());
        STEPS.get().add(step);
        Consumer<AgentStep> cb = CALLBACK.get();
        if (cb != null) {
            try {
                cb.accept(step);
            } catch (Exception ignored) {}
        }
    }

    // 获取 Agent 所有步骤
    public static List<AgentStep> getSteps() {
        return STEPS.get();
    }

    // 清除 Agent 所有步骤和回调
    public static void clear() {
        STEPS.remove();
        CALLBACK.remove();
    }
}