package org.example;

import org.example.config.ApplicationConfig;
import org.example.controller.impl.ExceptionController;
import org.example.exception.ApiException;
import org.example.exception.Message;
import org.example.routes.Routes;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Routes routes = new Routes();
    private static final Javalin app = Javalin.create();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final ExceptionController exceptionController = new ExceptionController();
    private static int count = 0;

    private static void requestInfoHandler(Context ctx) {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        ctx.attribute("requestInfo", requestInfo);
    }


    public static void main(String[] args) {

        app.before(Main::requestInfoHandler);
        app.updateConfig(ApplicationConfig::configuration);

        app.routes(routes.getRoutes(app));

        app.after(ctx -> LOGGER.info(" Request {} - {} was handled with status code {}", count++, ctx.attribute("requestInfo"), ctx.status()));

        app.error(400, ctx -> {
            ctx.status(400);
            ctx.json(new Message(400, "Bad Request - " + ctx.req().getRequestURI()));
        });

        app.exception(ApiException.class, exceptionController::apiExceptionHandler);
        app.exception(Exception.class, exceptionController::exceptionHandler);

        app.start(7071);
    }
}