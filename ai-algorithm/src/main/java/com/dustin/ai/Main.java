package com.dustin.ai;

import com.dustin.ai.schedule.v2.CoordinateSelector;
import com.dustin.ai.schedule.v2.Matrix;
import com.dustin.ai.schedule.v2.MatrixResult;

import java.util.Map;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        int[][] coordinateSystem = new int[200][218];

//        coordinateSystem[2][3] = 1;
//        coordinateSystem[2][4] = 1;
//        coordinateSystem[0][0] = 1;

        CoordinateSelector placer = new CoordinateSelector(coordinateSystem);

        placer.addArrayInfo("813", 30, 26, 122, 134);
        placer.addArrayInfo("541", 20, 26, 90, 98);
        placer.addArrayInfo("551", 20, 26, 75, 83);
        placer.addArrayInfo("194", 25, 20, 65, 75);
        placer.addArrayInfo("821", 25, 20, 65, 75);
        placer.addArrayInfo("831", 25, 20, 65, 75);
        placer.addArrayInfo("114", 27, 26, 42, 55);
        placer.addArrayInfo("184", 25, 26, 57, 73);
        placer.addArrayInfo("682", 18, 24, 80, 90);
        placer.addArrayInfo("692", 18, 24, 80, 90);
        placer.addArrayInfo("141", 25, 24, 51, 68);
        placer.addArrayInfo("151", 25, 24, 43, 59);
        placer.addArrayInfo("422", 13, 26, 49, 60);
        placer.addArrayInfo("435", 15, 30, 50, 62);
        placer.addArrayInfo("424", 15, 26, 49, 60);
        placer.addArrayInfo("102", 45, 28, 0, 17);
        placer.addArrayInfo("423", 13, 26, 49, 60);
        placer.addArrayInfo("682LB", 7, 23, 68, 83);
        placer.addArrayInfo("692LB", 7, 23, 68, 83);
//        placer.addArrayInfo("668LB",7,23,44,65);
//        placer.addArrayInfo("678LB",7,23,37,56);


//        placer.addArrayInfo("B",3,3,0,5);
//        placer.addArrayInfo("C",2,3,1,6);
//        placer.addArrayInfo("D",4,2,0,9);

        MatrixResult matrixResult = placer.placeAllMatrix();

        placer.generateReport(matrixResult);

        placer.visualDisplay();

        //显示结果
        System.out.println("最终结果");
        for (Map.Entry<String, Matrix> entry : matrixResult.successMatrixMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }


    }
}