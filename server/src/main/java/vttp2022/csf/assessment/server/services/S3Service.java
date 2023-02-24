package vttp2022.csf.assessment.server.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Service
public class S3Service {
    
    @Autowired
    private AmazonS3 s3client;

    public Optional<String> get(String id) {
        String imageUrlToCheck = "https://fkdl.sgp1.digitaloceanspaces.com/assessment%2F";

        try {
            GetObjectRequest gor = new GetObjectRequest("fkdl", "assessment/%s".formatted(id));
            S3Object s3Object = s3client.getObject(gor);
            ObjectMetadata metadata = s3Object.getObjectMetadata();
            Map<String, String> userData = metadata.getUserMetadata();

            if (userData.size() == 0) return Optional.empty();

            String title = userData.get("title");
            return Optional.of(imageUrlToCheck + title);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public String upload(byte[] imageBytes, String id) throws IOException {

        // name = name.replace(" ", "_");

        // create user metadata
        Map<String, String> userData = new HashMap<>();
        userData.put("title", id);
        userData.put("uploadDate", new Date().toString());
        userData.put("originalFilename", id);
        
        // create metadata for object
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBytes.length);
        metadata.setContentType("image/png");
        metadata.setUserMetadata(userData);

        // create put request
        PutObjectRequest por = new PutObjectRequest(
                                    "fkdl",
                                    "assessment/%s".formatted(id), 
                                    new ByteArrayInputStream(imageBytes), 
                                    metadata);

        // allow public access
        por = por.withCannedAcl(CannedAccessControlList.PublicRead);

        // "upload" object to s3 storage
        s3client.putObject(por);

        return "https://fkdl.sgp1.digitaloceanspaces.com/assessment%2F" + id;
    }
}
