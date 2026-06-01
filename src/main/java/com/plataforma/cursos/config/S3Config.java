package com.plataforma.cursos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    /**
     * Cliente de AWS S3.
     *
     * Las credenciales se resuelven con la cadena por defecto del SDK
     * (variables de entorno AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY en local,
     * o el IAM Role asociado a la instancia EC2 en produccion).
     */
    @Bean
    public S3Client s3Client(@Value("${aws.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClientBuilder(ApacheHttpClient.builder())
                .build();
    }
}
