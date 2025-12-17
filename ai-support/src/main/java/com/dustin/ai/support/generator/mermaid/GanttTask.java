package com.dustin.ai.support.generator.mermaid;

/**
 * mermaid gantt task
 * @author wangqingsong
 */
public class GanttTask {

    private String name;
    private String taskId;
    private String startDate;
    private int duration;//单位：天
    private String dependency;//依赖的任务ID 可选
    private String status;//状态, done, active, 默认为空


    public GanttTask() {
    }

    public GanttTask(String name, String taskId, String startDate, int duration, String dependency, String status) {
        this.name = name;
        this.taskId = taskId;
        this.startDate = startDate;
        this.duration = duration;
        this.dependency = dependency;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
