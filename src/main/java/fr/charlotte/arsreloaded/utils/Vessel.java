package fr.charlotte.arsreloaded.utils;


public class Vessel {

    private String name;
    private String vesselID;
    private String coID;
    private String template;
    private String defaultReport;
    private String reportOfficerMail;

    public Vessel(String name, String vesselID, String coID, String template, String defaultReport, String reportOfficerMail) {
        this.name = name;
        this.vesselID = vesselID;
        this.coID = coID;
        this.template = template;
        this.defaultReport = defaultReport;
        this.reportOfficerMail = reportOfficerMail;
    }

    public String getDefaultReport() {
        return defaultReport;
    }

    public String getTemplate() {
        return template;
    }


    public String getName() {
        return name;
    }

    public String getCoID() {
        return coID;
    }

    public String constructNewReport() {
        String[] d = defaultReport.split("\n");
        StringBuilder newDe = new StringBuilder();
        for (String sd : d) {
            if (sd.startsWith("#")) {
                newDe.append(sd.substring(1)).append("\n");
            }
        }
        return newDe.toString();
    }

    public String constructFullReport() {
        String[] d = defaultReport.split("\n");
        StringBuilder newDe = new StringBuilder();
        for (String sd : d) {
            if (!sd.startsWith("#")) {
                newDe.append(sd).append("\n");
            }
        }
        return newDe.toString();
    }

    public String getReportOfficerMail() {
        return reportOfficerMail;
    }

    public String getVesselID() {
        return vesselID;
    }
}
