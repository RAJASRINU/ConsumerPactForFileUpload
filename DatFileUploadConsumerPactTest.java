package com.example.demo;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
public class DatFileUploadConsumerPactTest {

    @Pact(provider = "DatFileProvider", consumer = "DatFileConsumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) throws IOException {
        byte[] fileBytes = "This is a test .dat file".getBytes();

        return builder
                .given("A .dat file is uploaded")
                .uponReceiving("A request to upload a .dat file")
                .path("/upload")
                .method("POST")
                .headers("Content-Type", "multipart/form-data")
                .withFileUpload("file", "test.dat", "application/octet-stream", fileBytes)
                .willRespondWith()
                .status(200)
                .body("File uploaded successfully")
                .toPact();
    }

    @Test
    @PactTestFor(providerName = "DatFileProvider")
    public void testDatFileUpload(MockServer mockServer) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("This is a test .dat file".getBytes()) {
            @Override
            public String getFilename() {
                return "test.dat";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/upload", requestEntity, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded successfully", response.getBody());
    }
}

