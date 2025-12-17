package com.dustin.ai.api.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.rag.repository.MilvusRepository;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

/**
 * Milvus 向量数据库配置类
 */
@Condition(onExpression = "${solon.ai.repo.milvus.status} == true")
@Configuration
public class MilvusRepositoryConfig {


    @Inject("${solon.ai.repo.milvus.status}")
    boolean status;


    /**
     * 默认的dbName 为 solon_ai
     * @return
     */
   @Bean("solonConnect")
   public ConnectConfig repositoryConfig(@Inject("${solon.ai.repo.milvus.url}") String uri) {
       if(status == true){
           return ConnectConfig.builder().uri(uri).build();
       }
       return null;
    }

    /**
     * 默认的实例MilvusRepository 集合为solon_ai
     * @param connectConfig
     * @return
     */
    @Bean("solonRepository")
    public MilvusRepository initMilvusRepository(@Inject("coscoEmbeddingModel") EmbeddingModel embeddingModel,
                                                  @Inject("solonConnect") ConnectConfig connectConfig) {
        if(status == true){
            return MilvusRepository.builder(embeddingModel, new MilvusClientV2(connectConfig)).build();
        }
        return null;
    }

    @Bean("aRepository")
    public MilvusRepository initaMilvusRepository(@Inject("coscoEmbeddingModel") EmbeddingModel embeddingModel,
                                                  @Inject("solonConnect") ConnectConfig connectConfig) {
        if(status == false){
            return null;
        }
        return MilvusRepository.builder(embeddingModel, new MilvusClientV2(connectConfig)).collectionName("sss").build();
    }

    @Bean("basicMilvus")
    public MilvusRepository initBasicMilvusRepository(@Inject("coscoEmbeddingModel") EmbeddingModel embeddingModel,
                                                      @Inject("solonConnect") ConnectConfig connectConfig) {
        return MilvusRepository.builder(embeddingModel, new MilvusClientV2(connectConfig)).collectionName("basic").build();
    }

}
