package de.uniwue.jpp.ticketim.base;

import de.uniwue.jpp.ticketim.IdManager;

import java.util.Objects;
import java.util.Set;

public class Reservation {
    private final int timestamp;
    private final Set<Integer> seatIDs;
    private final int ID;

    public Reservation(int timestamp, Set<Integer> seatIDs){
        this.timestamp = timestamp;
        this.seatIDs = seatIDs;
        this.ID = IdManager.getId();
    }

    public int getTimestamp() {
        return timestamp;
    }

    public Set<Integer> getSeatIDs() {
        return seatIDs;
    }
    public int getID(){ return this.ID;};

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return timestamp == that.timestamp && Objects.equals(seatIDs, that.seatIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, seatIDs);
    }
}
