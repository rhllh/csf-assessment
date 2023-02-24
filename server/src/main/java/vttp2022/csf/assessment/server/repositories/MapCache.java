package vttp2022.csf.assessment.server.repositories;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Repository
public class MapCache {
	
	public static final String MAP_API_URL = "http://map.chuklee.com/map";

	// TODO Task 4
	// Use this method to retrieve the map
	// You can add any parameters (if any) and the return type 
	// DO NOT CHNAGE THE METHOD'S NAME
	public byte[] getMap(String lat, String lng) {
		// Implmementation in here
		String uri = UriComponentsBuilder.fromUriString(MAP_API_URL)
                            .queryParam("lat", lat)
							.queryParam("lng", lng)
                            .toUriString();
		RequestEntity<Void> req = RequestEntity.get(uri).build();
        RestTemplate template = new RestTemplate();
        ResponseEntity<byte[]> resp = template.exchange(req, byte[].class);
        byte[] payload = resp.getBody();
	
		return payload;
	}

	// You may add other methods to this class

}
