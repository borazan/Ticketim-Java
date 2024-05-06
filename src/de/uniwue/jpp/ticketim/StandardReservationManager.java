package de.uniwue.jpp.ticketim;

import de.uniwue.jpp.ticketim.base.Reservation;
import de.uniwue.jpp.ticketim.base.ReservationManager;
import de.uniwue.jpp.ticketim.base.SeatState;

import java.util.*;

public class StandardReservationManager implements ReservationManager {

    int id;
    Event event;
    Set<Reservation> reservations;


    public StandardReservationManager(Event event) {
        if (event == null) {
            throw new NullPointerException("event is null!");
        }
        this.id = IdManager.getId();
        this.event = event;
        this.event.manager = this;
        this.reservations = new HashSet<>();
    }


    public Optional<Integer> requestReservation(String blockName, int row, int numberOfSeats, int now) {
        if (!this.event.getBlocks().contains(blockName) || this.event.getNumberOfAvailableSeatsOfBlock(blockName) < numberOfSeats || row >= this.event.getNumberOfRows(blockName)) {
            return Optional.empty();
        }
        int seatsToReserve = numberOfSeats;
        Set<Integer> IDsToReserve = new HashSet<>();
        int numberOfRows = this.event.getNumberOfRows(blockName);
        for (int i = row; i < numberOfRows; i++) {
            for (int id : this.event.getAvailableSeatIDsOfRow(blockName, i)) {
                if (seatsToReserve == 0) {
                    break;
                }
                IDsToReserve.add(id);
                seatsToReserve--;
            }
        }
        if (seatsToReserve == 0) {
            for (int seatID : IDsToReserve) {
                this.event.setState(seatID, SeatState.RESERVED);
            }
            Reservation reservation = new Reservation(now, IDsToReserve);
            reservations.add(reservation);
            return Optional.of(reservation.getID());
        }
        return Optional.empty();
    }

    public Optional<Integer> requestReservation(Set<Integer> seatIDs, int now) {
        for (int id : seatIDs) {
            if (this.event.getSeatInformation(id).isEmpty()) return Optional.empty();
            if (this.event.getState(id).get() == SeatState.RESERVED || this.event.getState(id).get() == SeatState.UNAVAILABLE)
                return Optional.empty();
        }
        for (int id : seatIDs) {
            this.event.setState(id, SeatState.RESERVED);
        }
        Reservation reservation = new Reservation(now, seatIDs);
        reservations.add(reservation);
        return Optional.of(reservation.getID());
    }

    public Optional<Set<Integer>> getSeatIDs(int reservationID) {
        for (Reservation reservation : reservations) {
            if (reservation.getID() == reservationID) {
                return Optional.of(reservation.getSeatIDs());
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getTimestamp(int reservationID) {
        for (Reservation reservation : reservations) {
            if (reservation.getID() == reservationID) {
                return Optional.of(reservation.getTimestamp());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getReservationInformation(int reservationID) {
        for (Reservation reservation : reservations) {
            if (reservation.getID() == reservationID) {
                String returnString = "";
                for (Integer seatID : reservation.getSeatIDs()){
                    if (returnString.equals("")) returnString = event.getSeatInformation(seatID).get();
                    else returnString = returnString + "\n" + event.getSeatInformation(seatID).get();
                }
                return Optional.of(returnString);
            }
        }
        return Optional.empty();
    }

    public Optional<Double> calculatePrice(int reservationID) {
        double totalPrice = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getID() == reservationID) {
                for (int seatID : reservation.getSeatIDs()) {
                    totalPrice += this.event.getPrice(seatID).get();
                }
                return Optional.of(totalPrice);
            }
        }
        return Optional.empty();
    }

    public Set<Integer> getValidReservationIDs() {
        Set<Integer> reservationIDs = new HashSet<>();
        for (Reservation reservation : reservations) {
            reservationIDs.add(reservation.getID());
        }
        return reservationIDs;
    }

    public boolean removeReservation(int reservationID) {
        for (Reservation reservation : reservations) {
            if (reservation.getID() == reservationID) {
                for (int seatID : reservation.getSeatIDs()) {
                    this.event.setState(seatID, SeatState.AVAILABLE);
                }
                this.reservations.remove(reservation);
                return true;
            }
        }
        return false;
    }

    public Optional<Set<Integer>> buy(int reservationID) {
        for (Reservation reservation : reservations) {
            if (reservation.getID() == reservationID) {
                for (int seatID : reservation.getSeatIDs()) {
                    this.event.setState(seatID, SeatState.UNAVAILABLE);
                }
                this.reservations.remove(reservation);
                return Optional.of(reservation.getSeatIDs());
            }
        }
        return Optional.empty();
    }

    public long updateAllReservations(int now) {
        long counter = 0;
        Set<Reservation> copyreservations = new HashSet<>(reservations);
        for (Reservation reservation : copyreservations) {
            if (now - reservation.getTimestamp() > 900) {
                for (int seatID : reservation.getSeatIDs()) {
                    this.event.setState(seatID, SeatState.AVAILABLE);
                }
                this.reservations.remove(reservation);
                counter++;
            }
        }
        return counter;
    }
}
