package org.int13h.plink;

import org.int13h.plink.router.Router;
import org.int13h.plink.server.HttpRequest;
import org.int13h.plink.server.HttpResponse;
import org.int13h.plink.server.HttpRouter;
import org.int13h.plink.server.NettyHttpServer;

import java.util.function.BiConsumer;

public class Main {

    public static void main(String[] args) throws Exception {

        HttpRouter router = new HttpRouter((req, res) -> {
        },
        (req, res) -> {
        });

        router.GET("/hello", (req, res) ->  {
           System.out.println("Hello World");
        });

        router.GET("/why/:yo/:yell", (req, res) -> {
            System.out.println("Yo World!");
        });

        router.GET("/static/*path", (req, res) -> {
            System.out.println("Static World");
        });

        new NettyHttpServer(40124, router).run();
    }
}
