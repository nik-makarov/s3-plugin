package info.makarov.s3.core.service;

import info.makarov.s3.core.entity.preferences.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BucketService {

    public List<Bucket> getBuckets(Profile profile) {
        S3Client s3Client = S3ClientFactory.create(profile);
        return Collections.unmodifiableList(s3Client.listBuckets().buckets());
    }

    public List<String> getBucketNames(Profile profile) {
        return getBuckets(profile).stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
    }

    public List<S3Object> getAllObjects(Profile profile, String bucket) {
        S3Client s3Client = S3ClientFactory.create(profile);
        List<S3Object> result = new ArrayList<>();
        String startWith = null;
        ListObjectsResponse response;
        while (true) {
            ListObjectsRequest request = ListObjectsRequest.builder()
                    .bucket(bucket)
                    .marker(startWith)
                    .build();
            response = s3Client.listObjects(request);
            result.addAll(response.contents());
            if (!response.isTruncated()) {
                break;
            }
            startWith = response.nextMarker();
        }
        return Collections.unmodifiableList(result);
    }

    public List<String> getAllKeys(Profile profile, String bucket) {
        return getAllObjects(profile, bucket).stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    public File download(Profile profile, String bucket, String object, String destination) {
        S3Client s3Client = S3ClientFactory.create(profile);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(object)
                .build();
        File file = new File(destination);
        s3Client.getObject(request, ResponseTransformer.toFile(file));
        return file;
    }

    public void addObject(Profile profile, String bucket, String key, InputStream source, long length) {
        S3Client s3Client = S3ClientFactory.create(profile);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(source, length));
    }

    private static class S3ClientFactory {

        static S3Client create(Profile profile) {
            return S3Client.builder()
                    .region(Region.of(profile.getRegion()))
                    .credentialsProvider(() -> AwsBasicCredentials.create(
                            profile.getCredentials().getAccessKey(),
                            profile.getCredentials().getSecretKey()
                    ))
                    .endpointOverride(URI.create(profile.getEndpoint()))
                    .build();
        }

    }

}
