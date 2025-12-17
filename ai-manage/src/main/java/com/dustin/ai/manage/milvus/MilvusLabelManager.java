package com.dustin.ai.manage.milvus;

import org.noear.solon.ai.rag.Document;
import org.noear.solon.ai.rag.repository.MilvusRepository;
import org.noear.solon.ai.rag.util.QueryCondition;
import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * milvus 向量数据库 管理工具
 * TODO 此处需要将静态和接口方法进行剥离,后续将MilvusTool抽离成独立接口
 */
@Component
public class MilvusLabelManager implements MilvusTools {

    static final Logger log = LoggerFactory.getLogger(MilvusLabelManager.class);

    private static final ConcurrentHashMap<String, MilvusRepository> milvusLabelMap = new ConcurrentHashMap<>();

    /**
     *  根据labelName获取到对应的milvus repository 实例
     * @param name
     * @return
     */
    public static MilvusRepository getMilvusRepositoryByName(String name){

        if(null != name && !"".equals(name)){
            return milvusLabelMap.get(name);
        }

        return null;
    }

    /**
     * 根据名称注册对应的milvusRepository实例
     * @param name
     * @param milvusRepository
     */
    public static void register(String name,MilvusRepository milvusRepository){
        if(null != milvusRepository && null != name && !"".equals(name)){
            log.info("当前注册的milvus repository Name:{}",name);
            milvusLabelMap.putIfAbsent(name,milvusRepository);
        }
    }

    /**
     * 根据名称name注销对应的milvus repository实例
     * @param name
     */
    public static void unregister(String name){
        milvusLabelMap.remove(name);
    }

    /**
     * 获取milvus repository map实例
     * @return
     */
    public static Map<String,MilvusRepository> getMap(){
        return milvusLabelMap;
    }


    /**
     * 保存文档
     *
     * @param name
     * @param document
     */
    @Override
    public void save(String name, Document document) throws IOException {
        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }

        repository.save(document);
    }

    /**
     * 批量保存文档
     *
     * @param name
     * @param documents
     */
    @Override
    public void save(String name, List<Document> documents) throws IOException {

        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }

        repository.save(documents);
    }


    /**
     * 异步保存文档
     *
     * @param name
     * @param documents
     * @param progressCallback - 回调进度
     */
    @Override
    public CompletableFuture<Void> asyncSave(String name, List<Document> documents, BiConsumer<Integer, Integer> progressCallback) {
        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }

        return repository.asyncSave(documents,progressCallback);
    }


    /**
     * 删除文档
     *
     * @param name
     * @param ids
     */
    @Override
    public void delete(String name,String... ids) throws IOException {
        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }

        repository.deleteById(ids);
    }


    /**
     * 更新文档
     * 基于文档的milvus向量数据库在处理数据时无法直接基于主键进行更新content，需要先删除集合再插入新的文档
     * @param name
     * @param documents
     */
    @Override
    public void update(String name, List<Document> documents) throws IOException {
        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }


        repository.dropRepository();

        //初始化新的集合
        repository.initRepository();

        //插入新的文档
        repository.save(documents);
    }


    /**
     * 根据条件检索文档
     *
     * @param name
     * @param condition
     * @return
     * @throws IOException
     */
    @Override
    public List<Document> search(String name, QueryCondition condition) throws IOException {

        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }
        return repository.search(condition);
    }

    /**
     * 根据条件检索文档
     *
     * @param name
     * @param query
     * @return
     * @throws IOException
     */
    @Override
    public List<Document> search(String name, String query) throws IOException {
        MilvusRepository repository = getMilvusRepositoryByName(name);
        if(null == repository){
            throw new RuntimeException("不存在对应的milvus 向量数据库实例，请重试");
        }
        return repository.search(query);
    }
}
