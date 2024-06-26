package org.example.routes;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;
import org.example.controller.impl.ExceptionController;
import org.example.exception.ApiException;
import org.example.exception.AuthorizationException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private final ExceptionController exceptionController = new ExceptionController();
    private int count = 0;

    private final HotelRoute hotelRoute = new HotelRoute();
    private final RoomRoute roomRoute = new RoomRoute();
    private final UserRoutes userRoutes = new UserRoutes();

    private final Logger LOGGER = LoggerFactory.getLogger(Routes.class);

    private void requestInfoHandler(Context ctx) {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        ctx.attribute("requestInfo", requestInfo);
    }

    public EndpointGroup getRoutes(Javalin app) {
        return () -> {
            app.before(this::requestInfoHandler);

            app.routes(() -> {
                path("/", userRoutes.getRoutes());
                path("/", hotelRoute.getRoutes());
                path("/", roomRoute.getRoutes());
            });

            app.after(ctx -> LOGGER.info(" Request {} - {} was handled with status code {}", count++, ctx.attribute("requestInfo"), ctx.status()));

            app.exception(ConstraintViolationException.class, exceptionController::constraintViolationExceptionHandler);
            app.exception(ValidationException.class, exceptionController::validationExceptionHandler);
            app.exception(ApiException.class, exceptionController::apiExceptionHandler);
            app.exception(AuthorizationException.class, exceptionController::exceptionHandlerNotAuthorized);
            app.exception(Exception.class, exceptionController::exceptionHandler);

        };
    }
}