package com.dustin.ai.support.flow;

import com.dustin.ai.support.flow.driver.GraphFlowDriver;
import org.noear.solon.core.util.ResourceUtil;

/**
 * 图流引擎
 */
public interface GraphFlowEngine {


    /**
     * 默认加载方式
     * @param graphUri
     */
    default void load(String graphUri){
        if(graphUri.contains("*")){
            for (String u1 : ResourceUtil.scanResources(graphUri)) {
                this.load(ToolWorkflowGraph.fromUri(u1));
            }
        }else {
            this.load(ToolWorkflowGraph.fromUri(graphUri));
        }
    }

    /**
     * 加载图
     * @param graph
     */
    void load(ToolWorkflowGraph graph);
    /**
     * 卸载图
     * @param workflowId
     */
    void unload(String workflowId);
    /**
     * 根据workflowId 运行
     * @param workflowId
     */
    String run(String workflowId);

    /**
     * 直接运行graph
     * @param graph
     * @return
     */
    String run(ToolWorkflowGraph graph);

    /**
     * 获取驱动
     * @return 驱动
     */
    GraphFlowDriver getDriver();

}
