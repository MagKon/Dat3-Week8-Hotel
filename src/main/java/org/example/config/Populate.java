package org.example.config;


import org.example.model.Hotel;
import org.example.model.Room;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Set;

public class Populate {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        Set<Room> calRooms = getCalRooms();
        Set<Room> hilRooms = getHilRooms();

        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Hotel california = new Hotel("Hotel California", "California", Hotel.HotelType.LUXURY);
            Hotel hilton = new Hotel("Hilton", "Copenhagen", Hotel.HotelType.STANDARD);
            california.setRooms(calRooms);
            hilton.setRooms(hilRooms);
            em.merge(california);
            em.merge(hilton);
            em.getTransaction().commit();
        }
    }

    @NotNull
    private static Set<Room> getCalRooms() {
        Room r100 = new Room(100, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r101 = new Room(101, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r102 = new Room(102, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r103 = new Room(103, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r104 = new Room(104, new BigDecimal(3200), Room.RoomType.DOUBLE, 30.0);
        Room r105 = new Room(105, new BigDecimal(4500), Room.RoomType.SUITE, 40.0);

        Room[] roomArray = {r100, r101, r102, r103, r104, r105};
        return Set.of(roomArray);
    }

    @NotNull
    private static Set<Room> getHilRooms() {
        Room r111 = new Room(111, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r112 = new Room(112, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r113 = new Room(113, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r114 = new Room(114, new BigDecimal(2520), Room.RoomType.DOUBLE, 30.0);
        Room r115 = new Room(115, new BigDecimal(3200), Room.RoomType.DOUBLE, 30.0);
        Room r116 = new Room(116, new BigDecimal(4500), Room.RoomType.SUITE, 40.0);

        Room[] roomArray = {r111, r112, r113, r114, r115, r116};
        return Set.of(roomArray);
    }
}
