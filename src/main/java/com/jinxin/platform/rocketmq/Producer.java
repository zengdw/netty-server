package com.jinxin.platform.rocketmq;

import com.jinxin.platform.utils.YamlConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/20 15:35
 */
@Slf4j
public class Producer {
    public static DefaultMQProducer p;

    static {
        // 实例化消息生产者Producer
        p = new DefaultMQProducer("group-1");
        String namesrvAddr = YamlConfig.getValue("rocketmq.nameserverAddr");
        p.setNamesrvAddr(namesrvAddr);
        try {
            p.start();
            log.info("===>rocketmq启动成功");
        } catch (MQClientException e) {
            log.error("===>rocketmq启动异常", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("===>rocketmq关闭");
            p.shutdown();
        }));
    }
}
