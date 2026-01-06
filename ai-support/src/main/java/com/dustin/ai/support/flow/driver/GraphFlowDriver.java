package com.dustin.ai.support.flow.driver;

import com.dustin.ai.support.flow.ToolWorkflowGraph;

import java.util.Map;

public interface GraphFlowDriver {

        /**
         * 启动一个任务图
         * @param graph 任务图
         * @return 任务实例
         */
        String run(ToolWorkflowGraph graph);

        /**
         * 新增一个任务图
         * @param taskId 任务实例id
         * @param graph 任务图
         */
        void addTask(String taskId,ToolWorkflowGraph graph);

        /**
         * 新增一个成功的任务图
         * @param taskId 任务实例id
         * @param graph 任务图
         */
        void addSuccessTask(String taskId,ToolWorkflowGraph graph);

        /**
         * 新增一个失败的任务图
         * @param taskId 任务实例id
         * @param graph 任务图
         */
        void addFailedTask(String taskId,ToolWorkflowGraph graph);

        /**
         * 获取一个任务图
         * @param taskId 任务实例id
         * @return 任务图
         */
        ToolWorkflowGraph getTask(String taskId);


        /**
         * 判断一个任务是否成功
         * @param taskId 任务实例id
         * @return 是否成功
         */
        boolean isSuccess(String taskId);


        /**
         * 判断一个任务是否失败
         * @param taskId 任务实例id
         * @return 是否失败
         */
        boolean isFailed(String taskId);

        /**
         * 重置一个任务图
         * @param taskId 任务实例id
         */
        void resetTask(String taskId);


        /**
         * 清空所有任务图
         */
        void clear();

        /**
         * 获取一个任务的执行结果
         * @param taskId 任务实例id
         * @return 执行结果
         */
        Map<String, Object> getExecutionResult(String taskId);

        /**
         * 获取所有任务图
         * @return 所有任务图
         */
        Map<String,ToolWorkflowGraph> getGraphTasks();
}
