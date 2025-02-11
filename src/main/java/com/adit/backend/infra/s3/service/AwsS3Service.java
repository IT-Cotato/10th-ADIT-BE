package com.adit.backend.infra.s3.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;
import static com.adit.backend.global.util.ImageUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.global.util.ImageUtil;
import com.adit.backend.infra.s3.exception.S3Exception;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AwsS3Service {

	private final AmazonS3 amazonS3;
	private final AmazonS3Client s3Client;
	private final Executor imageUploadExecutor;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Async("imageUploadExecutor")
	public CompletableFuture<List<Image>> uploadFile(List<String> imageUrlList, String dirName) {
		log.info("[S3] 파일 업로드 시작 : {}", imageUrlList);

		List<CompletableFuture<Image>> futureList = imageUrlList.stream()
			.map(imageUrl -> CompletableFuture.supplyAsync(() -> {
				try {
					// URL 연결 설정
					String normalizedUrl = ImageUtil.normalizeUrl(imageUrl);
					URL url = new URL(normalizedUrl);
					URLConnection connection = url.openConnection();
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(5000);

					String contentType = connection.getContentType();
					String originalFilename = ImageUtil.extractFileName(normalizedUrl);
					String fileName = createFileName(originalFilename, dirName, contentType);

					// ObjectMetadata 설정
					ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentType(contentType);
					long contentLength = connection.getContentLengthLong();
					if (contentLength > 0) {
						metadata.setContentLength(contentLength);
					}

					// InputStream으로 S3에 멀티파트 업로드
					try (InputStream inputStream = connection.getInputStream()) {
						amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata)
							.withCannedAcl(CannedAccessControlList.PublicRead));
						log.info("[S3] 파일 업로드 성공: 파일명 = {}", fileName);
					}

					String imageUrlFromBucket = getUrlFromBucket(fileName);
					return Image.builder().url(imageUrlFromBucket).build();

				} catch (IOException e) {
					log.error("[S3] 파일 업로드 실패: URL = {}, dirName = {}", imageUrl, dirName, e);
					throw new S3Exception(S3_UPLOAD_FAILED);
				}
			}, imageUploadExecutor))
			.toList();

		return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
			.thenApply(voidResult -> futureList.stream()
				.map(CompletableFuture::join)
				.toList()
			);
	}

	// 기존 이미지 제거 후 동일 경로에 새 이미지 업데이트 후 URL 반환
	@Async("imageUploadExecutor")
	public CompletableFuture<String> updateImage(String oldImageUrl, MultipartFile newImage) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// 기존 이미지 삭제
				AmazonS3URI oldS3Uri = new AmazonS3URI(oldImageUrl);
				String oldKey = oldS3Uri.getKey();
				amazonS3.deleteObject(new DeleteObjectRequest(bucket, oldKey));
				log.info("[S3] 기존 이미지 삭제 완료: key = {}", oldKey);

				// 새 이미지 업로드
				String dirName = ImageUtil.extractPathWithoutFileName(oldKey);
				String newKey = createFileName(newImage.getOriginalFilename(), dirName,
					newImage.getContentType());

				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentType(newImage.getContentType());
				metadata.setContentLength(newImage.getSize());

				amazonS3.putObject(new PutObjectRequest(bucket, newKey, newImage.getInputStream(), metadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));

				String newFileUrl = getUrlFromBucket(newKey);
				log.info("[S3] 신규 이미지 업로드 완료: newFileUrl = {}", newFileUrl);

				return newFileUrl;
			} catch (Exception e) {
				log.error("[S3] 이미지 업데이트 실패: oldImageUrl = {}", oldImageUrl);
				throw new S3Exception(S3_UPDATE_FAILED);
			}
		}, imageUploadExecutor);
	}

	public void deleteFile(String fileUrl) {
		try {
			AmazonS3URI s3Uri = new AmazonS3URI(fileUrl);
			String key = s3Uri.getKey();
			amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
			log.info("[S3] 파일 삭제 완료: 파일명 = {}, 버킷 이름 = {}", key, bucket);
		} catch (Exception e) {
			log.info("[S3] 파일 삭제 실패: 경로 = {}", fileUrl);
			throw new S3Exception(S3_DELETE_FAILED);
		}
	}

	private String getUrlFromBucket(String fileName) {
		return s3Client.getUrl(bucket, fileName).toString();
	}

}
