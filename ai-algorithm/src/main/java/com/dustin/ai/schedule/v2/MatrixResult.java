package com.dustin.ai.schedule.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 放置结果
 */
public class MatrixResult {

    public Map<String, Matrix> successMatrixMap;//成功放置矩阵数据集
    List<ArrayInfo> failedMatrixList;//放置失败的矩阵数据集合
    List<ArrayInfo> betterMatrixList;//放宽约束后放置的数组

    public MatrixResult() {
        this.successMatrixMap = new HashMap<>();
        this.failedMatrixList = new ArrayList<>();
        this.betterMatrixList = new ArrayList<>();
    }


    public void addSuccessMatrix(String name, Matrix matrix){
        successMatrixMap.put(name,matrix);
    }

    public void addFailedMatrix(ArrayInfo arrayInfo){
        failedMatrixList.add(arrayInfo);
    }

    public void addBetterMatrix(ArrayInfo arrayInfo){
        betterMatrixList.add(arrayInfo);
    }

    public int getTotalSuccess(){
        return successMatrixMap.size() + betterMatrixList.size();
    }

}
