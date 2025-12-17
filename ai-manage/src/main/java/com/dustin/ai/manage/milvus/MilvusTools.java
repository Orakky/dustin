package com.dustin.ai.manage.milvus;

import org.noear.solon.ai.rag.Document;
import org.noear.solon.ai.rag.util.QueryCondition;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 向量数据库文档操作工具类
 */
public interface MilvusTools {

    /**
     * 保存文档
     * @param name
     * @param document
     */
    void save(String name, Document document) throws IOException;

    /**
     * 批量保存文档
     * @param name
     * @param documents
     */
    void save(String name, List<Document> documents) throws IOException;


    /**
     * 异步保存文档
     * @param name
     * @param documents
     * @param progressCallback - 回调进度
     */
    CompletableFuture<Void> asyncSave(String name, List<Document> documents, BiConsumer<Integer, Integer> progressCallback);


    /**
     * 删除文档
     * @param name
     * @param ids
     */
    void delete(String name,String... ids) throws IOException;


    /**
     * 更新文档
     * 基于文档的milvus向量数据库在处理数据时无法直接基于主键进行更新content，需要先删除集合再插入新的文档
     * @param name
     * @param documents
     */
    void update(String name,List<Document> documents) throws IOException;


    /**
     * 根据条件检索文档
     * @param name
     * @param condition
     * @return
     * @throws IOException
     */
    List<Document> search(String name, QueryCondition condition) throws IOException;

    /**
     * 根据条件检索文档
     * @param name
     * @param query
     * @return
     * @throws IOException
     */
    List<Document> search(String name, String query) throws IOException;


}
