package com.jinxin.platform.message;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/27 14:35
 */
@Slf4j
public class MessageHandlerFactory {
    private static ServiceLoader<MessageHandler> LOADER;

    static {
        LOADER = ServiceLoader.load(MessageHandler.class);
    }

    public static void execute(ChannelHandlerContext ctx, String msg) throws Exception {
        JSONObject msgObj = JSON.parseObject(msg);
        String type = msgObj.getString("msgType");
        if ("reload".equals(type)) {
            log.debug("reload start");
            LOADER = ServiceLoader.load(MessageHandler.class, new URLClassLoader(getExtendJarUrl()));
            Map<String, Object> resMsg = new HashMap<>();
            resMsg.put("msg", "reload success");
            resMsg.put("handler", StringUtils.join(LOADER, "\n"));
            ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(resMsg)));
        } else {
            for (MessageHandler handler : LOADER) {
                if (handler.support(type)) {
                    handler.execute(ctx, msg);
                }
            }
        }
    }

    private static URL[] getExtendJarUrl() throws MalformedURLException {
        String projectFolder = System.getProperty("user.dir");
        File extendJarFolder = new File(projectFolder, "lib");
        log.debug("extendFolder Path: {}", extendJarFolder.getAbsolutePath());
        File[] jarList = extendJarFolder.listFiles();
        if (extendJarFolder.exists() && null != jarList) {
            URL[] jarUrls = new URL[jarList.length];
            for (int i = 0; i < jarList.length; i++) {
                jarUrls[i] = jarList[i].toURI().toURL();
            }
            return jarUrls;
        }
        return new URL[0];
    }
}
