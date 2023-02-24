package vttp2022.csf.assessment.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import vttp2022.csf.assessment.server.models.Comment;
import vttp2022.csf.assessment.server.models.Restaurant;
import vttp2022.csf.assessment.server.repositories.MapCache;
import vttp2022.csf.assessment.server.services.RestaurantService;
import vttp2022.csf.assessment.server.services.S3Service;

@Controller
@RequestMapping("/api")
public class RestaurantController {

    @Autowired
    private RestaurantService svc;

    @Autowired
    private MapCache cache;

    @Autowired
    private S3Service s3Service;
    
    @GetMapping(path="/cuisines", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin()
    public ResponseEntity<String> getListOfCuisines() {
        List<String> cuisines = svc.getCuisines();

        JsonArrayBuilder jab = Json.createArrayBuilder();
        cuisines.forEach(c -> {
            c = c.replace("/","_");
            jab.add(c);
        });
        JsonObject json = Json.createObjectBuilder()
                            .add("cuisines", jab.build())
                            .build();

        return ResponseEntity.ok(json.toString());
    }

    @GetMapping(path="/{cuisine}/restaurants", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin()
    public ResponseEntity<String> getRestaurantsByCuisine(@PathVariable String cuisine) {
        cuisine = cuisine.replace("_","/");
        System.out.println("cuisine > " + cuisine);
        List<String> restaurantNames = svc.getRestaurantsByCuisine(cuisine);

        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String r : restaurantNames) {
            jab.add(r);
        }
        JsonObject json = Json.createObjectBuilder()
                                .add("restaurants", jab.build())
                                .build();

        return ResponseEntity.ok(json.toString());
    } 

    @GetMapping(path="/restaurant/{name}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin()
    public ResponseEntity<String> getRestaurantByName(@PathVariable String name) {
        name = name.replace("_", "/");
        name = name.replace("%28", "(");
        name = name.replace("%29", ")");
        name = name.replace("%20", " ");
        if (name.contains("(")) name = name.substring(0, name.indexOf("("));
        System.out.println("rest name > " + name);
        Optional<Restaurant> restaurantOpt = svc.getRestaurant(name);

        if (restaurantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Restaurant r = restaurantOpt.get();
        String id = r.getRestaurantId();
        String lat = String.valueOf(r.getCoordinates().getLatitude());
        String lng = String.valueOf(r.getCoordinates().getLongitude());
        
        String imageUrl = "";
        // check if map exists in s3
        Optional<String> imageExistsOpt = s3Service.get(id);
        if (!imageExistsOpt.isEmpty()) {
            System.out.println("found in s3");
            imageUrl = imageExistsOpt.get();
        } else {
            System.out.println("not found in s3");
            // else call map api with latlng then upload
            byte[] mapByteArray = cache.getMap(lat,lng);
            try {
                imageUrl = s3Service.upload(mapByteArray, id);
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        // turn restaurant obj into json
        r.setMapURL(imageUrl);
        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonObject json = job.add("restaurantId", r.getRestaurantId())
                            .add("name", r.getName())
                            .add("cuisine", r.getCuisine().replace("/","_"))
                            .add("address", r.getAddress())
                            .add("coordinates", "[%s,%s]".formatted(lng, lat))
                            .add("image_url", imageUrl)
                            .build();

        return ResponseEntity.ok(json.toString());

    }

    @PostMapping(path="/comments", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin()
    public ResponseEntity<String> postComment(@RequestBody Comment comment) {

        try {
            svc.addComment(comment);
        } catch (Exception e) {
            // not created
            return ResponseEntity.status(500)
                .body(Json.createObjectBuilder().add("message", "Comment not posted").build().toString());
        }
    
        return ResponseEntity.status(201)
            .body(Json.createObjectBuilder().add("message", "Comment posted").build().toString());
    }
}
