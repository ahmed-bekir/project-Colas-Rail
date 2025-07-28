package esprit.project_cloasrail.model;

public class ItineraryStop {
    private String stationName;
    private String time;

    // Getters, setters, and constructors
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public ItineraryStop() {}
    public ItineraryStop(String stationName, String time) {
        this.stationName = stationName;
        this.time = time;
    }
}