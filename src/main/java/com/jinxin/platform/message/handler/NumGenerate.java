package com.jinxin.platform.message.handler;

import com.alibaba.fastjson2.JSON;
import com.jinxin.platform.message.MessageHandler;
import com.jinxin.platform.utils.IdWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 唯一序号生成
 *
 * @author zengd
 * @version 1.0
 * @date 2022/10/9 16:29
 */
public class NumGenerate implements MessageHandler {
    @Override
    public void execute(ChannelHandlerContext ctx, String msg) {
        List<Long> numList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            long id = IdWorker.getInstance().nextId();
            numList.add(id);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("msgType", "numGen");
        map.put("list", numList);
        ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(map)));
    }

    @Override
    public boolean support(String type) {
        return "numGen".equalsIgnoreCase(type);
    }
}
