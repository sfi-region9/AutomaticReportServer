package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Users {

    private final String name;
    private final String scc;
    private final String vesselID;
    private String report = "";
    private String uuid = "defaultuuid";


    public Users(String name, String scc, String vesselID, String report, String uuid) {
        this.name = name;
        this.scc = scc;
        this.vesselID = vesselID;
        this.report = report;
        this.uuid = uuid;
    }

    public Users(String name, String scc, String vesselID, String report) {
        this.name = name;
        this.scc = scc;
        this.vesselID = vesselID;
        this.report = report;
    }

    public String getReport() {
        return report;
    }

    public String getName() {
        return name;
    }

    public String getVesselID() {
        return vesselID;
    }

    public String getScc() {
        return scc;
    }

    public String getUuid() {
        return uuid;
    }

    public String constructReport(Vessel vessel) {
        ArrayList<String> str = new ArrayList<>();
        str.add(vessel.getTemplate().replace("%name%", name).replace("%scc%", scc).replace("%date%", AutomaticReportServer.DATE.format(new Date(System.currentTimeMillis()))).replace("%month%", AutomaticReportServer.DATE_M.format(new Date(System.currentTimeMillis()))).replace("%year%", AutomaticReportServer.DATE_Y.format(new Date(System.currentTimeMillis()))).replace("%vesselname%", vessel.getName().replace("_", " ")));
        str.addAll(Arrays.asList(report.split("\n")));
        return StringUtils.join(str, "\n");
    }
}
