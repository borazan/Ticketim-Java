package de.uniwue.jpp.ticketim.util;

import de.uniwue.jpp.ticketim.Event;
import de.uniwue.jpp.ticketim.EventDatabase;
import de.uniwue.jpp.ticketim.base.SeatState;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DataGenerator {

    private static final List<String> artistNames = List.of("Lua Dipa", "Red Hot Chili Pipers", "TuerFox", "Wory Cong", "Monesk");
    private static final List<String> locationNames = List.of("Posthalle", "Tempodrom", "Jahrhunderthalle", "Taubertal Festival", "Residenzplatz");

    public static EventDatabase generateEventDatabase(){
        return generateRandomEventDatabase(123456789, 10);
    }

    public static EventDatabase generateRandomEventDatabase(int seed, int numberOfDays){
        Random random = new Random(seed);

        LocalDate date = LocalDate.of(2023,1,1);
        int maxNumberOfEventsPerDay = Math.min(artistNames.size(), locationNames.size());

        Set<Event> result = new HashSet<>();
        for (int i = 0; i < numberOfDays; i++) {
            List<String> artists = new ArrayList<>(artistNames);
            List<String> locations = new ArrayList<>(locationNames);

            int numberOfEvents = random.nextInt(maxNumberOfEventsPerDay+1);
            for (int j = 0; j < numberOfEvents; j++) {
                String artist = getRandomValueFromList(artists, random);
                String location = getRandomValueFromList(locations, random);
                artists.remove(artist);
                locations.remove(location);
                Map<String, Double> prices = createRandomPrices(random).findFirst().get();
                Map<String, List<Integer>> availableSeats = createRandomAvailableSeats(prices.keySet(), random);

                result.add(createEventWithRandomSeatState(artist, location, date, availableSeats, prices, random));
            }
            date = date.plus(random.nextInt(1,3), ChronoUnit.DAYS);
        }

        if(result.isEmpty()){
            Map<String, Double> prices = createRandomPrices(random).findFirst().get();
            Map<String, List<Integer>> availableSeats = createRandomAvailableSeats(prices.keySet(), random);
            result.add(createEventWithRandomSeatState(getRandomArtistName(random), getRandomLocationName(random), date, availableSeats, prices, random));
        }

        return new EventDatabase(result);
    }

    private static Event createEventWithRandomSeatState(String artistName, String locationName, LocalDate date, Map<String, List<Integer>> availableSeats, Map<String, Double> prices, Random random){
        Event event = new Event(artistName, locationName, date, availableSeats, prices);

        ArrayList<Integer> seatIDs = new ArrayList<>(event.getSeatIDs());
        Collections.shuffle(seatIDs, random);

        int reserved = random.nextInt(seatIDs.size()/2);

        seatIDs.stream().limit(reserved).forEach(seatID -> event.setState(seatID, SeatState.RESERVED));
        seatIDs.stream().skip(reserved).limit(random.nextInt(seatIDs.size()/2)).forEach(seatID -> event.setState(seatID, SeatState.UNAVAILABLE));

        return event;
    }

    private static Stream<Map<String, Double>> createRandomPrices(Random random){
        return Stream.generate(() -> Stream.iterate('A', c -> (char) (c+1)).limit(random.nextInt(1, 11)).collect(Collectors.toMap(
                c -> "" + c,
                c -> random.nextInt(1,21)*5.0
        )));
    }

    private static Map<String, List<Integer>> createRandomAvailableSeats(Set<String> blockNames, Random random){
        return blockNames.stream().collect(Collectors.toMap(
                blockName -> blockName,
                blockName -> IntStream.generate(() -> random.nextInt(5,20)).limit(random.nextInt(3,20)).boxed().toList()
        ));
    }

    private static <T extends Comparable<? super T>> T getRandomValueFromList(List<T> list, Random random){
        return list.get(random.nextInt(list.size()));
    }

    private static String getRandomArtistName(Random random){
        return getRandomValueFromList(artistNames, random);
    }

    private static String getRandomLocationName(Random random){
        return getRandomValueFromList(locationNames, random);
    }
}
