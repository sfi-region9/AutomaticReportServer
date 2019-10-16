package fr.colin.arsreloaded.utils;

public class Vessel {

    private String name;
    private String vesselid;
    private String coid;
    private String template;
    private String defaul;

    public Vessel(String name, String vesselid, String coid, String template, String defaul) {
        this.name = name;
        this.vesselid = vesselid;
        this.coid = coid;
        this.template = template;
        this.defaul = defaul;
    }

    public String getDefaul() {
        return defaul;
    }

    public String getTemplate() {
        return template;
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
