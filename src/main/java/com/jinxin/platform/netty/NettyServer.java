package com.jinxin.platform.netty;

import com.jinxin.platform.netty.handler.HeartBeatHandler;
import com.jinxin.platform.netty.handler.LoginCheckHandler;
import com.jinxin.platform.netty.handler.TextWebSocketFrameHandler;
import com.jinxin.platform.utils.YamlConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengd
 * @version 1.0
 * @date 2022/9/19 10:44
 */
@Slf4j
public class NettyServer {
    private static final String WEBSOCKET_PATH = "/ws";

    public static void main(String[] args) {
        EventLoopGroup boss = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        EventLoopGroup work = new NioEventLoopGroup(new DefaultThreadFactory("work"));

        LoginCheckHandler loginCheckHandler = new LoginCheckHandler(false);
        HeartBeatHandler heartBeatHandler = new HeartBeatHandler();

        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new IdleStateHandler(0, 0, 60 * 5));
                            // HttpRequestDecoder和HttpResponseEncoder的一个组合，针对http协议进行编解码
                            p.addLast(new HttpServerCodec());
                            // 分块向客户端写数据，防止发送大文件时导致内存溢出， channel.write(new ChunkedFile(new File("bigFile.mkv")))
                            //p.addLast(new ChunkedWriteHandler());
                            /*
                                将HttpMessage和HttpContents聚合到一个完成的 FullHttpRequest或FullHttpResponse中,具体是FullHttpRequest对象还是FullHttpResponse对象取决于是请求还是响应
                                需要放到HttpServerCodec这个处理器后面
                             */
                            p.addLast(new HttpObjectAggregator(65536));
                            p.addLast(loginCheckHandler);
                            // webSocket 数据压缩扩展，当添加这个的时候WebSocketServerProtocolHandler的第三个参数需要设置成true
                            p.addLast(new WebSocketServerCompressionHandler());
                            /*
                                服务器端向外暴露的 web socket 端点，当客户端传递比较大的对象时，maxFrameSize参数的值需要调大
                             */
                            p.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, 65536, true, true));
                            p.addLast(heartBeatHandler);
                            p.addLast(new TextWebSocketFrameHandler());
                        }
                    });
            int port = Integer.parseInt(YamlConfig.getValue("netty.port"));
            Channel ch = b.bind(port).sync().channel();

            log.info("===>服务启动成功,端口:{}", port);

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("===>服务启动异常", e);
        } finally {
            log.info("===>Netty服务关闭");
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

    static class DefaultThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public DefaultThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            AtomicInteger poolNumber = new AtomicInteger(1);
            namePrefix = prefix + "-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
