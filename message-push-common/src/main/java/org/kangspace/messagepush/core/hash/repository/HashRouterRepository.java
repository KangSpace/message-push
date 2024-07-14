package org.kangspace.messagepush.core.hash.repository;

import org.kangspace.messagepush.core.hash.ConsistencyHashing;

import java.util.List;

/**
 * 一致性Hash路由数据接口
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
public interface HashRouterRepository<T> {

    /**
     * 初始化一致性Hash数据
     *
     * @return {@link ConsistencyHashing}
     */
    ConsistencyHashing<T> init();

    /**
     * 获取一致性Hash数据
     *
     * @return
     */
    ConsistencyHashing<T> get();

    /**
     * 保存一致性Hash数据
     *
     * @param hashRouter ConsistencyHashing
     * @return boolean
     */
    boolean store(ConsistencyHashing<T> hashRouter);

    /**
     * rehash
     *
     * @param actualServices 最新的服务列表(ip:端口列表)
     * @return ConsistencyHashing
     */
    ConsistencyHashing<T> rehash(List<String> actualServices);

    /**
     * 检查Hash环数据是否和当前Server列表一致
     *
     * @param hashRouter     需要对比的一致性Hash环数据
     * @param actualServices 最新的服务列表(ip:端口列表)
     * @return Hash数据是否一致, 是否需要rehash, true:需要rehash,false:不需要
     */
    boolean compareHashData(ConsistencyHashing<T> hashRouter, List<String> actualServices);

    /**
     * 检查Hash换数据是否和当前Server列表一致,
     * 若不一致则rehash
     *
     * @return
     */
    ConsistencyHashing<T> compareHashDataAndRehash(ConsistencyHashing<T> hashRouter, List<String> actualServices);
}
