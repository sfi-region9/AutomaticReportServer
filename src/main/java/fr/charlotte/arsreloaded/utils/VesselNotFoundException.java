package fr.charlotte.arsreloaded.utils;

public class VesselNotFoundException extends Exception {

    @Override
    public void printStackTrace() {
        System.err.println("The vessel specified was not found on the database");
    }
}
