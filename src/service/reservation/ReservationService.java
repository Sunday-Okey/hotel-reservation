package service.reservation;

import model.customer.Customer;
import model.reservation.Reservation;
import model.room.IRoom;

import java.util.Calendar;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationService {

    private static final ReservationService SINGLETON = new ReservationService();
    private static final int RECOMMENDED_ROOMS_DEFAULT_PLUS_DAYS = 7;

    private final Map<String, IRoom> rooms = new HashMap<>();
    private final Map<String, Collection<Reservation>> reservations = new HashMap<>();

    private ReservationService() {}

    public static ReservationService getSingleton() {
        return SINGLETON;
    }

    public void addRoom(final IRoom room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public IRoom getARoom(final String roomNumber) {
        return rooms.get(roomNumber);
    }

    public Collection<IRoom> getAllRooms() {
        return rooms.values();
    }

    public Reservation reserveARoom(final Customer customer, final IRoom room,
                                    final Date checkInDate, final Date checkOutDate) {
        final Reservation reservation = new Reservation(customer, room, checkInDate, checkOutDate);

        final Collection<Reservation> customerReservations = getCustomersReservation(customer);
        customerReservations.add(reservation);

        reservations.put(customer.getEmail(), customerReservations);

        return reservation;
    }

    public Collection<IRoom> findRooms(final Date checkInDate, final Date checkOutDate) {
        Collection<IRoom> availableRooms = findAvailableRooms(checkInDate, checkOutDate);

        if (availableRooms.isEmpty()) {
            return findAvailableRooms(addDefaultPlusDays(checkInDate), addDefaultPlusDays(checkOutDate));
        }

        return availableRooms;
    }

    private Collection<IRoom> findAvailableRooms(final Date checkInDate, final Date checkOutDate) {
        Collection<Reservation> allReservations = getAllReservations();

        Collection<IRoom> notAvailableRooms = allReservations.stream()
                .filter(reservation -> reservationOverlaps(reservation, checkInDate, checkOutDate))
                .map(Reservation::getRoom).collect(Collectors.toList());

        return rooms.values().stream().filter(room -> notAvailableRooms.stream()
                .noneMatch(notAvailableRoom -> notAvailableRoom.getRoomNumber().equals(room.getRoomNumber())))
                .collect(Collectors.toList());
    }

    private Date addDefaultPlusDays(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, RECOMMENDED_ROOMS_DEFAULT_PLUS_DAYS);

        return calendar.getTime();
    }

    private boolean reservationOverlaps(final Reservation reservation, final Date checkInDate,
                                        final Date checkOutDate){
        return checkInDate.before(reservation.getCheckOutDate())
                && checkOutDate.after(reservation.getCheckInDate());
    }

    public Collection<Reservation> getCustomersReservation(Customer customer) {
        return reservations.get(customer.getEmail());
    }

    public void printAllReservation() {
        getAllReservations().forEach(reservation -> System.out.println(reservation + "\n"));
    }

    private Collection<Reservation> getAllReservations() {
        Collection<Reservation> allReservations = new LinkedList<>();
        reservations.values().forEach(allReservations::addAll);
        return allReservations;
    }
}