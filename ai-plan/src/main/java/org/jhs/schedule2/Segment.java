package org.jhs.schedule2;

public class Segment {
    private String id;
    private int area = 1;       // 占用的面数
    private int days = 1;       // 占用的天数
    private int earliestStartDay = 0;  // 最早开始日期
    private int latestEndDay = 0;      // 最晚结束日期
    private int quantity = 0; //物量

    public Segment(String id, int area, int days, int earliestStartDay, int latestEndDay) {
        this(id, area, days, earliestStartDay, latestEndDay, 0);
    }

    public Segment(String id, int area, int days, int earliestStartDay, int latestEndDay, int quantity) {
        this.id = id;
        this.area = area;
        this.days = days;
        this.earliestStartDay = earliestStartDay;
        this.latestEndDay = latestEndDay;
    }

    // 获取该分段可能的开始日期范围
    public int getEarliestPossibleStart() {
        return earliestStartDay;
    }

    // 获取该分段可能的最晚开始日期
    public int getLatestPossibleStart() {
        return latestEndDay - days + 1;
    }

    // 检查该分段是否有可能被安排（时间上是否可行）
    public boolean isTimeFeasible() {
        return getEarliestPossibleStart() <= getLatestPossibleStart();
    }

    // getter方法
    public String getId() { return id; }
    public int getArea() { return area; }
    public int getDays() { return days; }
    public int getEarliestStartDay() { return earliestStartDay; }
    public int getLatestEndDay() { return latestEndDay; }

    @Override
    public String toString() {
        return String.format("分段 %s: 面积=%d, 天数=%d, 最早开始=%d, 最晚结束=%d",
                id, area, days, earliestStartDay, latestEndDay);
    }
}
