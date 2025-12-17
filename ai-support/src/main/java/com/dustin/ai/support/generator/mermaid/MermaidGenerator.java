package com.dustin.ai.support.generator.mermaid;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * mermaid 生成器
 * @author wangqingsong
 */
@Slf4j
public class MermaidGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * 创建mermaid gantt
     * @param sections
     * @return
     */
    public static String generateGantt(String title,List<GanttSection> sections){
        StringBuilder mdBuilder = new StringBuilder();

        // 添加Mermaid代码块开始
        mdBuilder.append("```mermaid\ngantt\n");

        // 添加标题
        mdBuilder.append("    title ").append(title).append("\n");

        // 设置日期格式
        mdBuilder.append("    dateFormat  YYYY-MM-DD\n");
        mdBuilder.append("    axisFormat  %m/%d\n\n");

        // 添加各个分组和任务
        for (GanttSection section : sections) {
            mdBuilder.append("    section ").append(section.getName()).append("\n");

            for (GanttTask task : section.getTasks()) {
                mdBuilder.append("    ").append(task.getName()).append(" :");

                // 添加状态
                if (task.getStatus() != null && !task.getStatus().isEmpty()) {
                    mdBuilder.append(task.getStatus()).append(", ");
                }

                // 添加任务ID
                if(task.getTaskId()!=null && !task.getTaskId().isEmpty()){
                    mdBuilder.append(task.getTaskId()).append(", ");
                }

                // 添加依赖关系
                if (task.getDependency() != null && !task.getDependency().isEmpty()) {
                    mdBuilder.append("after ").append(task.getDependency()).append(", ");
                }

                // 添加日期和持续时间
                mdBuilder.append(task.getStartDate())
                        .append(", ")
                        .append(task.getDuration()).append("d\n");
            }
            mdBuilder.append("\n");
        }

        // 结束Mermaid代码块
        mdBuilder.append("```");

        return mdBuilder.toString();
    }

    /**
     * 保存文件
     * @param content
     * @param filePath
     * @throws IOException
     */
    public static void saveMarkdownToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }

}
