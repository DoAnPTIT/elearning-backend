package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${app.aws.s3.bucket}")
    private String bucketName;

    @Value("${app.aws.s3.endpoint}")
    private String s3Endpoint;

    public String uploadUserImage(Integer userId, MultipartFile file) {
        validateImage(file);

        String fileName = "u" + userId + ".jpg";
        try {
            BufferedImage resized = resizeImage(file, 300, 300);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", os);
            byte[] bytes = os.toByteArray();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(bytes));

            return s3Endpoint + "/" + bucketName + "/" + fileName;

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    public void deleteUserImage(Integer userId) {
        String key = "u" + userId + ".jpg";
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            System.out.println("[WARN] Không thể xóa ảnh S3: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }

        String contentType = URLConnection.guessContentTypeFromName(file.getOriginalFilename());
        if (contentType == null) {
            String filename = file.getOriginalFilename().toLowerCase();
            if (!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
                throw new AppException(ErrorCode.FILE_INVALID_TYPE);
            }
        } else if (!(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new AppException(ErrorCode.FILE_INVALID_TYPE);
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 2MB
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    private BufferedImage resizeImage(MultipartFile file, int width, int height) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();

        return resized;
    }
}
