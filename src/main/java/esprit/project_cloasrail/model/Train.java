package esprit.project_cloasrail.model;

import jakarta.persistence.*;

@Entity
public class Train {
    @Id
    private String id;
    private String trainNumber;
    @ManyToOne
    private Route route;
    private Double latitude;
    private Double longitude;
    private Double speed;
    @ManyToOne
    private Station currentStation;
    @ManyToOne
    private Station nextStation;
    private boolean delayed;

    public Train() {}

    public Train(String id, String trainNumber, Route route) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.route = route;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    public Station getCurrentStation() { return currentStation; }
    public void setCurrentStation(Station currentStation) { this.currentStation = currentStation; }
    public Station getNextStation() { return nextStation; }
    public void setNextStation(Station nextStation) { this.nextStation = nextStation; }
    public boolean isDelayed() { return delayed; }
    public void setDelayed(boolean delayed) { this.delayed = delayed; }
}