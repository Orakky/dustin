package org.jhs.schedule2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulingAlgorithm {

    /**
     * 总面数
     */
    private int totalAreas = 1;

    /**
     * 总天数
     */
    private int totalDays = 1;

    /**
     * 优先安排的日期约束窗口大小
     */
    private int timeWindowSize = 10;

    /**
     * 一个二维数组表示资源占用情况，true表示已占用
     */
    private boolean[][] resourceGrid = null;

    public SchedulingAlgorithm(int totalAreas, int totalDays) {
        this(totalAreas, totalDays, 10);
    }

    public SchedulingAlgorithm(int totalAreas, int totalDays, int timeWindowSize) {
        this.totalAreas = totalAreas;
        this.totalDays = totalDays;
        this.timeWindowSize = timeWindowSize;
        this.resourceGrid = new boolean[totalDays][totalAreas];
    }

    // 尝试安排所有可能的分段，返回最优排程方案
    public List<SegmentSchedule> scheduleSegments(List<Segment> segments) {

        List<SegmentSchedule> schedule = new ArrayList<>();

        // 过滤掉时间上完全不可行的分段
        List<Segment> feasibleSegments = new ArrayList<>();
        for (Segment seg : segments) {
            // 检查时间可行性和大小可行性
            if (seg.isTimeFeasible() &&
                    seg.getArea() <= totalAreas &&
                    seg.getDays() <= totalDays) {
                feasibleSegments.add(seg);
            }
        }

        // 排序策略：优先安排时间约束紧,时间窗口在10天之内的
        List<Segment> shortTimeWindowSegments = feasibleSegments.stream().filter(
                s -> (s.getLatestPossibleStart() - Math.max(0, s.getEarliestPossibleStart()) <= timeWindowSize))
                .collect(Collectors.toList());
        shortTimeWindowSegments.sort((s1, s2) -> {
            // 1. 优先考虑时间窗口小的分段（约束更紧）
            int window1 = s1.getLatestPossibleStart() - Math.max(0, s1.getEarliestPossibleStart());
            int window2 = s2.getLatestPossibleStart() - Math.max(0, s2.getEarliestPossibleStart());
            if (window1 != window2) {
                return Integer.compare(window1, window2);
            }
            // 2. 考虑天数少的分段
            return Integer.compare(s1.getDays(), s2.getDays());
        });
        scheduleSegments(schedule, shortTimeWindowSegments);

        List<Segment> otherSegments = null;
        if (schedule.size() > 0) {
            // 排除已经计划的分段
            List<Segment> scheduleSegment = schedule.stream().map(s -> s.getSegment()).collect(Collectors.toList());
            otherSegments = feasibleSegments.stream().filter(s -> !scheduleSegment.contains(s)).collect(Collectors.toList());
        } else {
            otherSegments = feasibleSegments;
        }

        // 排序策略：优先安排资源占用多的分段
        otherSegments.sort((s1, s2) -> {
            // 2. 然后考虑面日数较大的分段
            int areaDays1 = s1.getArea() * s1.getDays();
            int areaDays2 = s2.getArea() * s2.getDays();
            if (areaDays1 != areaDays2) {
                return Integer.compare(areaDays2, areaDays1);
            }
            // 3. 最后考虑面数多的分段
            return Integer.compare(s2.getArea(), s1.getArea());
        });
        scheduleSegments(schedule, otherSegments);
        return schedule;
    }

    private void scheduleSegments(List<SegmentSchedule> schedule, List<Segment> feasibleSegments) {
        // 尝试安排每个分段
        for (Segment segment : feasibleSegments) {
            // 确定该分段可能的开始日期范围
            int startDayMin = segment.getEarliestPossibleStart();
            int startDayMax = segment.getLatestPossibleStart();

            // 确保不超出总天数限制
            startDayMin = Math.max(startDayMin, 0);
            startDayMax = Math.min(startDayMax, totalDays - segment.getDays());

            if (startDayMin > startDayMax) {
                continue; // 没有可用的时间窗口
            }

            // 尝试找到合适的位置
            boolean placed = false;
            // 在允许的日期范围内寻找
            for (int startDay = startDayMin; startDay <= startDayMax; startDay++) {
                // 遍历所有可能的起始面
                for (int startArea = 0; startArea <= totalAreas - segment.getArea(); startArea++) {
                    // 检查这个位置是否可用
                    boolean available = true;
                    outerLoop:
                    for (int d = 0; d < segment.getDays(); d++) {
                        for (int a = 0; a < segment.getArea(); a++) {
                            if (resourceGrid[startDay + d][startArea + a]) {
                                available = false;
                                break outerLoop;
                            }
                        }
                    }

                    // 如果可用，则安排这个分段
                    if (available) {
                        // 标记资源为已占用
                        for (int d = 0; d < segment.getDays(); d++) {
                            for (int a = 0; a < segment.getArea(); a++) {
                                resourceGrid[startDay + d][startArea + a] = true;
                            }
                        }

                        // 添加到排程
                        schedule.add(new SegmentSchedule(segment, startDay, startArea));
                        placed = true;
                        break;
                    }
                }
                if (placed) {
                    break;
                }
            }
        }
    }

    // 打印排程结果
    public void printSchedule(List<SegmentSchedule> schedule) {
        System.out.println("共安排了 " + schedule.size() + " 个分段");
        System.out.println("------------------------");
        for (SegmentSchedule sched : schedule) {
            System.out.println(sched.toString());
        }
    }

    // 打印排程结果
    public void printNoSchedule(List<Segment> segments, List<SegmentSchedule> schedule) {
        System.out.println("未排程分段");
        System.out.println("------------------------");
        for (Segment segment : segments) {
            SegmentSchedule sched = schedule.stream().filter(s -> segment.getId().equals(s.getSegment().getId())).findFirst().orElse(null);
            if (sched == null) {
                System.out.println(segment.toString());
            }
        }
    }

    public void printScheduleImage(List<SegmentSchedule> schedule, int totalAreas, int totalDays) {
        System.out.println("\n资源占用情况：");
        System.out.println("(行：面，列：日期（base计划开始日期），每个字符代表一个分段)");

        // 创建列标题
        System.out.print("    ");
        for (int a = 0; a < totalDays; a++) {
            System.out.printf("%3d", a + 1);
        }
        System.out.println();

        // 创建一个网格表示资源占用
        String[][] grid = new String[totalAreas][totalDays];
        for (int i = 0; i < totalAreas; i++) {
            Arrays.fill(grid[i], "   ");
        }

        // 标记已占用的资源
        for (SegmentSchedule sched : schedule) {
            Segment seg = sched.getSegment();
            // 用分段ID的最后一个字符作为标记
            String c = seg.getId().substring(seg.getId().length() - 2, seg.getId().length());
            for (int a = sched.getStartArea(); a <= sched.getEndArea(); a++) {
                for (int d = sched.getStartDay(); d <= sched.getEndDay(); d++) {
                    // 如果超出最晚结束日期，用小写字母标记
                    if (!sched.isCompletedBeforeLatestEnd()) {
                        c = c + "!";
                    }
                    grid[a][d] = c;
                }
            }
        }

        // 打印网格
        for (int a = 0; a < totalAreas; a++) {
            System.out.printf("%2d  ", a + 1);  // 从1开始
            for (int d = 0; d < totalDays; d++) {
                System.out.print(String.format("%3s", grid[a][d]));
            }
            System.out.println();
        }

        //System.out.println("\n图例：大写字母表示按时完成，小写字母表示超出最晚结束日期");
    }

}
