package com.dustin.ai.schedule.v2;

/**
 * 数组信息
 */
public class ArrayInfo {


    String name;//数组名称
    int width; //横坐标固定宽度，时间范围
    int height; //纵坐标固定高度，产线面数
    int minStartX; //最小开始横坐标
    int maxStartX; //最大开始横坐标
    boolean isPlaced; //是否已放置
    Matrix matrix;//放置矩阵位置

    public ArrayInfo(String name, int width, int height, int minStartX, int maxStartX) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.minStartX = minStartX;
        this.maxStartX = maxStartX;
    }

    //计算结束横坐标
    public int getEndX(int startX){
        return startX + width -1;
    }

    //校验横坐标是否符合条件
    public boolean isValidStartX(int startX){
        return startX >= minStartX && startX <= maxStartX;
    }

    /**
     * 获取放置难度权重
     * @return
     */
    public double getPlacementWeight(){
        double timeFlexibility = (double) (maxStartX - minStartX + 1) / (maxStartX + 1);
        double area = width * height;
        //约束，面积越大的数组难度权重越高
        return area / (timeFlexibility + 0.1);
    }


}
