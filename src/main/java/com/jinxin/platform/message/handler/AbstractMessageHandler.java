package com.jinxin.platform.message.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jinxin.platform.message.MessageHandler;
import com.jinxin.platform.redis.RedisTemplate;
import com.jinxin.platform.rocketmq.Producer;
import com.jinxin.platform.utils.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/10/10 15:43
 */
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler {
    void send(ChannelHandlerContext ctx, Message message) throws Exception {
        setTokenEx(ctx);

        SendResult send = Producer.p.send(message);
        if (send.getSendStatus() == SendStatus.SEND_OK) {
            log.debug("===>消息转发成功");
        } else {
            throw new Exception("消息发送失败");
        }
    }

    void setTokenEx(ChannelHandlerContext ctx) {
        String userId = ctx.channel().attr(AttributeKey.<String>valueOf("userId")).get();
        String token = ctx.channel().attr(AttributeKey.<String>valueOf("token")).get();
        RedisTemplate.setEx(token, 30 * 60, userId);
    }

    Message propertySet(ChannelHandlerContext ctx, String msg) {
        JSONObject msgObj = JSON.parseObject(msg);
        String type = msgObj.getString("opeType");
        String topic = msgObj.getString("msgType");
        String userId = ctx.channel().attr(AttributeKey.<String>valueOf("userId")).get();
        msgObj.put("userId", userId);
        msgObj.put("time", System.currentTimeMillis());
        msgObj.put("ip", NettyUtil.getRemoteIp(ctx));

        return new Message(topic, type, JSON.toJSONString(msgObj).getBytes(StandardCharsets.UTF_8));
    }
}
