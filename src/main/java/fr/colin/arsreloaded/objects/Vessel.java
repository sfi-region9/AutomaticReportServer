package fr.colin.arsreloaded.objects;

public class Vessel {

    private String name;
    private String vesselid;
    private String coid;



    public Vessel(String name, String vesselid, String coid) {
        this.name = name;
        this.vesselid = vesselid;
        this.coid = coid;
    }

    public String getName() {
        return name;
    }

    public String getCoid() {
        return coid;
    }

    public String getVesselid() {
        return vesselid;
    }
}
