package esprit.project_cloasrail.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(
            name = "route_stations",
            joinColumns = @JoinColumn(name = "route_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private List<Station> stations;
    private List<LocalDateTime> timetable;

    public Route() {}

    public Route(String name, List<Station> stations, List<LocalDateTime> timetable) {
        this.name = name;
        this.stations = stations;
        this.timetable = timetable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Station> getStations() { return stations; }
    public void setStations(List<Station> stations) { this.stations = stations; }
    public List<LocalDateTime> getTimetable() { return timetable; }
    public void setTimetable(List<LocalDateTime> timetable) { this.timetable = timetable; }
}