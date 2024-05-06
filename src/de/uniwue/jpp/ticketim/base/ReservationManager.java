package de.uniwue.jpp.ticketim.base;

import java.util.Optional;
import java.util.Set;

public interface ReservationManager {

    public Optional<Integer> requestReservation(String blockName, int row, int numberOfSeats, int now);
    public Optional<Integer> requestReservation(Set<Integer> seatIDs, int now);
    public Optional<Set<Integer>> getSeatIDs(int reservationID);
    public Optional<Integer> getTimestamp(int reservationID);
    public Optional<String> getReservationInformation(int reservationID);
    public Optional<Double> calculatePrice(int reservationID);
    public Set<Integer> getValidReservationIDs();
    public boolean removeReservation(int reservationID);
    public Optional<Set<Integer>> buy(int reservationID);
    public long updateAllReservations(int now);

}
