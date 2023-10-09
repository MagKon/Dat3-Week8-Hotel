package org.example.controller.impl;

import org.example.config.HibernateConfig;
import org.example.controller.IController;
import org.example.dao.impl.RoomDao;
import org.example.dto.HotelDto;
import org.example.dto.RoomDto;
import org.example.exception.Message;
import org.example.model.Hotel;
import org.example.model.Room;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.function.BiFunction;

public class RoomController implements IController<Room, Integer> {

    private RoomDao dao;

    public RoomController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig();
        this.dao = RoomDao.getInstance(emf);
    }

    @Override
    public void read(Context ctx) {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        // entity
        Room room = dao.read(id);
        // dto
        RoomDto roomDto = new RoomDto(room);
        // response
        ctx.res().setStatus(200);
        ctx.json(roomDto, RoomDto.class);

    }

    @Override
    public void readAll(Context ctx) {
        // entity
        List<Room> rooms = dao.readAll();
        // dto
        List<RoomDto> roomDtos = RoomDto.toRoomDTOList(rooms);
        // response
        ctx.res().setStatus(200);
        ctx.json(roomDtos, RoomDto.class);

    }

    @Override
    public void create(Context ctx) {
        // request
        Room jsonRequest = validateEntity(ctx);

        int hotelId = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        Boolean hasRoom = validateHotelRoomNumber.apply(jsonRequest.getRoomNumber(), hotelId);

        if (hasRoom) {
            ctx.res().setStatus(400);
            ctx.json(new Message(400, "Room number already in use by hotel"));
            return;
        }

        // entity
        Hotel hotel = dao.addRoomToHotel(hotelId, jsonRequest);
        // dto
        HotelDto hotelDto = new HotelDto(hotel);
        // response
        ctx.res().setStatus(201);
        ctx.json(hotelDto, HotelDto.class);
    }

    @Override
    public void update(Context ctx) {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        // entity
        Room update = dao.update(id, validateEntity(ctx));
        // dto
        RoomDto roomDto = new RoomDto(update);
        // response
        ctx.res().setStatus(200);
        ctx.json(roomDto, RoomDto.class);
    }

    @Override
    public void delete(Context ctx) {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        // entity
        dao.delete(id);
        // response
        ctx.res().setStatus(204);
    }

    @Override
    public boolean validatePrimaryKey(Integer integer) {return dao.validatePrimaryKey(integer);}

    // Checks if the room number is already in use by the hotel
    BiFunction<Integer, Integer, Boolean> validateHotelRoomNumber = (roomNumber, hotelId) -> dao.validateHotelRoomNumber(roomNumber, hotelId);

    @Override
    public Room validateEntity(Context ctx) {
        return ctx.bodyValidator(Room.class)
                .check(r -> r.getRoomNumber() != null && r.getRoomNumber() > 0, "Not a valid room number")
                .check(r -> r.getRoomType() != null, "Not a valid room type")
                .check(r -> r.getRoomPrice() != null , "Not a valid price")
                .get();
    }
}
