// src/main/java/esprit/project_cloasrail/controller/TrainController.java
package esprit.project_cloasrail.controller;

import esprit.project_cloasrail.model.Train;
import esprit.project_cloasrail.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trains")
@CrossOrigin(origins = "http://localhost:4200")
public class TrainController {

    @Autowired
    private TrainService trainService;



    @GetMapping("/{trainNumber}/itinerary")
    public List<Map<String, Object>> getTrainItinerary(@PathVariable String trainNumber) {
        return trainService.getTrainItinerary(trainNumber);
    }
}