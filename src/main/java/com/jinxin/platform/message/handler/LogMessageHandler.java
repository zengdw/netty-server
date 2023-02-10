package com.jinxin.platform.message.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jinxin.platform.log.LogUtils;
import com.jinxin.platform.message.MessageHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 日志消息处理
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/26 16:12
 */
public class LogMessageHandler implements MessageHandler {
    @Override
    public void execute(ChannelHandlerContext ctx, String msg) {
        JSONObject msgObj = JSON.parseObject(msg);
        LogUtils.logLevel(msgObj.getIntValue("msg"));
    }

    @Override
    public boolean support(String type) {
        return "log".equalsIgnoreCase(type);
    }
}
