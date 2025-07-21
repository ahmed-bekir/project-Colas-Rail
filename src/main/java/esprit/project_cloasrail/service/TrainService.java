package esprit.project_cloasrail.service;

import esprit.project_cloasrail.model.Route;
import esprit.project_cloasrail.model.Station;
import esprit.project_cloasrail.model.Train;
import esprit.project_cloasrail.repository.StationRepository;
import esprit.project_cloasrail.repository.RouteRepository;
import esprit.project_cloasrail.repository.TrainRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RestController
public class TrainService {

    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private TrainRepository trainRepository;

    private final Map<String, double[]> stationCoordinates = new HashMap<>() {{
        put("2001D", new double[]{36.79657, 10.17979}); // Tunis Ville Barcelone
        put("SMB", new double[]{36.78728, 10.16592}); // Saida Manoubia
        put("NJH", new double[]{36.79349, 10.15474}); // Ennajeh
        put("ETY", new double[]{36.79239, 10.13818}); // Ettayarane
        put("EZH", new double[]{36.79309, 10.13838}); // Ezzouhour
        put("HRR", new double[]{36.78450, 10.11687}); // Hrairia
        put("2005E", new double[]{36.78042, 10.10238}); // Bougatfa
        put("1015D", new double[]{36.82014, 10.07461}); // Gobaa
        put("ORG", new double[]{36.81844, 10.08582}); // Les Orangers
        put("MNB", new double[]{36.800, 10.1000}); // Manouba
        put("ELB", new double[]{36.8050, 10.1100}); // El Bortal
        put("BRD", new double[]{36.80724, 10.13523}); // Bardo
        put("ERD", new double[]{36.801940, 10.14808}); // Erraoudha
        put("MLS", new double[]{36.791666, 10.15533}); // Mellassine
        put("3001D", new double[]{36.79657, 10.17979}); // Tunis Ville Barcelone
    }};

    private final Map<String, String> stationNameMapping = new HashMap<>() {{
        put("Gobaa", "1015D");
        put("Gobba_Ville", "1015D");
        put("Les Orangers", "ORG");
        put("Manouba", "MNB");
        put("El Bortal", "ELB");
        put("Le Bardo", "BRD");
        put("Erraoudha", "ERD");
        put("Mellassine", "MLS");
        put("Saida Mannoubia", "SMB");
        put("Tunis ville", "2001D");
    }};

    @PostConstruct
    @Transactional
    public void initData() {
        System.out.println("Initializing train data...");
        File excelFile = new File("injection train pointe été 2025.xlsx");
        System.out.println("Looking for file at: " + excelFile.getAbsolutePath());

        if (!excelFile.exists()) {
            System.err.println("Excel file not found at: " + excelFile.getAbsolutePath());
            return;
        }

        try {
            System.out.println("Clearing existing data...");
            trainRepository.deleteAll();
            routeRepository.deleteAll();
            stationRepository.deleteAll();

            System.out.println("Loading data from Excel...");
            loadTimetableFromExcel(excelFile.getAbsolutePath());

            System.out.println("Initialization complete. Loaded:");
            System.out.println("- Stations: " + stationRepository.count());
            System.out.println("- Routes: " + routeRepository.count());
            System.out.println("- Trains: " + trainRepository.count());
        } catch (Exception e) {
            System.err.println("Failed to initialize data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    private void loadTimetableFromExcel(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Map<String, List<Map<String, Object>>> trainData = new HashMap<>();
            Map<String, Station> stationCache = new HashMap<>();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                System.out.println("Processing sheet: " + sheetName);

                Row trainRow = sheet.getRow(0);
                List<String> trainNumbers = new ArrayList<>();
                for (int i = 1; i < trainRow.getLastCellNum(); i++) {
                    String trainNumber = getCellValue(trainRow.getCell(i));
                    if (!trainNumber.isEmpty()) {
                        trainNumbers.add(trainNumber);
                        trainData.putIfAbsent(trainNumber, new ArrayList<>());
                    }
                }

                for (int rowIndex = 2; rowIndex <= 20; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    String stationName = getCellValue(row.getCell(0));
                    if (stationName.isEmpty() || stationName.equals("Fréquence")) continue;

                    String sname = stationNameMapping.getOrDefault(stationName, "");
                    if (sname.isEmpty()) {
                        System.out.println("No mapping found for station: " + stationName + ", skipping.");
                        continue;
                    }

                    for (int i = 0; i < trainNumbers.size(); i++) {
                        Cell timeCell = row.getCell(i + 1);
                        double decimalTime = getNumericCellValue(timeCell);
                        if (decimalTime == 0.0) continue;

                        LocalDateTime departureTime = convertDecimalToDateTime(decimalTime);

                        Map<String, Object> stop = new HashMap<>();
                        stop.put("locationSname", sname);
                        stop.put("locationLname", stationName);
                        stop.put("departureTime", departureTime);
                        trainData.get(trainNumbers.get(i)).add(stop);
                    }
                }
            }

            for (Map.Entry<String, List<Map<String, Object>>> entry : trainData.entrySet()) {
                String trainNumber = entry.getKey();
                List<Map<String, Object>> stops = entry.getValue();

                stops.sort(Comparator.comparing(stop -> (LocalDateTime) stop.get("departureTime")));

                List<Station> stations = new ArrayList<>();
                List<LocalDateTime> timetable = new ArrayList<>();
                for (Map<String, Object> stop : stops) {
                    String sname = (String) stop.get("locationSname");
                    String lname = (String) stop.get("locationLname");
                    double[] coords = stationCoordinates.getOrDefault(sname, null);
                    if (coords == null) {
                        System.out.println("Missing coordinates for station: " + sname + ", skipping.");
                        continue;
                    }

                    Station station = stationCache.computeIfAbsent(sname, k -> {
                        Station newStation = new Station(sname, lname, coords[0], coords[1]);
                        return stationRepository.save(newStation);
                    });
                    stations.add(station);
                    timetable.add((LocalDateTime) stop.get("departureTime"));
                }

                if (stations.size() < 2) {
                    System.out.println("Skipping train " + trainNumber + ": insufficient valid stations");
                    continue;
                }

                String routeName = trainNumber + "_Route";
                Route route = new Route(routeName, stations, timetable);
                routeRepository.saveAndFlush(route);
                System.out.println("Saved route: " + routeName);

                Train train = new Train(trainNumber, trainNumber, route);
                train.setLatitude(stations.get(0).getLatitude());
                train.setLongitude(stations.get(0).getLongitude());
                train.setDelayed(false);
                trainRepository.saveAndFlush(train);
                System.out.println("Saved train: " + trainNumber + " at " + train.getLatitude() + ", " + train.getLongitude());
            }
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        return 0.0;
    }

    private LocalDateTime convertDecimalToDateTime(double decimalDays) {
        long hours = (long) (decimalDays * 24);
        long minutes = (long) ((decimalDays * 24 * 60) % 60);
        return LocalDateTime.of(2025, 1, 1, (int) hours, (int) minutes);
    }

    @GetMapping("/api/trains/live")
    @Transactional(readOnly = true)
    public List<Train> getLiveTrains() {
        try {
            List<Train> trains = trainRepository.findAll();
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Paris")); // CET
            System.out.println("Calculating live positions at: " + now);

            for (Train train : trains) {
                if (train.getRoute() != null) {
                    Route route = train.getRoute();
                    List<Station> stations = route.getStations();
                    List<LocalDateTime> timetable = route.getTimetable();

                    if (stations.size() < 2 || timetable.size() != stations.size()) {
                        continue;
                    }

                    int currentSegment = -1;
                    for (int i = 0; i < timetable.size() - 1; i++) {
                        if (now.isAfter(timetable.get(i)) && now.isBefore(timetable.get(i + 1))) {
                            currentSegment = i;
                            break;
                        }
                    }

                    if (currentSegment == -1) {
                        if (now.isBefore(timetable.get(0))) {
                            currentSegment = 0; // Before first station
                        } else {
                            currentSegment = timetable.size() - 2; // After last station, stay at last
                        }
                    }

                    Station startStation = stations.get(currentSegment);
                    Station endStation = stations.get(currentSegment + 1);
                    LocalDateTime startTime = timetable.get(currentSegment);
                    LocalDateTime endTime = timetable.get(currentSegment + 1);

                    double progress = calculateProgress(now, startTime, endTime);
                    double lat = interpolate(startStation.getLatitude(), endStation.getLatitude(), progress);
                    double lon = interpolate(startStation.getLongitude(), endStation.getLongitude(), progress);

                    train.setLatitude(lat);
                    train.setLongitude(lon);
                    train.setDelayed(now.isAfter(endTime)); // Mark as delayed if past scheduled arrival
                }
            }

            System.out.println("Retrieved " + trains.size() + " trains with live positions.");
            return trains;
        } catch (Exception e) {
            System.err.println("Error retrieving live trains: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @GetMapping("/api/stations")
    @Transactional(readOnly = true)
    public List<Station> getAllStations() {
        try {
            List<Station> stations = stationRepository.findAll();
            System.out.println("Retrieved " + stations.size() + " stations from the database.");
            return stations;
        } catch (Exception e) {
            System.err.println("Error retrieving stations: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private double calculateProgress(LocalDateTime now, LocalDateTime startTime, LocalDateTime endTime) {
        long startMillis = startTime.atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli();
        long endMillis = endTime.atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli();
        long nowMillis = now.atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli();
        return (double) (nowMillis - startMillis) / (endMillis - startMillis);
    }

    private double interpolate(double start, double end, double progress) {
        return start + (end - start) * Math.min(Math.max(progress, 0.0), 1.0);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrainItinerary(String trainNumber) {
        try {
            Optional<Train> trainOptional = trainRepository.findById(trainNumber);
            if (trainOptional.isEmpty() || trainOptional.get().getRoute() == null) {
                System.out.println("Train not found or no route associated: " + trainNumber);
                return Collections.emptyList();
            }

            Train train = trainOptional.get();
            Route route = train.getRoute();
            List<Station> stations = route.getStations();
            List<LocalDateTime> timetable = route.getTimetable();

            if (stations == null || timetable == null || stations.size() != timetable.size()) {
                System.out.println("Invalid route data for train: " + trainNumber);
                return Collections.emptyList();
            }

            List<Map<String, Object>> itinerary = new ArrayList<>();
            for (int i = 0; i < stations.size(); i++) {
                Map<String, Object> stop = new HashMap<>();
                stop.put("stationName", stations.get(i).getName());
                stop.put("time", timetable.get(i).toString());
                itinerary.add(stop);
            }
            System.out.println("Retrieved itinerary for train: " + trainNumber + " with " + itinerary.size() + " stops.");
            return itinerary;
        } catch (Exception e) {
            System.err.println("Error retrieving itinerary for train " + trainNumber + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}