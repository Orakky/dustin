package com.dustin.ai.support.flow;

import com.dustin.ai.support.flow.driver.GraphFlowDriver;
import com.dustin.ai.support.flow.driver.MemoryGraphFlowDriver;

/**
 * 默认的图流引擎实现类，使用内存图流驱动
 */
public class DefaultGraphFlowEngine implements GraphFlowEngine {


    protected final GraphFlowContext graphFlowContext = GraphFlowContext.getInstance();

    protected  GraphFlowDriver graphFlowDriver;

    public DefaultGraphFlowEngine() {
        this.graphFlowDriver = null;
    }

    public DefaultGraphFlowEngine(GraphFlowDriver graphFlowDriver) {
        this.graphFlowDriver = graphFlowDriver;
    }

    /**
     * 新增一个任务图
     * @param graph
     */
    @Override
    public void load(ToolWorkflowGraph graph) {
        if(null == graph) {
            throw new IllegalArgumentException("graph is null");
        }
        this.graphFlowContext.addToolGraph(graph);
    }

    @Override
    public void unload(String workflowId) {
        if(null == workflowId || workflowId.isEmpty()) {
            throw new IllegalArgumentException("workflowId is null");
        }
        graphFlowContext.removeToolGraph(workflowId);
    }

    /**
     开始启动流程
     * @param workflowId
     */
    @Override
    public String run(String workflowId) {
        if(null == workflowId || workflowId.isEmpty()) {
            throw new IllegalArgumentException("workflowId is null");
        }
        ToolWorkflowGraph graph = graphFlowContext.getToolGraph(workflowId);
        if(null == graph) {
            throw new IllegalArgumentException("workflowId not found");
        }
        if(null == graphFlowDriver) {
            //如果不存在 则默认选择内存图流驱动
            graphFlowDriver = new MemoryGraphFlowDriver();
        }
        String taskId = graphFlowDriver.run(graph);
        if(null == taskId || taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId is null");
        }
        return taskId;
    }

    /**
     * 直接运行graph
     *
     * @param graph
     * @return
     */
    @Override
    public String run(ToolWorkflowGraph graph) {
        if(null == graph) {
            throw new IllegalArgumentException("graph is null");
        }

        load(graph);

        return run(graph.getWorkflowId());
    }

    /**
      * 获取驱动
      * @return 驱动
      */
    @Override
    public GraphFlowDriver getDriver() {
        return graphFlowDriver;
    }
}
