package org.int13h.plink.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.hc.core5.http.ContentType;
import org.int13h.plink.server.HttpResponse;
import org.int13h.plink.server.HttpStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NettyHttpResponse extends DefaultFullHttpResponse implements HttpResponse {

    private final ChannelHandlerContext ctx;

    private ContentType contentType;

    public NettyHttpResponse(ChannelHandlerContext ctx, HttpHeaders headers) {
        this(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, ctx);
        headers().setAll(headers);
    }

    public NettyHttpResponse(HttpVersion version, HttpResponseStatus status, ChannelHandlerContext ctx) {
        super(version, status);
        this.ctx = ctx;
    }

    @Override
    public void status(HttpStatus status) {
        setStatus(HttpResponseStatus.valueOf(status.getCode()));
    }

    public void contentType(ContentType contentType) {
        checkContentType();
        this.contentType = contentType;
    }

    public void contentType(String contentType, Charset charset) {
        checkContentType();
        this.contentType = ContentType.create(contentType, charset);
    }

    private void checkContentType() {
        if(contentType != null) {
            throw new IllegalStateException("Attempted to reset content-type");
        }
    }

    @Override
    public void write(CharSequence cs) {
        content().writeCharSequence(cs, StandardCharsets.UTF_8);
    }

    @Override
    public void html(CharSequence cs) {
        checkContentType();
        contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
        content().writeCharSequence(cs, StandardCharsets.UTF_8);
    }

    @Override
    public void text(CharSequence cs) {
        checkContentType();
        contentType = ContentType.create("text/plain", StandardCharsets.UTF_8);
        content().writeCharSequence(cs, StandardCharsets.UTF_8);
    }

    void finish() {
        headers().set("Content-Type", contentType == null ? "text/plain" : contentType);
        headers().set("Content-Length", content().readableBytes());
    }
}
