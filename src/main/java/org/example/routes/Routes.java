package org.example.routes;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private final HotelRoute hotelRoute = new HotelRoute();
    private final RoomRoute roomRoute = new RoomRoute();

    public EndpointGroup getRoutes(Javalin app) {
        return () -> {
            app.routes(() -> {
                path("/", hotelRoute.getRoutes());
                path("/", roomRoute.getRoutes());
            });
        };
    }
}
