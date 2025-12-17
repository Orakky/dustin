package com.dustin.ai.support.generator.mermaid;


import java.util.ArrayList;
import java.util.List;

/**
 * mermaid gantt section
 */
public class GanttSection {

    private String name;
    private List<GanttTask> tasks = new ArrayList<>();


    public GanttSection(String name, List<GanttTask> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public GanttSection() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GanttTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<GanttTask> tasks) {
        this.tasks = tasks;
    }
}
