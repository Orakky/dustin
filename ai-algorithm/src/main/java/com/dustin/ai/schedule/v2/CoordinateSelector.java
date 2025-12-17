package com.dustin.ai.schedule.v2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 坐标选择器
 */
public class CoordinateSelector {

    private int[][] coordinateSystem;//坐标系
    private Set<Position> obstacles;//障碍物位置
    List<ArrayInfo> arrayInfoList;//分段集合
    private ConcurrentHashMap<String, Matrix> currentMatrixMap;//分段矩阵位置信息
    int maxX;//坐标系最大横坐标
    private int maxY;//坐标系最大纵坐标



    public CoordinateSelector(int[][] coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        this.maxY = coordinateSystem.length -1;
        this.maxX = (maxY >=0 ) ? coordinateSystem[0].length - 1: 0;
        this.obstacles = new HashSet<>();
        this.arrayInfoList = new ArrayList<>();
        this.currentMatrixMap = new ConcurrentHashMap<>();

        //计算障碍物
        detectObstacles();
    }

    /**
     * 计算坐标系中是否存在障碍物，即是否已填充
     */
    private void detectObstacles(){
        for(int y = 0; y <= maxY; y++){
            for(int x = 0; x <= maxX; x++){
                if(coordinateSystem[y][x] != 0){
                    obstacles.add(new Position(x,y));
                }
            }
        }
        System.out.println("检测到 " + obstacles.size() + " 个障碍物");
    }

    //添加分段信息
    public void addArrayInfo(String name, int width,int height,int minStartX,int maxStartX){
        arrayInfoList.add(new ArrayInfo(name,width,height,minStartX,maxStartX));
    }

    /**
     * 检查举行区域是否和障碍物重叠 - 优化版
     * @param topLeft
     * @param bottomRight
     * @return
     */
    private boolean overlapWithObstacles(Position topLeft, Position bottomRight) {
        // 快速路径：如果障碍物集合为空，直接返回false
        if (obstacles.isEmpty()) {
            return false;
        }

        // 预计算边界以减少重复计算
        int minX = topLeft.x;
        int minY = topLeft.y;
        int maxX = bottomRight.x;
        int maxY = bottomRight.y;

        // 优化：使用Bresenham算法检查边界而不是整个区域
        // 首先检查四个顶点
        if (obstacles.contains(new Position(minX, minY)) ||
                obstacles.contains(new Position(maxX, minY)) ||
                obstacles.contains(new Position(minX, maxY)) ||
                obstacles.contains(new Position(maxX, maxY))) {
            return true;
        }

        // 对于大区域，采用扫描线算法，每隔几行检查一次
        int step = Math.max(1, (maxY - minY) / 10); // 最多检查10行
        for (int y = minY; y <= maxY; y += step) {
            // 检查行的两端和中间
            if (obstacles.contains(new Position(minX, y)) ||
                    obstacles.contains(new Position(maxX, y)) ||
                    obstacles.contains(new Position((minX + maxX) / 2, y))) {
                return true;
            }
        }

        // 对于小区域，仍然使用原始方法确保正确性
        if (maxX - minX < 20 && maxY - minY < 20) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    if (obstacles.contains(new Position(x, y))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查两个分段矩阵之间是否重叠
     * @param matrix1
     * @param matrix2
     * @return
     */
    private boolean matrixOverlap(Matrix matrix1, Matrix matrix2){
        return matrix1.topLeft.x <= matrix2.bottomRight.x &&
                matrix1.bottomRight.x >= matrix2.topLeft.x &&
                matrix1.topLeft.y <= matrix2.bottomRight.y &&
                matrix1.bottomRight.y >= matrix2.topLeft.y;
    }

    /**
     * 检查新的分段矩阵和已放置好的矩阵之间是否重叠 - 优化版
     * @param newMatrix
     * @return
     */
    private boolean overlapWithBlockMatrixMap(Matrix newMatrix){
        // 快速路径：如果当前没有放置任何矩阵，直接返回false
        if (currentMatrixMap.isEmpty()) {
            return false;
        }

        // 预计算边界
        int newMinX = newMatrix.topLeft.x;
        int newMinY = newMatrix.topLeft.y;
        int newMaxX = newMatrix.bottomRight.x;
        int newMaxY = newMatrix.bottomRight.y;

        // 优化：使用空间分区或快速排除
        for (Matrix oldMatrix : currentMatrixMap.values()) {
            // 快速排除：如果Y范围完全不重叠，直接跳过
            if (oldMatrix.bottomRight.y < newMinY || oldMatrix.topLeft.y > newMaxY) {
                continue;
            }
            // 快速排除：如果X范围完全不重叠，直接跳过
            if (oldMatrix.bottomRight.x < newMinX || oldMatrix.topLeft.x > newMaxX) {
                continue;
            }
            // 精确检查重叠
            if (matrixOverlap(newMatrix, oldMatrix)) {
                return true;
            }
        }

        return false;
    }


    /**
     * 检查矩阵位置是否符合坐标系
     * @param arrayInfo
     * @param topLeft
     * @return
     */
    private boolean isValidPositionStrict(ArrayInfo arrayInfo, Position topLeft){
        //检查边界
        if(topLeft.x < 0 || topLeft.y < 0 ||
        topLeft.x + arrayInfo.width -1 > maxX||
        topLeft.y + arrayInfo.height -1 > maxY){
            return false;
        }

        //检查横坐标（时间约束）
        if(!arrayInfo.isValidStartX(topLeft.x)){
            return false;
        }

        //计算bottomRight
        Position bottomRight = new Position(topLeft.x + arrayInfo.width - 1, topLeft.y + arrayInfo.height - 1);

        //检查与障碍物的重叠
        if(overlapWithObstacles(topLeft,bottomRight)){
            return false;
        }

        //创建临时矩阵进行计算检查
        Matrix tempMatrix = new Matrix(arrayInfo.name, topLeft, bottomRight);


        //与已放置的矩阵进行重叠检查
        return !overlapWithBlockMatrixMap(tempMatrix);
    }


    /**
     * 检查位置是否有效，忽视横坐标约束
     * @param arrayInfo
     * @param topLeft
     * @return
     */
    private boolean isValidPositionBetter(ArrayInfo arrayInfo, Position topLeft){
        //检查边界
        if(topLeft.x < 0 || topLeft.y < 0 ||
                topLeft.x + arrayInfo.width -1 > maxX||
                topLeft.y + arrayInfo.height -1 > maxY){
            return false;
        }

        //计算bottomRight
        Position bottomRight = new Position(topLeft.x + arrayInfo.width - 1, topLeft.y + arrayInfo.height - 1);

        //检查与障碍物的重叠
        if(overlapWithObstacles(topLeft,bottomRight)){
            return false;
        }

        //创建临时矩阵进行计算检查
        Matrix tempMatrix = new Matrix(arrayInfo.name, topLeft, bottomRight);


        //与已放置的矩阵进行重叠检查
        return !overlapWithBlockMatrixMap(tempMatrix);

    }


    /**
     * 找到所有可能的放置位置,严格
     * @param arrayInfo
     * @return
     */
    public List<Position> findPossiblePositionStrict(ArrayInfo arrayInfo){
        List<Position> positions = new ArrayList<>();

        // 提前检查数组尺寸是否过大
        if (arrayInfo.width > maxX + 1 || arrayInfo.height > maxY + 1) {
            return positions;
        }

        // 优化循环顺序：先Y后X，这样可以更早发现冲突
        for (int startY = 0; startY <= maxY - arrayInfo.height + 1; startY++) {
            for (int startX = arrayInfo.minStartX; startX <= arrayInfo.maxStartX; startX++) {
                Position position = new Position(startX, startY);
                // 快速剪枝：先检查边界，再检查详细重叠
                if (position.x < 0 || position.y < 0 ||
                        position.x + arrayInfo.width - 1 > maxX ||
                        position.y + arrayInfo.height - 1 > maxY) {
                    continue;
                }
                if (isValidPositionStrict(arrayInfo, position)) {
                    positions.add(position);
                }
            }
        }
        return positions;
    }


    /**
     * 找到所有可能的放置位置，忽视横坐标约束 - 优化版
     * @param arrayInfo
     * @return
     */
    public List<Position> findPossiblePositionBetter(ArrayInfo arrayInfo){
        List<Position> positions = new ArrayList<>();

        // 提前检查数组尺寸是否过大
        if (arrayInfo.width > maxX + 1 || arrayInfo.height > maxY + 1) {
            return positions;
        }

        int betterMinX = Math.max(0, arrayInfo.minStartX - 2);//向左放宽2个坐标
        int betterMaxX = Math.min(maxX - arrayInfo.width + 1, arrayInfo.maxStartX + 2);//向右放宽2个坐标

        // 优化循环顺序：先Y后X，这样可以更早发现冲突
        for (int startY = 0; startY <= maxY - arrayInfo.height + 1; startY++) {
            for (int startX = betterMinX; startX <= betterMaxX; startX++) {
                // 快速剪枝：先检查边界，再检查详细重叠
                if (startX < 0 || startY < 0 ||
                        startX + arrayInfo.width - 1 > maxX ||
                        startY + arrayInfo.height - 1 > maxY) {
                    continue;
                }
                Position position = new Position(startX, startY);
                if (isValidPositionBetter(arrayInfo, position)) {
                    positions.add(position);
                }
            }
        }

        return positions;
    }


    /**
     * 按照放置难度权重排序
     */
    private void sortMatrixByWeight(){
        arrayInfoList.sort((a1, a2) -> {
            // 改进的权重计算逻辑
            double score1 = calculatePlacementWeight(a1);
            double score2 = calculatePlacementWeight(a2);
            return Double.compare(score2, score1);
        });
    }

    /**
     * 计算放置难度权重
     * @param arrayInfo
     * @return
     */
    private double calculatePlacementWeight(ArrayInfo arrayInfo) {
        // 避免除零错误
        int timeWindowSize = arrayInfo.maxStartX - arrayInfo.minStartX + 1;
        if (timeWindowSize <= 0) {
            timeWindowSize = 1;
        }

        // 改进的权重计算：更合理地处理时间灵活性
        double timeFlexibility = Math.max(0.1, (double) timeWindowSize / Math.max(1, arrayInfo.maxStartX + 1));
        double area = arrayInfo.width * arrayInfo.height;

        // 对于特别大的数组，给予适当调整，避免过度优先放置导致的回溯问题
        double sizePenalty = 1.0;
        if (arrayInfo.width > 40 && arrayInfo.height > 25) { // 针对大尺寸数组
            sizePenalty = 0.8; // 略微降低优先级
        }

        //约束，面积越大的数组难度权重越高，但对超大数组进行微调
        return (area / timeFlexibility) * sizePenalty;
    }

    /**
     * 按照约束条件，严格放置 - 激进优化版
     * @param arrayIndex
     * @return
     */
    private boolean placeWithStrict(int arrayIndex){
        // 1. 快速终止条件
        if(arrayIndex >= arrayInfoList.size()){
            return true;
        }

        ArrayInfo currentBlock = arrayInfoList.get(arrayIndex);

        // 2. 对于剩余未放置的数组数量过多时，采用更激进的限制策略
        int remainingBlocks = arrayInfoList.size() - arrayIndex;

        // 3. 获取可能的位置 - 对于大数组或后期阶段，限制位置数量
        List<Position> possiblePositions = findPossiblePositionStrict(currentBlock);

        // 如果没有可能的位置，直接返回false
        if (possiblePositions.isEmpty()) {
            return false;
        }

        // 4. 优化排序策略：综合考虑位置紧凑度和回溯效率
        possiblePositions.sort((o1, o2) -> {
            // 首先按Y坐标排序，优先放置在上方
            if (o1.y != o2.y) {
                return Integer.compare(o1.y, o2.y);
            }
            // Y相同的情况下，计算X到时间窗口中心的距离
            int center = (currentBlock.minStartX + currentBlock.maxStartX) / 2;
            int dist1 = Math.abs(o1.x - center);
            int dist2 = Math.abs(o2.x - center);
            return Integer.compare(dist1, dist2);
        });

        // 5. 动态调整尝试次数：根据剩余数组数量和数组大小动态调整
        int maxAttempts;
        if (currentBlock.width > 40 && currentBlock.height > 25) { // 大数组
            maxAttempts = Math.min(possiblePositions.size(), 5); // 大数组只尝试前5个最佳位置
        } else if (remainingBlocks > 10) { // 早期阶段
            maxAttempts = Math.min(possiblePositions.size(), 10); // 早期阶段尝试更多位置
        } else { // 后期阶段
            maxAttempts = Math.min(possiblePositions.size(), 50); // 后期阶段可以尝试更多位置
        }

        // 6. 使用非递归回溯或剪枝策略
        for (int i = 0; i < maxAttempts; i++) {
            Position position = possiblePositions.get(i);
            //放置当前数组
            Position bottomRight = new Position(position.x + currentBlock.width - 1, position.y + currentBlock.height - 1);
            Matrix matrix = new Matrix(currentBlock.name, position, bottomRight);

            currentMatrixMap.put(currentBlock.name, matrix);
            currentBlock.isPlaced = true;

            // 7. 对于大数组，添加早期剪枝：检查剩余空间是否足够放置所有剩余数组
            if (currentBlock.width > 30 && currentBlock.height > 20) {
                // 估算剩余空间
                int remainingHeight = maxY + 1;
                int placedHeight = 0;
                for (Matrix m : currentMatrixMap.values()) {
                    placedHeight = Math.max(placedHeight, m.bottomRight.y + 1);
                }
                int availableHeight = remainingHeight - placedHeight;

                // 简单估算：如果剩余高度不足以放置剩余数组的平均高度，则回溯
                double avgRemainingHeight = arrayInfoList.stream()
                        .skip(arrayIndex + 1)
                        .mapToDouble(ai -> ai.height)
                        .average().orElse(0);

                if (remainingBlocks * avgRemainingHeight > availableHeight * 1.5) { // 1.5是安全系数
                    // 回溯
                    currentMatrixMap.remove(currentBlock.name);
                    currentBlock.isPlaced = false;
                    continue;
                }
            }

            // 8. 递归尝试下一个数组
            if (placeWithStrict(arrayIndex + 1)) {
                return true;
            }

            //回溯
            currentMatrixMap.remove(currentBlock.name);
            currentBlock.isPlaced = false;
        }

        return false;
    }

    /**
     * 尝试放置未成功的数组，横坐标限制放宽
     */
    private void placeWithBetter(){
        for (ArrayInfo arrayInfo : arrayInfoList) {
            if(!arrayInfo.isPlaced){
                List<Position> betterPositions = findPossiblePositionBetter(arrayInfo);
                if(!betterPositions.isEmpty()){
                    //选择第一个可用位置
                    Position position = betterPositions.get(0);
                    Position bottomRight = new Position(position.x + arrayInfo.width - 1, position.y + arrayInfo.height - 1);
                    Matrix matrix = new Matrix(arrayInfo.name, position, bottomRight);
                    currentMatrixMap.put(arrayInfo.name,matrix);
                    arrayInfo.isPlaced =  true;
                    arrayInfo.matrix = matrix;
                    System.out.println("数组 " + arrayInfo.name + " 在放宽约束后成功放置");
                }
            }
        }
    }


    /**
     * 矩阵放置算法 - 性能优化版
     * @return
     */
    public MatrixResult placeAllMatrix(){
        long startTime = System.currentTimeMillis();
        MatrixResult matrixResult = new MatrixResult();
        currentMatrixMap.clear();

        // 重置状态
        for (ArrayInfo arrayInfo : arrayInfoList) {
            arrayInfo.isPlaced = false;
            arrayInfo.matrix = null;
        }

        // 1. 预排序：将数组按难度排序
        sortMatrixByWeight();

        System.out.println(" ==== 阶段一，按照横坐标,纵坐标约束放置 ====");

        // 2. 使用超时机制，避免无限递归
        AtomicBoolean strictSuccess = new AtomicBoolean(false);
        try {
            // 设置一个合理的超时时间（毫秒）
            final long timeout = 10000; // 10秒

            // 创建一个线程来执行回溯算法
            Thread thread = new Thread(() -> {
                strictSuccess.set(placeWithStrict(0));
            });

            thread.start();
            thread.join(timeout); // 等待线程完成或超时

            // 如果线程仍在运行，中断它
            if (thread.isAlive()) {
                thread.interrupt();
                System.out.println("严格放置算法超时，进入放宽约束阶段...");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("放置过程被中断，进入放宽约束阶段...");
        }

        if (strictSuccess.get()) {
            System.out.println("严格限制下成功放置所有的矩阵!");
        }else{
            System.out.println("严格约束下无法放置所有的矩阵，进入第二个阶段，放宽横坐标的约束...");
            System.out.println(" ==== 阶段二，放宽横坐标约束，纵坐标约束不变 ====");
            placeWithBetter();
        }

        //处理最终结果
        for (ArrayInfo arrayInfo : arrayInfoList) {
            if(arrayInfo.isPlaced){
                Matrix matrix = currentMatrixMap.get(arrayInfo.name);
                matrixResult.addSuccessMatrix(arrayInfo.name,matrix);

                //检查是否时是放宽约束后放置的
                if(!arrayInfo.isValidStartX(matrix.topLeft.x)){
                    matrixResult.addBetterMatrix(arrayInfo);
                }
            }else{
                matrixResult.addFailedMatrix(arrayInfo);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("总放置耗时: " + (endTime - startTime) + "ms");

        return matrixResult;
    }


    /**
     * 可视化效果
     */
    public void visualDisplay(){

        if(currentMatrixMap.isEmpty()){
            System.out.println("没有放置结果可以展示");
            return;
        }

        String[][] display = new String[maxY + 1][maxX +1];

        for(int i = 0; i <= maxY; i++){
            Arrays.fill(display[i],".");
        }


        //标记障碍物
        for (Position obstacle : obstacles) {
            if(obstacle.y <= maxY && obstacle.x <= maxX){
                display[obstacle.y][obstacle.x] = "X";
            }
        }

        //标记已放置的数组
        for (Matrix matrix :  currentMatrixMap.values()) {

            for(int y = matrix.topLeft.y; y <= matrix.bottomRight.y; y++){
                for(int x = matrix.topLeft.x; x <= matrix.bottomRight.x; x++){
                    if(y <= maxY && x <= maxX){
                        display[y][x] = matrix.name;
                    }
                }
            }
        }

        System.out.println("\n=== 可视化 ====");

        for(int i=0; i <= maxY; i++){
            for(int j = 0; j <= maxX; j++){
                System.out.print(display[i][j] + " ");
            }
            System.out.println();
        }
    }


    public void generateReport(MatrixResult matrixResult){
        System.out.println("\n==== 放置结果详细报告 ===");

        System.out.println("\n成功放置的数组 ( " + matrixResult.successMatrixMap.size() + " 个)");

        for (Map.Entry<String, Matrix> entry : matrixResult.successMatrixMap.entrySet()) {
            Matrix matrix = entry.getValue();
            String name = entry.getKey();
            ArrayInfo arrayInfo = findBlockByName(name);
            System.out.println(" " + matrix);
            System.out.println(" 时间约束：[" + arrayInfo.minStartX +", " + arrayInfo.maxStartX + "]");
            System.out.println(" 实际位置：x=" + matrix.topLeft.x + " 到 x=" + matrix.bottomRight.x);

            if(matrixResult.betterMatrixList.contains(arrayInfo)){
                System.out.println("   **注意：此数组在放宽时间约束后放置");
            }
        }
        if(!matrixResult.betterMatrixList.isEmpty()){
            System.out.println("\n放宽约束后放置的数组 (" + matrixResult.betterMatrixList.size() + " 个)");

            for (ArrayInfo arrayInfo : matrixResult.betterMatrixList) {
                System.out.println(" " + arrayInfo.name + ":原约束:[" + arrayInfo.minStartX + ", " + arrayInfo.maxStartX +"], 实际x=" + arrayInfo.matrix.topLeft.x);
            }
        }

        if(!matrixResult.failedMatrixList.isEmpty()){
            System.out.println("\n无法放置的数组 (" + matrixResult.failedMatrixList.size() + "个)");
            for (ArrayInfo arrayInfo : matrixResult.failedMatrixList) {
                System.out.println(" " + arrayInfo.name + ": " + arrayInfo.width + "x" + arrayInfo.height + " (时间约束：[" + arrayInfo.minStartX + ", " + arrayInfo.maxStartX +"])");
            }
        }
        System.out.println("\n 统计： 总共" + arrayInfoList.size() + "个数组，成功放置" + matrixResult.getTotalSuccess() + "个, 失败" + matrixResult.failedMatrixList.size() + "个");
    }



    private ArrayInfo findBlockByName(String name){
        for (ArrayInfo arrayInfo : arrayInfoList) {
            if(arrayInfo.name.equals(name)){
                return arrayInfo;
            }
        }
        return null;
    }



}

