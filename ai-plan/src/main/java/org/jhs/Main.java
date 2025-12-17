package org.jhs;

import org.jhs.schedule2.SchedulingAlgorithm;
import org.jhs.schedule2.Segment;
import org.jhs.schedule2.SegmentSchedule;

import java.util.ArrayList;
import java.util.List;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {

    public static void main(String[] args) {
        // 创建一些测试分段（参数：ID, 面积, 天数, 最早开始日, 最晚结束日）
        // 注意：日期使用1-based索引
        List<Segment> segments = new ArrayList<>();
//        segments.add(new Segment("101", 2, 4, 1, 5));   // 最早开始日1，最晚结束日5
//        segments.add(new Segment("102", 3, 3, 2, 6));   // 最早开始日2，最晚结束日6
//        segments.add(new Segment("103", 1, 2, 1, 3));   // 最早开始日1，最晚结束日3
//        segments.add(new Segment("104", 2, 2, 3, 7));   // 最早开始日3，最晚结束日7
//        segments.add(new Segment("105", 4, 2, 5, 7));   // 最早开始日5，最晚结束日7
//        segments.add(new Segment("106", 1, 5, 1, 6));   // 最早开始日1，最晚结束日6
//        segments.add(new Segment("107", 2, 3, 4, 7));   // 最早开始日4，最晚结束日7
//
//        // 总面数8，总天数7
//        int totalAreas = 8;
//        int totalDays = 7;

        segments.add(new Segment("N1187528", 3, 13, -58, 16));
        segments.add(new Segment("N1187538", 3, 13, -58, 17));
        segments.add(new Segment("N1187529", 3, 13, -52, 18));
        segments.add(new Segment("N1187539", 3, 13, -52, 18));
        segments.add(new Segment("N1227109", 1, 30, -30, 53));
        segments.add(new Segment("N1188525", 2, 15, -22, 42));
        segments.add(new Segment("N1188535", 2, 15, -22, 44));
        segments.add(new Segment("N1188537", 2, 15, -17, 47));
        segments.add(new Segment("N1227674", 3, 16, 26, 48));
        segments.add(new Segment("N1227683", 2, 17, 29, 50));
        segments.add(new Segment("N1227693", 2, 17, 29, 50));
        segments.add(new Segment("N1188541", 2, 16, -16, 53));
        segments.add(new Segment("N1188551", 2, 16, -16, 53));
        segments.add(new Segment("N1188528", 2, 13, -3, 52));
        segments.add(new Segment("N1227664", 2, 16, 26, 55));
        segments.add(new Segment("N1188538", 2, 13, -3, 53));
        segments.add(new Segment("N1188529", 2, 12, -1, 54));
        segments.add(new Segment("N1188539", 2, 12, -1, 54));
        segments.add(new Segment("N1206525", 2, 14, 22, 77));
        segments.add(new Segment("N1206535", 2, 14, 22, 78));
        segments.add(new Segment("N1206526", 2, 11, 26, 78));
        segments.add(new Segment("N1206536", 2, 11, 27, 79));
        segments.add(new Segment("N1206529", 2, 13, 33, 89));
        segments.add(new Segment("N1206539", 2, 13, 33, 89));
        segments.add(new Segment("N1193523", 2, 15, 32, 105));
        segments.add(new Segment("N1193533", 2, 15, 32, 105));
        segments.add(new Segment("N1193525", 2, 15, 33, 111));
        segments.add(new Segment("N1193535", 2, 15, 33, 113));
        segments.add(new Segment("N1193527", 2, 15, 40, 115));
        segments.add(new Segment("N1193537", 2, 15, 40, 116));
        segments.add(new Segment("N1228133", 2, 30, 75, 131));
        segments.add(new Segment("N1228123", 2, 30, 75, 135));
        segments.add(new Segment("N1193528", 2, 13, 64, 121));
        segments.add(new Segment("N1193538", 2, 13, 64, 122));
        segments.add(new Segment("N1194523", 2, 15, 61, 134));
        segments.add(new Segment("N1194533", 2, 15, 61, 134));
        segments.add(new Segment("ZSKY1801", 2, 25, 89, 148));
        segments.add(new Segment("N1194524", 2, 13, 75, 138));
        segments.add(new Segment("N1194525", 2, 15, 75, 140));
        segments.add(new Segment("N1194535", 2, 15, 75, 142));
        segments.add(new Segment("N1194526", 2, 13, 75, 141));
        segments.add(new Segment("N1194536", 2, 13, 75, 143));
        segments.add(new Segment("N1228109", 2, 33, 57, 169));
        segments.add(new Segment("N1194528", 2, 13, 77, 150));
        segments.add(new Segment("N1194538", 2, 13, 77, 151));
        segments.add(new Segment("N1194529", 2, 13, 77, 152));
        segments.add(new Segment("N1194539", 2, 13, 77, 152));

        // 总面数8，总天数7（1-based）
        int totalAreas = 20;
        int totalDays = 90;

        // 执行排程算法
        SchedulingAlgorithm scheduler = new SchedulingAlgorithm(totalAreas, totalDays);
        List<SegmentSchedule> schedule = scheduler.scheduleSegments(segments);

        System.out.println("排程结果：");
        // 打印结果
        System.out.println("共安排了 " + schedule.size() + " 个分段");
        System.out.println("------------------------");
        for (SegmentSchedule sched : schedule) {
            System.out.println(sched.toString());
        }
        // 打印未排程
        scheduler.printNoSchedule(segments, schedule);
        //打印模拟的排程图
        scheduler.printScheduleImage(schedule, totalAreas, totalDays);
    }

//    public static void main(String[] args) {
//        //TIP 当文本光标位于高亮显示的文本处时按 <shortcut actionId="ShowIntentionActions"/>
//        // 查看 IntelliJ IDEA 建议如何修正。
//        System.out.printf("Hello and welcome!");
//
//        for (int i = 1; i <= 5; i++) {
//            //TIP 按 <shortcut actionId="Debug"/> 开始调试代码。我们已经设置了一个 <icon src="AllIcons.Debugger.Db_set_breakpoint"/> 断点
//            // 但您始终可以通过按 <shortcut actionId="ToggleLineBreakpoint"/> 添加更多断点。
//            System.out.println("i = " + i);
//        }
//    }
}