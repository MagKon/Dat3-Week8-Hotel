package org.example.routes;

import org.example.controller.impl.HotelController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class HotelRoute {

    private final HotelController hotelController = new HotelController();

    protected EndpointGroup getRoutes() {

        return () -> {
            path("/hotels", () -> {
                post("/", hotelController::create);
                get("/", hotelController::readAll);
                get("/{id}", hotelController::read);
                put("/{id}", hotelController::update);
                delete("/{id}", hotelController::delete);
            });
        };
    }
}
