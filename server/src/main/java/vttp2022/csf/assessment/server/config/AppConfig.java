package vttp2022.csf.assessment.server.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AppConfig {

    @Value("${ACCESS_KEY}") private String ACCESS_KEY;
    @Value("${SECRET_KEY}") private String SECRET_KEY;
    
    @Bean
    public AmazonS3 getS3Client() {
        BasicAWSCredentials cred = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        EndpointConfiguration ec = new EndpointConfiguration(
                                        "sgp1.digitaloceanspaces.com", 
                                        "sgp1");

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(ec)
                .withCredentials(new AWSStaticCredentialsProvider(cred))
                .build();
    }
}
