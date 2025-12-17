package com.dustin.ai.schedule.v2;

import java.util.Objects;

/**
 * 矩阵索引位置
 */
public class Position {

    //横坐标位置
    int x;
    //纵坐标位置
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "{" + x + "," + y + "}";
    }


    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x  && y == position.y;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x,y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
