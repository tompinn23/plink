package org.int13h.plink;

import org.int13h.plink.server.HttpRouter;
import org.int13h.plink.server.netty.NettyHttpServer;

public class Main {

    public static void main(String[] args) throws Exception {

        HttpRouter router = new HttpRouter((req, res) -> {
            res.write("<h1>404 Not Found</h1>");
        },
        (req, res) -> {
        });

        router.GET("/hello", (req, res) ->  {
           res.text("Hello World!!");
        });

        router.GET("/why/:yo", (req, res) -> {
            res.text(String.format("Hello %s", req.param("yo")));
        });

        router.GET("/static/*path", (req, res) -> {
            System.out.println("Static World");
        });

        new NettyHttpServer(40124, router).run();
    }
}
