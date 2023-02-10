package com.jinxin.platform.message;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/26 16:06
 */
public interface MessageHandler {

    void execute(ChannelHandlerContext ctx, String msg) throws Exception;

    boolean support(String type);

}
