package com.plataforma.cursos.service;

import com.plataforma.cursos.exception.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Encapsula las operaciones contra el bucket de AWS S3 donde se almacenan
 * los resumenes de inscripcion.
 *
 * Cada resumen se guarda en una carpeta cuyo nombre es el numero del resumen:
 *   {numeroResumen}/resumen-inscripcion-{numeroResumen}.pdf
 */
@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3StorageService(S3Client s3Client,
                            @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    /** Sube (o reemplaza) el archivo del resumen y devuelve la clave (key) en S3. */
    public String guardar(Long numeroResumen, byte[] contenido, String contentType) {
        String key = construirKey(numeroResumen);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(contenido));
        return key;
    }

    /** Descarga el contenido del archivo del resumen desde S3. */
    public byte[] descargar(Long numeroResumen) {
        String key = construirKey(numeroResumen);
        try {
            ResponseBytes<GetObjectResponse> objeto = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(key).build());
            return objeto.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new RecursoNoEncontradoException(
                    "No existe el archivo del resumen " + numeroResumen + " en S3");
        }
    }

    /** Elimina el archivo del resumen de S3. */
    public void eliminar(Long numeroResumen) {
        if (!existe(numeroResumen)) {
            throw new RecursoNoEncontradoException(
                    "No existe el archivo del resumen " + numeroResumen + " en S3");
        }
        String key = construirKey(numeroResumen);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    /** Indica si el archivo del resumen ya existe en S3. */
    public boolean existe(Long numeroResumen) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(construirKey(numeroResumen))
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // headObject devuelve 404 sin cuerpo, por lo que no siempre llega como NoSuchKeyException
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    public String construirKey(Long numeroResumen) {
        return numeroResumen + "/resumen-inscripcion-" + numeroResumen + ".pdf";
    }
}
