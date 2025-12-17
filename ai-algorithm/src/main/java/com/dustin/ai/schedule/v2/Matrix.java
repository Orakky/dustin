package com.dustin.ai.schedule.v2;

/**
 * 矩阵位置信息
 */
public class Matrix {

    String name;
    Position topLeft;
    Position bottomRight;


    public Matrix(String name, Position topLeft, Position bottomRight) {
        this.name = name;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    /**
     * 获取分段矩阵的宽度
     * @return
     */
    public int getWidth(){
        return bottomRight.x - topLeft.x + 1;
    }

    /**
     * 获取分段矩阵的高度
     * @return
     */
    public int getHeight(){
        return topLeft.y - bottomRight.y + 1;
    }

    @Override
    public String toString() {
        return name + ": 左上角 - " + topLeft + ", 右下角 - " + bottomRight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Position topLeft) {
        this.topLeft = topLeft;
    }

    public Position getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Position bottomRight) {
        this.bottomRight = bottomRight;
    }
}

