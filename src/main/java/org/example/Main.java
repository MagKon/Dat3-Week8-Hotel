package org.example;

import io.javalin.Javalin;
import org.example.config.ApplicationConfig;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ApplicationConfig
                .startServer(
                        Javalin.create(),
                        Integer.parseInt(ApplicationConfig.getProperty("javalin.port")));
    }
}