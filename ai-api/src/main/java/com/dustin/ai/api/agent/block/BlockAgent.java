//package com.dustin.ai.api.agent.block;
//
//import cn.hutool.core.map.MapUtil;
//import cn.hutool.json.JSONUtil;
//import agent.com.dustin.ai.api.BasicAgent;
//import agent.com.dustin.ai.api.IntentEnums;
//import vo.block.agent.com.dustin.ai.api.BlockAiDataVo;
//import vo.block.agent.com.dustin.ai.api.BlockMatrixVo;
//import vo.block.agent.com.dustin.ai.api.BlockVo;
//import vo.block.agent.com.dustin.ai.api.PlVo;
//import annotation.com.dustin.ai.manage.AgentLabel;
//import entity.session.com.dustin.ai.manage.BasicChatTask;
//import service.session.com.dustin.ai.manage.BasicChatService;
//import enums.support.com.dustin.ai.BasicDictEnums;
//import mermaid.generator.support.com.dustin.ai.GanttSection;
//import mermaid.generator.support.com.dustin.ai.GanttTask;
//import mermaid.generator.support.com.dustin.ai.MermaidGenerator;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.compress.utils.Lists;
//import org.apache.commons.lang3.StringUtils;
//import v2.schedule.com.dustin.ai.CoordinateSelector;
//import v2.schedule.com.dustin.ai.Matrix;
//import v2.schedule.com.dustin.ai.MatrixResult;
//import v2.schedule.com.dustin.ai.Position;
//import org.noear.solon.ai.chat.message.AssistantMessage;
//import org.noear.solon.annotation.Component;
//import org.noear.solon.annotation.Inject;
//import reactor.core.publisher.Flux;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 需要优化，本质上需要链式流程处理
// */
//@Slf4j
//@AgentLabel(name = "BLOCK")
//@Component(name = "blockAgent")
//public class BlockAgent implements BasicAgent {
//
//    @Inject
//    McpServerConsumer mcpServerConsumer;
//
//    @Inject
//    BlockIntentRecognition blockIntentRecognition;
//
//    @Inject
//    BasicChatService basicChatService;
//
//
//    /**
//     * 获取业务数据
//     *
//     * @param paramMap
//     * @return
//     */
//    public Map<String, Object> fetchData(Map<String, Object> paramMap) {
//        return mcpServerConsumer.findMcpData(paramMap);
//    }
//
//    /**
//     * 将业务数据回传给mes系统
//     *
//     * @param dataMap
//     * @return
//     */
//    public Map<String, Object> saveDataToMes(Map<String, Object> dataMap) {
//        return mcpServerConsumer.translationData(dataMap);
//    }
//
//    /**
//     * 处理数据
//     *
//     * @param paramMap
//     * @return
//     */
//    public Map<String, Object> processData(Map<String, Object> paramMap) {
//        //1 获取定盘数据
//        Map<String, Object> oriDataMap = fetchData(paramMap);
//        if (MapUtil.isEmpty(oriDataMap)) {
//            log.info("无法获取到对应的大组定盘数据，退出数据处理流程");
//            return null;
//        }
//        //2 处理数据,提取核心数据块
//        BlockAiDataVo blockAiDataVo = JSONUtil.parseObj(oriDataMap).toBean(BlockAiDataVo.class);
//        //3 按照跨进行分组处理数据
//        List<PlVo> plList = blockAiDataVo.getPlList();
//        Map<String, List<BlockVo>> blockListMap = blockAiDataVo.getBlockList();
//        Map<String, String> dateMap = blockAiDataVo.getDateMap();
//        Map<Integer, MatrixResult> successMatrixMap = new HashMap<>();
//        Map<String, MatrixResult> plMatrixMap = new HashMap<>();
//        log.info("开始处理跨数据");
//        for (PlVo plVo : plList) {
//            Integer plId = plVo.getPlId();
//            String plName = plVo.getPlName();
//            List<BlockVo> blockVos = blockListMap.get(plId.toString());
//
//            MatrixResult matrixResult = blockProcessMatrix(plVo, blockVos, dateMap);
//
//            if (null != matrixResult) {
//                Map<String, Matrix> matrixMap = matrixResult.successMatrixMap;
//                successMatrixMap.put(plId, matrixResult);
//                plMatrixMap.put(plName, matrixResult);
//            }
//        }
//        //4 判断map是否为空
//        if (MapUtil.isEmpty(successMatrixMap)) {
//            log.info("未能成功放置任何大组分段，退出数据处理流程");
//            return null;
//        }
//        List<BlockMatrixVo> blockMatrixVoList = new ArrayList<>();
//        //5 构建数据返回体
//        log.info("开始构建数据返回体...");
//        for (Map.Entry<Integer, MatrixResult> entry : successMatrixMap.entrySet()) {
//            Integer plId = entry.getKey();
//            MatrixResult matrixResult = entry.getValue();
//            Map<String, Matrix> matrixMap = matrixResult.successMatrixMap;
//            for (Map.Entry<String, Matrix> matrixEntry : matrixMap.entrySet()) {
//                String blockIdStr = matrixEntry.getKey();
//                String[] split = blockIdStr.split("-");
//                Matrix matrix = matrixEntry.getValue();
//
//                Position topLeft = matrix.getTopLeft();
//                Position bottomRight = matrix.getBottomRight();
//                BlockMatrixVo tempVo = new BlockMatrixVo();
//                tempVo.setBlockId(Integer.valueOf(split[0]));
//                tempVo.setPlId(plId);
//                tempVo.setShipId(Integer.valueOf(split[1]));
//                tempVo.setBlockNo(split[2]);
//                tempVo.setStartX(topLeft.getX());
//                tempVo.setStartY(topLeft.getY());
//                tempVo.setEndX(bottomRight.getX());
//                tempVo.setEndY(bottomRight.getY());
//
//                blockMatrixVoList.add(tempVo);
//            }
//        }
//
//        log.info("成功构建定盘分组计划返回体");
//
//        Map<String, Object> resultMap = new HashMap<>();
//        resultMap.put("dateMap", dateMap);
//        resultMap.put("blockList", blockMatrixVoList);
//        //6 将数据回传给mes进行入库保存
//        Map<String, Object> saveRstMap = saveDataToMes(resultMap);
//        //7 保存回传后的返回数据
//        resultMap.put("response", saveRstMap);
//        resultMap.put("matrixResult", plMatrixMap);
//        resultMap.put("shipNos", paramMap.get("shipNos"));
//        log.info("大组定盘数据完整数据集:{}", JSONUtil.toJsonStr(oriDataMap));
//        log.info("定盘大组计划完全返回体数据:{}", JSONUtil.toJsonStr(resultMap));
//        return resultMap;
//    }
//
//    /**
//     * 创建mermaid gantt 图
//     *
//     * @param paramMap
//     * @return
//     */
//    public Map<String, Object> createMermaidGantt(Map<String, Object> paramMap) {
//        if (null == paramMap || null == paramMap.get("matrixResult")) {
//            log.info("没有合适的入参，无法生成mermaid gantt 文档");
//            return paramMap;
//        }
//
//        log.info("根据输入的数据，开始构建mermaid gantt图，生成对应的md字符串");
//        //key 为 plNo value为分段集合
//        Map<String, MatrixResult> martixRstMap = (Map<String, MatrixResult>) paramMap.get("matrixResult");
//        Map<String, String> dateMap = (Map<String, String>) paramMap.get("dateMap");
//        String shipNos = paramMap.get("shipNos").toString();
//        List<GanttSection> sectionList = Lists.newArrayList();
//        for (Map.Entry<String, MatrixResult> entry : martixRstMap.entrySet()) {
//            String plNo = entry.getKey();//跨名称，也就是section name
//            MatrixResult matrixResult = entry.getValue();
//            Map<String, Matrix> successMatrixMap = matrixResult.successMatrixMap;
//
//            List<GanttTask> taskList = Lists.newArrayList();
//
//            for (Map.Entry<String, Matrix> matrixEntry : successMatrixMap.entrySet()) {
//                String blockStr = matrixEntry.getKey();
//                Matrix matrix = matrixEntry.getValue();
//                //block_id ship_id block_no
//                String[] split = blockStr.split("-");
//
//                GanttTask ganttTask = new GanttTask();
//
//                ganttTask.setStatus("");
//                String startDate = dateMap.get(String.valueOf(matrix.getTopLeft().getX()));
//                String endDate = dateMap.get(String.valueOf(matrix.getBottomRight().getX()));
//                ganttTask.setName(split[2] + "(" + startDate + " ~ " + endDate + ")");
//                ganttTask.setStartDate(startDate);
//                ganttTask.setDuration(matrix.getBottomRight().getY() - matrix.getTopLeft().getY());
//
//                taskList.add(ganttTask);
//            }
//            GanttSection ganttSection = new GanttSection(plNo, taskList);
//            sectionList.add(ganttSection);
//        }
//        log.info("成功构建mermaid gantt 图数据结构,数据集合：{}", JSONUtil.toJsonStr(sectionList));
//        String ganttMdStr = MermaidGenerator.generateGantt("大组定盘计划-" + shipNos, sectionList);
//        log.info("甘特图md 字符串：{}", ganttMdStr);
//        paramMap.put("gantt", ganttMdStr);
//        return paramMap;
//    }
//
//
//    /**
//     * 处理跨的数据
//     *
//     * @param plVo
//     * @param blockVos
//     * @param dateMap
//     * @return
//     */
//    private MatrixResult blockProcessMatrix(PlVo plVo, List<BlockVo> blockVos, Map<String, String> dateMap) {
//        log.info("按跨对分段数据进行处理，当前处理的跨：{}", plVo.getPlName());
//        Map<String, Object> rstMap = new HashMap<>();
//        Integer plNo = plVo.getPlNo();//面数代表的y坐标，也就是行
//        int size = dateMap.size();//时间代表的x坐标，也就是列
//        int[][] coordinateSystem = new int[plNo][size];
//        CoordinateSelector placer = new CoordinateSelector(coordinateSystem);
//        for (BlockVo blockVo : blockVos) {
//            if (blockVo.getSurface() != null && blockVo.getCycle() != null && blockVo.getMinStartIndex() != null && blockVo.getMaxStartIndex() != null) {
//                log.info("当前数据：{}", JSONUtil.toJsonStr(blockVo));
//                placer.addArrayInfo(blockVo.getBlockId() + "-" + blockVo.getShipId() + "-" + blockVo.getBlockNo(), blockVo.getCycle(), blockVo.getSurface(), blockVo.getMinStartIndex(), blockVo.getMaxStartIndex());
//            }
//        }
//        log.info("分段已全部构建成算法需要的集合数据,开始进行矩阵放置任务,当前处理的跨矩阵为：{}", plVo.getPlName());
//        MatrixResult matrixResult = placer.placeAllMatrix();
//        placer.generateReport(matrixResult);
//
//        placer.visualDisplay();
//
//        rstMap.put(plVo.getPlName(), matrixResult);
//
//        log.info("当前跨的数据处理已完成，当前处理的跨: {}", plVo.getPlName());
//        return matrixResult;
//    }
//
//
//    /**
//     * 对外执行方法
//     *
//     * @param paramMap
//     * @return
//     */
//    @Override
//    public Map<String, Object> executeChain(Map<String, Object> paramMap) {
//
//        //1 处理数据
//        Map<String, Object> dataMap = processData(paramMap);
//
//        //2 生成gantt 图字符串
//        Map<String, Object> rstMap = createMermaidGantt(dataMap);
//
//        return rstMap;
//
//    }
//
//    /**
//     * 智能体流式输
//     *
//     * @param paramMap
//     * @return
//     */
//    @Override
//    public Flux<AssistantMessage> chatFluxReturn(Map<String, Object> paramMap) {
//        log.info("流式相应bot 聊天接口");
//
//        IntentEnums intent = blockIntentRecognition.recognize(paramMap);
//
//        return null;
//    }
//
//    /**
//     * 根据识别出来的基础意图选择对应的方法
//     *
//     * @param intent
//     * @param paramMap
//     */
//    public Map<String, Object> taskRoute(IntentEnums intent, Map<String, Object> paramMap) {
//
//
//        switch (intent) {
//            case PRE_TASK:
//                return preTaskExecute(paramMap);
//            case TASK_MESSAGE:
////                taskMessageExecute(paramMap);
//                break;
//            case PURE_MESSAGE:
////                pureMessageExecute(paramMap);
//                break;
//            case NONE:
//                break;
//        }
//
//        return null;
//    }
//
//    /**
//     * 用户没有任何输入，开始处理
//     *
//     * @param paramMap
//     */
//    private Map<String, Object> preTaskExecute(Map<String, Object> paramMap) {
//        //1 获取任务名称
//        String preTask = (String) paramMap.get("pre_task");
//
//        //2 获取sessionId
//        String sessionId = (String) paramMap.get("sessionId");
//
//        //3 获取参数
//        String shipNos = (String) paramMap.get("shipNos");
//
//        //4 根据sessionId获取对应的任务历史
//        List<BasicChatTask> chatTaskList = basicChatService.listBasicChatTask(sessionId);
//
//        if (CollectionUtils.isEmpty(chatTaskList)) {
//            //表示这一次是初次执行任务，并非中间流程 TODO
//            log.info("当前为特定任务初次执行");
//            if (StringUtils.isEmpty(shipNos)) {
//                //当前不存在对应的任务
//                log.info("不满足任务执行的条件,退出处理");
////                throw new Exception("当前不满足任务执行的条件");
//                return null;
//            }
//            //shipNos存在
//            //执行任务链
//            //并且需要将当前任务链进行保存
//            Map<String, Object> rstMap = executeChain(paramMap);
//            //存储当前任务
//            BasicChatTask basicChatTask = new BasicChatTask();
//            basicChatTask.setSessionId(sessionId);
//            basicChatTask.setTaskSort(1);
//            basicChatTask.setTaskFunction("com.dustin.ai.api.agent.block.BlockAgent.executeChain");
//            basicChatTask.setTaskStatus(BasicDictEnums.FunctionStatus.FINISH.getCode());//
//            return rstMap;
//        }
//        //TODO 如果存在任务链则需要判断是否执行过当前的任务
//
//
//        return null;
//    }
//
//
//}
