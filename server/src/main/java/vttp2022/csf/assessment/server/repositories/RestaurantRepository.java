package vttp2022.csf.assessment.server.repositories;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import vttp2022.csf.assessment.server.models.Comment;
import vttp2022.csf.assessment.server.models.LatLng;
import vttp2022.csf.assessment.server.models.Restaurant;

@Repository
public class RestaurantRepository {

	@Autowired
	private MongoTemplate template;

	// TODO Task 2
	// Use this method to retrive a list of cuisines from the restaurant collection
	// You can add any parameters (if any) and the return type 
	// DO NOT CHNAGE THE METHOD'S NAME
	// Write the Mongo native query above for this method
	// db.restaurants.distinct("cuisine")
	public List<String> getCuisines() {
		// Implmementation in here

		List<String> cuisines = template.findDistinct(new Query(), "cuisine", "restaurants", String.class);

		return cuisines;
	}

	// TODO Task 3
	// Use this method to retrive a all restaurants for a particular cuisine
	// You can add any parameters (if any) and the return type 
	// DO NOT CHNAGE THE METHOD'S NAME
	// Write the Mongo native query above for this method
	/*
	 * db.restaurants.aggregate([
			{
				$match: { cuisine: { $regex: 'Sandwiches', $options: 'i' } }
			},
			{
				$project: { name: 1 }
			},
			{
				$sort: { name: 1 }
			}
		])
	 */
	public List<Document> getRestaurantsByCuisine(String cuisine) {
		// Implmementation in here
		MatchOperation mOps = Aggregation.match(
			Criteria.where("cuisine").regex(cuisine, "i"));
		
		ProjectionOperation pOps = Aggregation.project("name");

		SortOperation sOps = Aggregation.sort(Direction.ASC, "name");

		Aggregation pipeline = Aggregation.newAggregation(mOps, pOps, sOps);

		AggregationResults<Document> results = template.aggregate(pipeline, "restaurants", Document.class);

		List<Document> resultList = new LinkedList<>();
		results.forEach(d -> resultList.add(d));
		
		return resultList;
	}

	// TODO Task 4
	// Use this method to find a specific restaurant
	// You can add any parameters (if any) 
	// DO NOT CHNAGE THE METHOD'S NAME OR THE RETURN TYPE
	// Write the Mongo native query above for this method
	/*
	* db.restaurants.aggregate([
			{
				$match: { name: { $regex: 'ajisen ramen', $options: 'i' } }
			},
			{
				$project: { 
					restaurant_id: 1, 
					name: 1, 
					cuisine: 1, 
					address: { 
						$concat: ["$address.building", ", ", "$address.street", ", ", "$address.zipcode", ", ", "$borough" ] 
					},
					coordinates: "$address.coord"
				}
			}
		])
	 */
	public Optional<Restaurant> getRestaurant(String restaurantName) {
		// Implmementation in here

		MatchOperation mOps = Aggregation.match(
			Criteria.where("name").regex(restaurantName, "i"));

		ProjectionOperation pOps = Aggregation.project("restaurant_id", "name", "cuisine")
									.and(StringOperators.Concat.valueOf("address.building")
																.concat(", ")
																.concatValueOf("address.street")
																.concat(", ")
																.concatValueOf("address.zipcode")
																.concat(", ")
																.concatValueOf("borough")
										).as("address")
									.and("address.coord").as("coordinates");

		Aggregation pipeline = Aggregation.newAggregation(mOps, pOps);
		AggregationResults<Document> results = template.aggregate(pipeline, "restaurants", Document.class);

		List<Restaurant> resultList = new LinkedList<>();
		results.forEach(d -> {
			Restaurant r = new Restaurant();
			r.setRestaurantId(d.getString("restaurant_id"));
			r.setName(d.getString("name"));
			r.setCuisine(d.getString("cuisine"));
			r.setAddress(d.getString("address"));
			LatLng ll = new LatLng();
			ll.setLongitude(Float.valueOf(d.get("coordinates", ArrayList.class).get(0).toString()));
			ll.setLatitude(Float.valueOf(d.get("coordinates", ArrayList.class).get(1).toString()));
			r.setCoordinates(ll);

			resultList.add(r);
		});

		if (resultList.isEmpty()) return Optional.empty();
		
		return Optional.of(resultList.get(0));
	}

	// TODO Task 5
	// Use this method to insert a comment into the restaurant database
	// DO NOT CHNAGE THE METHOD'S NAME OR THE RETURN TYPE
	// Write the Mongo native query above for this method
	// db.comments.insert({
	// 	restaurant_id: '11',
	// 	name: '11',
	// 	rating: '11',
	// 	text: '11'
	// })
	public void addComment(Comment comment) {
		// Implmementation in here
		Document d = new Document();
		d.append("restaurant_id", comment.getRestaurantId());
		d.append("name", comment.getName());
		d.append("rating", comment.getRating());
		d.append("text", comment.getText());

		template.insert(d, "comments");
	}
	
	// You may add other methods to this class

}
