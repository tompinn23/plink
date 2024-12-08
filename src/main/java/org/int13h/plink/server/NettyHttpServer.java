package org.int13h.plink.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.int13h.plink.router.Router;

import java.util.function.BiConsumer;

public class NettyHttpServer {

    private final EventLoopGroup boss;
    private final EventLoopGroup worker;

    private final ServerBootstrap bootstrap;

    private final int port;

    public NettyHttpServer(int port, HttpRouter router) {
        this.port = port;

        this.boss = new NioEventLoopGroup();
        this.worker = new NioEventLoopGroup();
        this.bootstrap = new ServerBootstrap();
        this.bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(1048576));
                        pipeline.addLast(new HttpServerExpectContinueHandler());
                        pipeline.addLast(new CustomHttpServerHandler(router));
                    }
                });
    }

    public void run() throws Exception {
        try {
            Channel ch = bootstrap.bind(port).sync().channel();

            ch.closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private static class CustomHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private final Router<BiConsumer<HttpRequest, HttpResponse>> router;

        public CustomHttpServerHandler(Router<BiConsumer<HttpRequest, HttpResponse>> router) {
            this.router = router;
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
            var req = new NettyHttpRequest(msg);
            var handler = router.handler(req.method(), req.path());
            req.setHandler(handler);

            handler.handler().accept(req, new HttpResponse() {
            });
        }

    }
}
