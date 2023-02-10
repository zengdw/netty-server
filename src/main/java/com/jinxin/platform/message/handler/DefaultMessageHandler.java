package com.jinxin.platform.message.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;

import java.util.Arrays;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/26 16:17
 */
@Slf4j
public class DefaultMessageHandler extends AbstractMessageHandler {

    private final String[] types = new String[]{"web", "backstage", "app", "applet", "node", "sys", "error"};

    @Override
    public void execute(ChannelHandlerContext ctx, String msg) throws Exception {
        Message message = propertySet(ctx, msg);

        send(ctx, message);
    }

    @Override
    public boolean support(String type) {
        return Arrays.asList(types).contains(type);
    }
}
