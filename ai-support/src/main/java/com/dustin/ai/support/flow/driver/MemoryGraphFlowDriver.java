package com.dustin.ai.support.flow.driver;

import com.dustin.ai.support.flow.ToolWorkflowGraph;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版任务图驱动
 */
public class MemoryGraphFlowDriver implements GraphFlowDriver {
    /**
     * 内存存储graph 实例
     */
    private final Map<String, ToolWorkflowGraph> graphTasks = new ConcurrentHashMap<>();

    /**
     * 内存存储任务失败的实例
     */
    private final  Map<String,ToolWorkflowGraph> failedTasks = new ConcurrentHashMap<>();

    /**
     * 内存存储任务成功的实例
     */
    private final Map<String,ToolWorkflowGraph> completedTasks = new ConcurrentHashMap<>();

    /**
     * 启动一个任务图
     * @param graph 任务图
     * @return 任务实例taskId
     */
    @Override
    public String run(ToolWorkflowGraph graph) {
        if(null == graph) {
            throw new IllegalArgumentException("graph is null");
        }
        String workflowId = graph.getWorkflowId();
        String taskId = UUID.randomUUID().toString().replaceAll("-", "");
        addTask(taskId,graph);
        return taskId;
    }

    /**
     * 新增一个任务图
     * @param taskId 任务实例id
     * @param graph 任务图
     */
    @Override
    public void addTask(String taskId,ToolWorkflowGraph graph) {
        if(null == graph) {
            throw new IllegalArgumentException("graph is null");
        }
        graphTasks.put(taskId, graph);
    }

        /**
         * 新增一个成功的任务图
         * @param taskId 任务实例id
         * @param graph 任务图
         */
        @Override
        public void addSuccessTask(String taskId,ToolWorkflowGraph graph) {
            if(null == graph) {
                throw new IllegalArgumentException("graph is null");
            }
            completedTasks.put(taskId, graph);
        }

        /**
         * 新增一个失败的任务图
         * @param taskId 任务实例id
         * @param graph 任务图
         */
        @Override
        public void addFailedTask(String taskId,ToolWorkflowGraph graph) {
            if(null == graph) {
                throw new IllegalArgumentException("graph is null");
            }
            failedTasks.put(taskId, graph);
        }


        /**
         * 获取一个任务图
         * @param taskId 任务实例id
         * @return 任务图
         */
        @Override
        public ToolWorkflowGraph getTask(String taskId) {
            if(null == taskId || taskId.isEmpty()) {
                throw new IllegalArgumentException("taskId is null");
            }
            return graphTasks.get(taskId);
        }

        /**
         * 判断一个任务是否成功
         * @param taskId 任务实例id
         * @return 是否成功
         */
        @Override
        public boolean isSuccess(String taskId) {
            if(null == taskId || taskId.isEmpty()) {
                throw new IllegalArgumentException("taskId is null");
            }
            return completedTasks.containsKey(taskId);
        }

        /**
         * 判断一个任务是否失败
         * @param taskId 任务实例id
         * @return 是否失败
         */
        @Override
        public boolean isFailed(String taskId) {
            if(null == taskId || taskId.isEmpty()) {
                throw new IllegalArgumentException("taskId is null");
            }
            return failedTasks.containsKey(taskId);
        }

        /**
         * 重置一个任务图
         * @param taskId 任务实例id
         */
        @Override
        public void resetTask(String taskId) {
            if(null == taskId || taskId.isEmpty()) {
                throw new IllegalArgumentException("taskId is null");
            }
            completedTasks.remove(taskId);
            failedTasks.remove(taskId);
        }

        /**
         * 清空所有任务图
         */
        @Override
        public void clear() {
            graphTasks.clear();
            completedTasks.clear();
            failedTasks.clear();
        }
}
