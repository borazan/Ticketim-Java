package de.uniwue.jpp.ticketim;

import de.uniwue.jpp.ticketim.base.Reservation;
import de.uniwue.jpp.ticketim.base.ReservationManager;

import java.util.*;

public class AnalyzingReservationManager implements ReservationManager {
    private ReservationManager manager;
    private long numberOfCanceledReservations;
    private int numberOfSuccessfulReservations;
    private int numberOfTotalSeatsReserved;
    private int numberOfReservationsSold;
    private int totalPriceOfSoldReservations;
    public AnalyzingReservationManager(ReservationManager standard){
        if (standard == null) throw new NullPointerException();
        this.manager = standard;
    }


    @Override
    public Optional<Integer> requestReservation(String blockName, int row, int numberOfSeats, int now) {
        Optional<Integer> temp = manager.requestReservation(blockName, row, numberOfSeats, now);
        if (temp.isEmpty()){
            return Optional.empty();
        }
        numberOfTotalSeatsReserved += numberOfSeats;
        numberOfSuccessfulReservations++;
        return temp;
    }

    @Override
    public Optional<Integer> requestReservation(Set<Integer> seatIDs, int now) {
        Optional<Integer> temp = manager.requestReservation(seatIDs,now);
        if (temp.isEmpty()){
            return Optional.empty();
        }
        numberOfTotalSeatsReserved += seatIDs.size();
        numberOfSuccessfulReservations++;
        return temp;
    }

    @Override
    public Optional<Set<Integer>> getSeatIDs(int reservationID) {
        return manager.getSeatIDs(reservationID);
    }

    @Override
    public Optional<Integer> getTimestamp(int reservationID) {
        return manager.getTimestamp(reservationID);
    }

    @Override
    public Optional<String> getReservationInformation(int reservationID) {
        return manager.getReservationInformation(reservationID);
    }

    @Override
    public Optional<Double> calculatePrice(int reservationID) {
        return manager.calculatePrice(reservationID);
    }

    @Override
    public Set<Integer> getValidReservationIDs() {
        return manager.getValidReservationIDs();
    }

    @Override
    public boolean removeReservation(int reservationID) {
        if (manager.removeReservation(reservationID)){
            //this.numberOfCanceledReservations++;
            return true;
        }
        return false;
    }

    @Override
    public Optional<Set<Integer>> buy(int reservationID) {
        Optional<Set<Integer>> seatsSold = manager.buy(reservationID);
        if (seatsSold.isEmpty()){
            return Optional.empty();
        }
        this.numberOfReservationsSold++;
        this.numberOfSuccessfulReservations++;
        this.totalPriceOfSoldReservations += manager.calculatePrice(reservationID).get();
        return seatsSold;
    }

    @Override
    public long updateAllReservations(int now) {
        long canceledReservations = manager.updateAllReservations(now);
        this.numberOfCanceledReservations += canceledReservations;
        return canceledReservations;
    }

    public long getNumberOfSuccessfulReservations(){
        return this.numberOfSuccessfulReservations;
    }

    public double getAverageNumberOfSeatsOfOneReservation(){
        if (numberOfTotalSeatsReserved == 0) return 0;
        return (double)this.numberOfTotalSeatsReserved / (double)this.numberOfSuccessfulReservations;
    }

    public long getNumberOfCancelledReservations(){
        return this.numberOfCanceledReservations;
    }

    public double getAveragePriceOfOneReservation(){
        if (numberOfReservationsSold == 0) return 0;
        return (double)this.totalPriceOfSoldReservations / (double)this.numberOfReservationsSold;
    }
}
