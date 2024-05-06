package de.uniwue.jpp.ticketim;

public class IdManager {
    private IdManager(){
    }
    private static int counter = 0;
    public static int getId(){
        counter++;
        return counter;
    }
}
