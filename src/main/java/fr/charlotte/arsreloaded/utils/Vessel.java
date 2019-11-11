package fr.charlotte.arsreloaded.utils;


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

    public String constructNewReport() {
        String[] d = defaul.split("\n");
        StringBuilder newDe = new StringBuilder();
        for (String sd : d) {
            if (sd.startsWith("#")) {
                newDe.append(sd.substring(1)).append("\n");
            }
        }
        return newDe.toString();
    }

    public String constructFullReport() {
        String[] d = defaul.split("\n");
        StringBuilder newDe = new StringBuilder();
        for (String sd : d) {
            if (sd.startsWith("#")) {
                //DONOTHING
            } else {
                newDe.append(sd).append("\n");
            }
        }
        return newDe.toString();
    }

    public String getVesselid() {
        return vesselid;
    }
}
