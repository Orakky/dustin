package org.jhs.schedule2;

public class SegmentSchedule {
    private Segment segment;
    private int startDay;   // 开始日期（0-based索引）
    private int startArea;  // 开始面（0-based索引）

    public SegmentSchedule(Segment segment, int startDay, int startArea) {
        this.segment = segment;
        this.startDay = startDay;
        this.startArea = startArea;
    }

    // 获取结束日期（0-based计划开始的日期天数）
    public int getEndDay() {
        return startDay + segment.getDays() - 1;
    }

    // 获取结束面（0-based索引）
    public int getEndArea() {
        return startArea + segment.getArea() - 1;
    }

    // 检查是否在最晚结束日期前完成
    public boolean isCompletedBeforeLatestEnd() {
        return getEndDay() <= segment.getLatestEndDay();
    }

    // getter方法
    public Segment getSegment() { return segment; }
    //获取开始日期（0-based计划开始的日期天数）
    public int getStartDay() { return startDay; }
    //获取开始面数（0-based索引）
    public int getStartArea() { return startArea; }

    @Override
    public String toString() {
        return String.format("分段 %s: 从日期 %d 到 %d, 从面 %d 到 %d %s",
                segment.getId(),
                startDay + 1,  // 转换为1-based显示
                getEndDay() + 1,
                startArea + 1,
                getEndArea() + 1,
                isCompletedBeforeLatestEnd() ? "" : "(超出最晚结束日期)");
    }
}
