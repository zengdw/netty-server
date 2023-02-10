package com.jinxin.platform.netty.handler;

import com.jinxin.platform.message.MessageHandlerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/19 14:35
 */
@Slf4j
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private String msgBody;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        MDC.put("userId", ctx.channel().attr(AttributeKey.<String>valueOf("userId")).get());

        msgBody = frame.text();
        log.debug("===>接收消息:{}", msgBody);

        MessageHandlerFactory.execute(ctx, msgBody);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("===>消息处理异常, 消息体: \n " + msgBody, cause);
        ctx.writeAndFlush(new TextWebSocketFrame("{'msgType': 'error', 'msgBody': " + msgBody + ", 'msg': " + cause.getMessage() + "}"));
    }

}
