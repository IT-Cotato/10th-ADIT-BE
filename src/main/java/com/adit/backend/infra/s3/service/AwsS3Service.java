package com.adit.backend.infra.s3.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;
import static com.adit.backend.global.util.ImageUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.image.entity.Image;
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

	/**
	 * 기존 uploadFile() 수정: MultipartFile 리스트를 받아 업로드하는 새로운 메서드 추가
	 */
	@Async("imageUploadExecutor")
	public CompletableFuture<List<Image>> uploadFiles(List<MultipartFile> newFiles, String dirName) {
		List<CompletableFuture<Image>> futureList = newFiles.stream()
			.map(file -> CompletableFuture.supplyAsync(() -> {
				try {
					// S3 파일명 생성
					String fileName = createFileName(file.getOriginalFilename(), dirName, file.getContentType());

					// 메타데이터 설정
					ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentType(file.getContentType());
					metadata.setContentLength(file.getSize());

					// S3 업로드
					amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
						.withCannedAcl(CannedAccessControlList.PublicRead));

					return Image.builder().url(getUrlFromBucket(fileName)).build();
				} catch (Exception e) {
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

	/**
	 * 기존 이미지 삭제 후, 새 이미지로 업데이트 (개별 파일)
	 */
	@Async("imageUploadExecutor")
	public CompletableFuture<String> updateImage(String oldImageUrl, MultipartFile newImage) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// 기존 이미지 삭제
				AmazonS3URI oldS3Uri = new AmazonS3URI(oldImageUrl);
				amazonS3.deleteObject(new DeleteObjectRequest(bucket, oldS3Uri.getKey()));

				// 새 이미지 업로드
				String newKey = createFileName(newImage.getOriginalFilename(), "event", newImage.getContentType());

				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentType(newImage.getContentType());
				metadata.setContentLength(newImage.getSize());

				amazonS3.putObject(new PutObjectRequest(bucket, newKey, newImage.getInputStream(), metadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));

				return getUrlFromBucket(newKey);
			} catch (Exception e) {
				throw new S3Exception(S3_UPDATE_FAILED);
			}
		}, imageUploadExecutor);
	}

	/**
	 * 여러 개의 기존 이미지 삭제 후, 새로운 이미지로 한 번에 업데이트
	 */
	@Async("imageUploadExecutor")
	public CompletableFuture<List<String>> updateImages(List<String> oldImageUrls, List<MultipartFile> newImages) {
		List<CompletableFuture<String>> updateFutures = new ArrayList<>();

		for (int i = 0; i < oldImageUrls.size(); i++) {
			final int index = i;
			updateFutures.add(CompletableFuture.supplyAsync(() -> {
				try {
					AmazonS3URI oldS3Uri = new AmazonS3URI(oldImageUrls.get(index));
					amazonS3.deleteObject(new DeleteObjectRequest(bucket, oldS3Uri.getKey()));

					String newKey = createFileName(newImages.get(index).getOriginalFilename(), "event",
						newImages.get(index).getContentType());

					ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentType(newImages.get(index).getContentType());
					metadata.setContentLength(newImages.get(index).getSize());

					amazonS3.putObject(new PutObjectRequest(bucket, newKey, newImages.get(index).getInputStream(), metadata)
						.withCannedAcl(CannedAccessControlList.PublicRead));

					return getUrlFromBucket(newKey);
				} catch (Exception e) {
					throw new S3Exception(S3_UPDATE_FAILED);
				}
			}, imageUploadExecutor));
		}

		return CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0]))
			.thenApply(voidResult -> updateFutures.stream()
				.map(CompletableFuture::join)
				.toList()
			);
	}

	public void deleteFile(String fileUrl) {
		try {
			AmazonS3URI s3Uri = new AmazonS3URI(fileUrl);
			amazonS3.deleteObject(new DeleteObjectRequest(bucket, s3Uri.getKey()));
			log.info("[S3] 파일 삭제 완료: 파일명 = {}, 버킷 이름 = {}", s3Uri.getKey(), bucket);
		} catch (Exception e) {
			log.info("[S3] 파일 삭제 실패: 경로 = {}", fileUrl);
			throw new S3Exception(S3_DELETE_FAILED);
		}
	}

	private String getUrlFromBucket(String fileName) {
		return s3Client.getUrl(bucket, fileName).toString();
	}
}

