package com.jinxin.platform.message.handler;

import com.jinxin.platform.message.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 心跳消息处理
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/26 16:10
 */
public class PingMessageHandler implements MessageHandler {
    @Override
    public void execute(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush(new TextWebSocketFrame("{'msgType': 'pone'}"));
    }

    @Override
    public boolean support(String type) {
        return "ping".equalsIgnoreCase(type);
    }
}
