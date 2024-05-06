package de.uniwue.jpp.ticketim;

import de.uniwue.jpp.ticketim.base.SeatInformation;
import de.uniwue.jpp.ticketim.base.SeatState;

import java.sql.Array;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Event {
    private String artistName;
    private String locationName;
    private LocalDate date;
    private Map<String, List<Integer>> availableSeats;
    private Map<String, Double> prices;
    private Map<Integer, SeatInformation> idMap;
    public StandardReservationManager manager;

    public Event(String artistName, String locationName, LocalDate date, Map<String, List<Integer>> availableSeats, Map<String, Double> prices) {
        if (artistName == null || locationName == null || date == null || availableSeats == null || prices == null) {
            throw new NullPointerException("One of the parameters is null in Event Constructor!");
        }
        if (!availableSeats.keySet().equals(prices.keySet())) {
            throw new IllegalArgumentException("Keysets for availableSeats and prices don't match!");
        }
        this.artistName = artistName;
        this.locationName = locationName;
        this.date = date;
        this.availableSeats = availableSeats;
        this.prices = prices;
        this.idMap = new HashMap<>();
        int id = 1;
        for (String block : availableSeats.keySet()) {
            List<Integer> rows = availableSeats.get(block);
            int row = 0;
            for (int seats_in_row : rows) {
                for (int i = 0; i < seats_in_row; i++) {
                    SeatInformation seat = new SeatInformation(block, row, i);
                    seat.setSeatID(id);
                    idMap.put(id, seat);
                    id++;
                }
                row++;
            }
        }
        this.manager = new StandardReservationManager(this);
    }


    public String getArtistName() {
        return this.artistName;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public LocalDate getDate() {
        return this.date;
    }
    public Set<String> getBlocks() {
        return this.availableSeats.keySet();
    }

    public int getNumberOfRows(String blockName) {
        if (this.availableSeats.containsKey(blockName)) return this.availableSeats.get(blockName).size();
        else return 0;
    }

    public int getNumberOfSeatsInRow(String blockName, int row) {
        if (this.availableSeats.containsKey(blockName) && row < this.availableSeats.get(blockName).size()) return this.availableSeats.get(blockName).get(row);
        else return 0;
    }

    public Optional<Integer> getSeatID(String blockName, int row, int number) {
        for (SeatInformation seat : idMap.values()) {
            if (seat.getBlockname() == blockName && seat.getRow() == row && seat.getNumber() == number) {
                return Optional.of(seat.getSeatID());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getSeatInformation(int seatID) {
        if (idMap.get(seatID) == null) {
            return Optional.empty();
        } else {
            SeatInformation seat = idMap.get(seatID);
            String block = seat.getBlockname();
            int row = seat.getRow();
            int number = seat.getNumber();
            return Optional.of("Block: " + block + "; Row: " + row + "; Number: " + number);
        }
    }

    public Optional<String> getBlockName(int seatID) {
        if (idMap.get(seatID) == null) {
            return Optional.empty();
        } else {
            return Optional.of(idMap.get(seatID).getBlockname());
        }
    }

    public Optional<Double> getPrice(int seatID) {
        if (idMap.get(seatID) == null) {
            return Optional.empty();
        } else {
            SeatInformation seat = idMap.get(seatID);
            String block = seat.getBlockname();
            return Optional.of(prices.get(block));
        }
    }

    public Optional<SeatState> getState(int seatID) {
        if (idMap.get(seatID) == null) {
            return Optional.empty();
        } else {
            SeatInformation seat = idMap.get(seatID);
            return Optional.of(seat.getState());
        }
    }

    public boolean setState(int seatID, SeatState state) {
        if (idMap.get(seatID) == null) {
            return false;
        } else {
            SeatInformation seat = idMap.get(seatID);
            seat.setState(state);
            return true;
        }
    }


    public Set<Integer> getSeatIDs() {
        return idMap.keySet();
    }

    public Set<Integer> getSeatIDsOfBlock(String blockname) {
        Set<Integer> IDs = new HashSet<>();
        for (SeatInformation seat : idMap.values()) {
            if (seat.getBlockname() == blockname) {
                IDs.add(seat.getSeatID());
            }
        }
        return IDs;
    }

    public List<Integer> getSeatIDsOfRow(String blockname, int row) {
        if (!availableSeats.containsKey(blockname)) {
            return new ArrayList<>(); //blockname unknown
        }
        if (row >= availableSeats.get(blockname).size()) {
            return new ArrayList<>(); //row too big
        }
        List<Integer> IDs = new ArrayList<>();
        for (SeatInformation seat : idMap.values()) {
            if (seat.getBlockname() == blockname && seat.getRow() == row) {
                IDs.add(seat.getSeatID());
            }
        }
        Collections.sort(IDs);
        return IDs;
    }


    public Set<Integer> getAvailableSeatIDs() {
        Set<Integer> IDs = new HashSet<>();
        for (SeatInformation seat : idMap.values()) {
            if (seat.getState() == SeatState.AVAILABLE) {
                IDs.add(seat.getSeatID());
            }
        }
        return IDs;
    }

    public Set<Integer> getAvailableSeatIDsOfBlock(String blockName) {
        Set<Integer> IDs = new HashSet<>();
        for (SeatInformation seat : idMap.values()) {
            if (seat.getBlockname() == blockName && seat.getState() == SeatState.AVAILABLE) {
                IDs.add(seat.getSeatID());
            }
        }
        return IDs;
    }

    public List<Integer> getAvailableSeatIDsOfRow(String blockName, int row) {
        if (!availableSeats.containsKey(blockName)) {
            return new ArrayList<>(); //blockname unknown
        }
        if (row >= availableSeats.get(blockName).size()) {
            return new ArrayList<>(); //row too big
        }
        List<Integer> IDs = new ArrayList<>();
        for (SeatInformation seat : idMap.values()) {
            if (seat.getBlockname() == blockName && seat.getRow() == row && seat.getState() == SeatState.AVAILABLE) {
                IDs.add(seat.getSeatID());
            }
        }
        Collections.sort(IDs);
        return IDs;
    }


    public long getNumberOfAvailableSeats() {
        return getAvailableSeatIDs().size();
    }

    public long getNumberOfAvailableSeatsOfBlock(String blockName) {
        return getAvailableSeatIDsOfBlock(blockName).size();
    }

    public long getNumberOfAvailableSeatsOfRow(String blockName, int row) {
        return getAvailableSeatIDsOfRow(blockName, row).size();
    }

    public Map<String, Long> getAvailableSeatsPerBlock() {
        Map<String, Long> blocks = new HashMap<>();
        for (String block : availableSeats.keySet()) {
            blocks.put(block, getNumberOfAvailableSeatsOfBlock(block));
        }
        return blocks;
    }


    public long getNumberOfAvailableSeats(double maxPrice) {
        Set<Integer> IDs = new HashSet<>();
        for (SeatInformation seat : idMap.values()) {
            if (seat.getState() == SeatState.AVAILABLE && prices.get(seat.getBlockname()) <= maxPrice) {
                IDs.add(seat.getSeatID());
            }
        }
        return IDs.size();
    }

    public List<String> getBlocksSorted() {
        List<Map.Entry<String, Double>> sorted = prices.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        List<String> returnKeys = sorted.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return returnKeys;
    }

    public Optional<Double> calculateCheapestPrice(int numberOfTickets) {
        if (getNumberOfAvailableSeats() < numberOfTickets) return Optional.empty();
        ArrayList<Integer> takenIDs = new ArrayList<>();
        List<String> blocks = getBlocksSorted();
        double totalPrice = 0;
        int ticketsToBuy = numberOfTickets;
        for (String block : blocks) {
            long available = getNumberOfAvailableSeatsOfBlock(block);
            if (available >= ticketsToBuy) {
                totalPrice += ticketsToBuy * prices.get(block);
                return Optional.of(totalPrice);
            } else {
                totalPrice += available * prices.get(block);
                ticketsToBuy -= available;
            }
        }
        return Optional.of(totalPrice);
    }
}
