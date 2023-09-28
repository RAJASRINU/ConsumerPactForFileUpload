package com.example.demo;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class ExampleFileUploadSpec {

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("File Service", this);

    @Pact(provider = "File Service", consumer= "Junit Consumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("test.dat");

        InputStream inputStream = null;
        byte[] bytes = new byte[0];
        try {
            inputStream = classPathResource.getInputStream();
            bytes = IOUtils.toByteArray(inputStream);
        }catch (Exception e){

        }finally {
            if(null != inputStream){
                inputStream.close();
            }
        }

        return builder
                .uponReceiving("a multipart file POST")
                .path("/upload")
                .method("POST")
                .withFileUpload("file", "test.dat", "application/octet-stream", bytes)
                .willRespondWith()
                .status(201)
                .body("file uploaded ok", "text/plain")
                .toPact();
    }

    @Test
    @PactVerification
    public void runTest() throws Exception {

        ClassPathResource classPathResource = new ClassPathResource("test.dat");

        InputStream inputStream = null;
        byte[] bytes = new byte[0];
        try {
            inputStream = classPathResource.getInputStream();
            bytes = IOUtils.toByteArray(inputStream);
        }catch (Exception e){

        }finally {
            if(null != inputStream){
                inputStream.close();
            }
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("file", bytes, ContentType.create("application/octet-stream"), "test.dat");

        HttpPost request = new HttpPost(mockProvider.getUrl() + "/upload");
        request.setEntity(entityBuilder.build());

        System.out.println("Executing request " + request.getRequestLine());

        CloseableHttpResponse response = httpClient.execute(request);
        // Optionally handle the response if needed
        response.close();
        httpClient.close();
    }
}

