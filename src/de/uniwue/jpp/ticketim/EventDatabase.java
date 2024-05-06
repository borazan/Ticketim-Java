package de.uniwue.jpp.ticketim;

import de.uniwue.jpp.ticketim.base.SeatState;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniwue.jpp.ticketim.util.DataGenerator.generateEventDatabase;

public class EventDatabase {

    private Set<Event> events;

    public EventDatabase(Set<Event> events) {
        if (events == null) throw new NullPointerException();
        HashMap<LocalDate, ArrayList<String>> DatePlace = new HashMap<>(); //eger iki kere place varsa listede yanlis
        HashMap<String, ArrayList<LocalDate>> ArtistDate = new HashMap<>(); //
        for (Event event : events) {

            if (DatePlace.containsKey(event.getDate())) {
                DatePlace.get(event.getDate()).add(event.getLocationName());
            } else {
                ArrayList<String> newPlaceList = new ArrayList<>();
                newPlaceList.add(event.getLocationName());

                DatePlace.put(event.getDate(), newPlaceList);
            }

            if (ArtistDate.containsKey(event.getArtistName())) {
                ArtistDate.get(event.getArtistName()).add(event.getDate());
            } else {
                ArrayList<LocalDate> newDateList = new ArrayList<>();
                newDateList.add(event.getDate());
                ArtistDate.put(event.getArtistName(), newDateList);
            }
        }

        for (LocalDate date : DatePlace.keySet()) {
            ArrayList<String> places = DatePlace.get(date);
            for (String place : places) {
                if (Collections.frequency(places, place) > 1) throw new IllegalArgumentException();
            }
        }
        for (String artist : ArtistDate.keySet()) {
            ArrayList<LocalDate> dates = ArtistDate.get(artist);
            for (LocalDate date : dates) {
                if (Collections.frequency(dates, date) > 1) throw new IllegalArgumentException();
            }
        }
        this.events = events;
    }


    public Set<String> getArtists() {
        HashSet<String> artists = new HashSet<>();
        for (Event event : events) {
            artists.add(event.getArtistName());
        }
        return artists;
    }

    public Set<String> getLocations() {
        HashSet<String> locations = new HashSet<>();
        for (Event event : events) {
            locations.add(event.getLocationName());
        }
        return locations;
    }

    public Map<LocalDate, String> getLocationsOfArtist(String artistName) {
        Map<LocalDate, String> dateLocation = new HashMap<>();
        for (Event event : events) {
            if (event.getArtistName().equals(artistName)) {
                dateLocation.put(event.getDate(), event.getLocationName());
            }
        }
        return dateLocation;
    }

    public Map<LocalDate, String> getArtistsOfLocation(String locationName) {
        Map<LocalDate, String> dateArtist = new HashMap<>();
        for (Event event : events) {
            if (event.getLocationName().equals(locationName)) {
                dateArtist.put(event.getDate(), event.getArtistName());
            }
        }
        return dateArtist;
    }

    public Optional<String> getNextLocationOfArtist(String artistName, LocalDate now) {
        Map<LocalDate, String> dateLocation = getLocationsOfArtist(artistName);

        LocalDate nextDate = null;
        String nextLocation = null;

        for (Map.Entry<LocalDate, String> entry : dateLocation.entrySet()) {
            LocalDate date = entry.getKey();
            if (date.isAfter(now)) {
                if (nextDate == null || date.isBefore(nextDate)) {
                    nextDate = date;
                    nextLocation = entry.getValue();
                }
            }
        }
        if (nextDate == null) return Optional.empty();
        return Optional.of(nextLocation);
    }

    public Optional<String> getNextArtistOfLocation(String locationName, LocalDate now) {
        Map<LocalDate, String> dateArtist = getArtistsOfLocation(locationName);

        LocalDate nextDate = null;
        String nextArtist = null;

        for (Map.Entry<LocalDate, String> entry : dateArtist.entrySet()) {
            LocalDate date = entry.getKey();
            if (date.isAfter(now)) {
                if (nextDate == null || date.isBefore(nextDate)) {
                    nextDate = date;
                    nextArtist = entry.getValue();
                }
            }
        }
        if (nextDate == null) return Optional.empty();
        return Optional.of(nextArtist);
    }

    public Map<LocalDate, String> getLocationsOfArtist(String artistName, LocalDate from, LocalDate to) {
        Map<LocalDate, String> dateLocation = new HashMap<>();
        for (Event event : events) {
            LocalDate date = event.getDate();
            if (event.getArtistName().equals(artistName) && date.isBefore(to) && date.isAfter(from)) {
                dateLocation.put(date, event.getLocationName());
            }
        }
        return dateLocation;
    }

    public Map<LocalDate, String> getArtistsOfLocation(String locationName, LocalDate from, LocalDate to) {
        Map<LocalDate, String> dateArtist = new HashMap<>();
        for (Event event : events) {
            LocalDate date = event.getDate();
            if (event.getLocationName().equals(locationName) && date.isBefore(to) && date.isAfter(from)) {
                dateArtist.put(date, event.getArtistName());
            }
        }
        return dateArtist;
    }

    public long getNumberOfAvailableSeatsForArtist(String artistName) {
        long totalseats = 0;
        for (Event event : events) {
            if (event.getArtistName().equals(artistName)) {
                totalseats += event.getNumberOfAvailableSeats();
            }
        }
        return totalseats;
    }

    public Map<LocalDate, Double> getPricesForArtist(String artistName, int numberOfTickets) {
        Map<LocalDate, Double> dateTotalprice = new HashMap<>();
        for (Event event : events) {
            if (event.getArtistName().equals(artistName) && event.calculateCheapestPrice(numberOfTickets).isPresent()) {
                double price = event.calculateCheapestPrice(numberOfTickets).get();
                LocalDate date = event.getDate();
                dateTotalprice.put(date, price);
            }

        }
        return dateTotalprice;
    }

    public List<String> getArtistsSortedByRevenue() {
        HashMap<String, Double> artistRevenue = new HashMap<>();
        for (Event event : events) {
            double totalPrice = 0;
            String artistName = event.getArtistName();
            for (int id : event.getSeatIDs()) {
                if (event.getState(id).get() == SeatState.UNAVAILABLE) //seat is sold
                {
                    totalPrice += event.getPrice(id).get();
                }
            }
            artistRevenue.merge(artistName, totalPrice, Double::sum);
        }

        return artistRevenue.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public Map<LocalDate, Double> getPercentageOfSoldSeatsForArtistsPerDate(String artistName) {
        Map<LocalDate, Double> datePercent = new HashMap<>();
        for (Event event : events) {
            long soldSeats = 0;
            long totalSeats = event.getSeatIDs().size();

            if (event.getArtistName().equals(artistName)) {
                for (int id : event.getSeatIDs()) {
                    if (event.getState(id).get() == SeatState.UNAVAILABLE) {
                        soldSeats++;
                    }
                }
                double percentage = (soldSeats / (double) totalSeats);
                double roundedPercentage = Math.round(percentage * 100.0) / 100.0;
                datePercent.put(event.getDate(), roundedPercentage);
            }
        }

        return datePercent;
    }

    public Map<String, Double> getAveragePercentageOfSoldSeatsPerArtist() {
        Map<String, Double> artistsPercentage = new HashMap<>();
        List<String> artists = getArtistsSortedByRevenue();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        for (String artist : artists) {
            double percentagesTotal = 0;
            int eventCount = 0;
            for (Event event : events) {
                if (event.getArtistName().equals(artist)) {
                    int soldSeats = 0;
                    for (int id : event.getSeatIDs()) {
                        if (event.getState(id).get() == SeatState.UNAVAILABLE) {
                            soldSeats++;
                        }
                    }
                    int totalSeats = event.getSeatIDs().size();
                    double soldPercentage = (double) soldSeats / totalSeats;
                    percentagesTotal += soldPercentage;
                    eventCount++;
                }
            }
            artistsPercentage.put(artist, Double.valueOf(decimalFormat.format(percentagesTotal / (double) eventCount)));
        }
        return artistsPercentage;
    }

    public Map<DayOfWeek, Double> getAveragePercentOfSoldSeatsPerWeekday() {
        HashMap<DayOfWeek, ArrayList<Double>> daySoldMap = new HashMap<>();
        HashMap<DayOfWeek, Double> returnMap = new HashMap<>();
        for (int i = 1; i < 8; i++) {
            daySoldMap.put(DayOfWeek.of(i), new ArrayList<>());
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        for (Event event : events) {
            long soldSeats = 0;
            long totalSeats = event.getSeatIDs().size();
            for (int id : event.getSeatIDs()) {
                if (event.getState(id).get() == SeatState.UNAVAILABLE) //seat is sold
                {
                    soldSeats++;
                }
            }
            Double percentage = soldSeats / (double) totalSeats;
            daySoldMap.get(event.getDate().getDayOfWeek()).add(percentage);
        }

        for (Map.Entry<DayOfWeek, ArrayList<Double>> day : daySoldMap.entrySet()) {
            Double sum = 0.0;
            ArrayList<Double> numbers = day.getValue();
            for (Double number : numbers) {
                sum += number;
            }
            Double average = Double.valueOf(decimalFormat.format(sum / (double) numbers.size()));
            if (!numbers.isEmpty()) returnMap.put(day.getKey(), average);
        }
        return returnMap;
    }

    public Optional<StandardReservationManager> getManagerForEvent(String artistName, String locationName, LocalDate date) {
        for (Event event : events) {
            if (event.getArtistName().equals(artistName) && event.getLocationName().equals(locationName) && event.getDate().equals(date)) {
                return Optional.of(event.manager);
            }
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        EventDatabase eventDatabase = generateEventDatabase();

        // do tests here (see instructions)
        System.out.println(eventDatabase.getPercentageOfSoldSeatsForArtistsPerDate("Monesk"));
        System.out.println("Erwartete Rückgabe: {2023-01-13=0.21, 2023-01-09=0.36, 2023-01-05=0.0, 2023-01-03=0.02}");

        System.out.println(eventDatabase.getPercentageOfSoldSeatsForArtistsPerDate("TuerFox"));
        System.out.println("Erwartete Rückgabe: {2023-01-09=0.25, 2023-01-05=0.44, 2023-01-04=0.3, 2023-01-03=0.14}");

        System.out.println(eventDatabase.getPercentageOfSoldSeatsForArtistsPerDate("Lua Dipa"));
        System.out.println("Erwartete Rückgabe: {2023-01-09=0.25, 2023-01-06=0.38, 2023-01-05=0.37, 2023-01-03=0.45, 2023-01-01=0.14}");

    }

}
