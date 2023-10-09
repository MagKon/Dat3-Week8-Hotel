package org.example.controller.impl;

import org.example.config.HibernateConfig;
import org.example.controller.IController;
import org.example.dao.impl.HotelDao;
import org.example.dto.HotelDto;
import org.example.model.Hotel;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class HotelController implements IController<Hotel, Integer> {

    private final HotelDao dao;

    public HotelController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = HotelDao.getInstance(emf);
    }

    @Override
    public void read(Context ctx)  {
        // request
        int id = ctx.pathParamAsClass("id", Integer.class).check(this::validatePrimaryKey, "Not a valid id").get();
        // entity
        Hotel hotel = dao.read(id);
        // dto
        HotelDto hotelDto = new HotelDto(hotel);
        // response
        ctx.res().setStatus(200);
        ctx.json(hotelDto, HotelDto.class);
    }

    @Override
    public void readAll(Context ctx) {
        // entity
        List<Hotel> hotels = dao.readAll();
        // dto
        List<HotelDto> hotelDtos = HotelDto.toHotelDTOList(hotels);
        // response
        ctx.res().setStatus(200);
        ctx.json(hotelDtos, HotelDto.class);
    }

    @Override
    public void create(Context ctx) {
        // request
        //Hotel jsonRequest = validateEntity(ctx);
        Hotel jsonRequest = ctx.bodyAsClass(Hotel.class);
        // entity
        Hotel hotel = dao.create(jsonRequest);
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
        Hotel update = dao.update(id, validateEntity(ctx));
        // dto
        HotelDto hotelDto = new HotelDto(update);
        // response
        ctx.res().setStatus(200);
        ctx.json(hotelDto, Hotel.class);
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
    public boolean validatePrimaryKey(Integer integer) {
        return dao.validatePrimaryKey(integer);
    }

    @Override
    public Hotel validateEntity(Context ctx) {
        return ctx.bodyValidator(Hotel.class)
                .check( h -> h.getHotelAddress() != null && !h.getHotelAddress().isEmpty(), "Hotel address must be set")
                .check( h -> h.getHotelName() != null && !h.getHotelName().isEmpty(), "Hotel name must be set")
                .check( h -> h.getHotelType() != null, "Hotel type must be set")
                .get();
    }

}
