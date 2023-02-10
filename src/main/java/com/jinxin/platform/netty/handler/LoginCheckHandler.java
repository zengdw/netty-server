package com.jinxin.platform.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jinxin.platform.netty.Config;
import com.jinxin.platform.redis.RedisTemplate;
import com.jinxin.platform.rocketmq.Producer;
import com.jinxin.platform.utils.NettyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

/**
 * 登录验证handler
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/19 15:20
 */
@ChannelHandler.Sharable
@Slf4j
public class LoginCheckHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public LoginCheckHandler(boolean autoRelease) {
        super(autoRelease);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        MDC.put("userId", "");
        
        String uri = request.uri();
        log.debug("===>连接uri:{}", uri);

        String delimiter = "?";
        if (!uri.contains(delimiter)) {
            log.error("===>路径[{}]未发现参数token", uri);
            ctx.channel().close();
            return;
        }

        String token = uri.substring(uri.indexOf("?") + 1).split("=")[1];
        String userId = RedisTemplate.get(token);
        if (StringUtils.isBlank(userId)) {
            log.error("===>token[{}]验证失败", token);
            ctx.channel().close();
            return;
        }
        MDC.put("userId", userId);

        ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
        ctx.channel().attr(AttributeKey.valueOf("token")).set(token);
        ctx.channel().attr(AttributeKey.valueOf("isClear")).set(false);

        clearUser(userId);

        if (null == Config.con.get(userId)) {
            log.info("===>管道[{}]连接成功", ctx.channel().id().asLongText());
            sendMsg("login", userId, ctx);
            RedisTemplate.lpush("ONLINE_USERS", userId);
        }
        Config.con.put(userId, ctx.channel());

        // 传递消息给下一个handler
        ctx.fireChannelRead(request);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String userId = ctx.channel().attr(AttributeKey.<String>valueOf("userId")).get();
        Boolean isClear = ctx.channel().attr(AttributeKey.<Boolean>valueOf("isClear")).get();
        if (null != userId && !isClear) {
            log.info("===>管道[{}]断开连接", ctx.channel().id().asLongText());
            Config.con.remove(userId);
            RedisTemplate.lrem("ONLINE_USERS", userId);
            sendMsg("loginOut", userId, ctx);
        }
        super.channelInactive(ctx);
    }

    /**
     * 清除旧的连接
     *
     * @param userId 用户id
     */
    private void clearUser(String userId) {
        Channel channel = Config.con.get(userId);
        if (channel != null) {
            log.debug("===>清除旧的连接");
            channel.attr(AttributeKey.<Boolean>valueOf("isClear")).set(true);
            channel.close();
        }
    }

    /**
     * 发送登录/登出消息
     *
     * @param tag    消息标签
     * @param userId 用户id
     */
    private void sendMsg(String tag, String userId, ChannelHandlerContext ctx) throws Exception {
        JSONObject object = new JSONObject();
        object.put("userId", userId);
        object.put("time", System.currentTimeMillis());
        object.put("remark", "login".equals(tag) ? "登录成功" : "退出登录");
        object.put("opeType", tag);
        object.put("ip", NettyUtil.getRemoteIp(ctx));

        String msg = JSON.toJSONString(object);

        Message message = new Message("sys", tag, msg.getBytes(StandardCharsets.UTF_8));
        SendResult send = Producer.p.send(message);
        if (send.getSendStatus() == SendStatus.SEND_OK) {
            log.debug("===>消息转发成功:{}", msg);
        } else {
            throw new Exception("消息发送失败");
        }
    }
}
