package com.dustin.ai.support.flow;


import java.util.*;

/**
 * 图流上下文
 */

public class GraphFlowContext {

    /**
     * 图单例
     */
    private static volatile GraphFlowContext instance;

    public GraphFlowContext() {
    }

    public static GraphFlowContext getInstance(){
        if(null == instance){
            synchronized (GraphFlowContext.class){
                if(null == instance){
                    instance = new GraphFlowContext();
                }
            }
        }
        return instance;
    }

    /**
     * 图流上下文，用户存储系统中所有的任务图
     */
    Map<String,ToolWorkflowGraph> flowContext = new HashMap<>();

    /**
     * 不允许出现重复的任务图
     */
    List<ToolWorkflowGraph> graphList = new LinkedList<>();

    /**
     * 新增一个任务图
     * @param graph
     */
    public void addToolGraph(ToolWorkflowGraph graph){
        if(graphList.contains(graph)){
            throw new IllegalArgumentException("图流上下文已经存在该任务图");
        }
        graphList.add(graph);
        flowContext.put(graph.getWorkflowId(),graph);
    }

     /**
     * 获取一个任务图
     * @param workflowId
     * @return
     */
    public ToolWorkflowGraph getToolGraph(String workflowId){
        return flowContext.get(workflowId);
    }

     /**
     * 获取所有的任务图
     * @return
     */
    public List<ToolWorkflowGraph> getAllGraph(){
        return graphList;
    }

    /**
     * 清空所有任务图
     */
    public void clear(){
        flowContext.clear();
        graphList.clear();
    }

    /**
     * 全局添加一个任务图
     * @param graph
     */
    public static void addGraph(ToolWorkflowGraph graph){
        getInstance().addToolGraph(graph);
    }

    /**
     * 获取全局访问的快捷方式
     * @return
     */
    public static Map<String,ToolWorkflowGraph> getFlowContext(){
        return getInstance().flowContext;
    }

     /**
     * 全局获取一个任务图
     * @param workflowId
     * @return
     */
    public static  ToolWorkflowGraph getGraph(String workflowId){
        return getInstance().getToolGraph(workflowId);
    }
}
