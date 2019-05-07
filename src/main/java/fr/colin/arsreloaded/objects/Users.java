package fr.colin.arsreloaded.objects;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Users {

    private String name;
    private String scc;
    private String vesselid;
    private String report = "";


    public Users(String name, String scc, String vesselid) {
        this.name = name;
        this.scc = scc;
        this.vesselid = vesselid;
    }

    public Users(String name, String scc, String vesselid, String report) {
        this.name = name;
        this.scc = scc;
        this.vesselid = vesselid;
        this.report = report;
    }

    public String getReport() {
        return report;
    }

    public String getName() {
        return name;
    }

    public String getVesselid() {
        return vesselid;
    }

    public String getScc() {
        return scc;
    }

    public String constructReport() {
        ArrayList<String> str = new ArrayList<>();
        str.add("Name : " + name);
        str.add(scc);
        str.addAll(Arrays.asList(report.split("\n")));
        return StringUtils.join(str, "\n");
    }
}
