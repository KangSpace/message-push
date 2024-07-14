package org.kangspace.messagepush.ws.gateway;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.kangspace.messagepush.core.hash.repository.HashRouterRepository;
import org.kangspace.messagepush.core.redis.RedisService;
import org.kangspace.messagepush.ws.gateway.hash.RedisHashRouterRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Redis一致性Hash路由数据处理测试
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MessagePushWsGatewayApplication.class)
public class RedisHashRouterRepositoryTest {
    @Resource
    private RedisService redisService;

    private HashRouterRepository hashRouterRepository;

    @Before
    public void init() {
        this.hashRouterRepository = new RedisHashRouterRepository(redisService, null);
    }

    @Test
    public void storeTest() {
        List<String> servers = Arrays.asList(
                "node1", "node2", "node3", "node4"
        );
        ConsistencyHashing hashRouter = new ConsistencyHashing(MessagePushConstants.NUMBER_OF_VIRTUAL_NODE, servers);
        hashRouterRepository.store(hashRouter);
        System.out.println(hashRouter);
        initTest();
        System.out.println("save end");
    }

    @Test
    public void initTest() {
        ConsistencyHashing hashRouter = hashRouterRepository.init();
        System.out.println(hashRouter);
        Assert.assertTrue("一致性Hash环数据为空", hashRouter != null);
    }
}
