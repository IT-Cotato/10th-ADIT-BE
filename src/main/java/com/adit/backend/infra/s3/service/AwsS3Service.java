package com.adit.backend.infra.s3.service;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public List<Image> uploadFile(List<String> imageUrlList, String dirName) {
		List<Image> imageList = new ArrayList<>();
		imageUrlList.forEach(imageUrl -> {
			// URL 정규화: 프로토콜이 없으면 "https://" 추가
			String normalizedUrl = normalizeUrl(imageUrl);
			MultipartFile file = ImageUtil.convertUrlToMultipartFile(normalizedUrl);
			String originalFilename = file.getOriginalFilename();
			String fileName = createFileName(originalFilename, dirName);

			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(file.getSize());
			objectMetadata.setContentType(file.getContentType());

			try (InputStream inputStream = file.getInputStream()) {
				amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));
				log.info("[S3] 파일 업로드 성공: 파일명 = {}", fileName);
			} catch (IOException e) {
				log.error("[S3] 파일 업로드 실패: 원본 파일명 = {}, dirName = {}", originalFilename, dirName);
				throw new S3Exception(S3_UPLOAD_FAILED);
			}
			String imageUrlFromBucket = getUrlFromBucket(fileName);
			Image image = Image.builder().url(imageUrlFromBucket).build();
			imageList.add(image);
		});
		return imageList;
	}

	// 기존 이미지 제거 후 동일 경로에 새 이미지 업데이트 후 URL 반환
	public String updateImage(String oldImageUrl, MultipartFile newImage) {
		try {
			AmazonS3URI oldS3Uri = new AmazonS3URI(oldImageUrl);
			String oldKey = oldS3Uri.getKey();
			amazonS3.deleteObject(new DeleteObjectRequest(bucket, oldKey));
			log.info("[S3] 기존 이미지 삭제 완료: key = {}", oldKey);
			String dirName = extractPathWithoutFileName(oldKey);
			String newKey = createFileName(newImage.getOriginalFilename(), dirName);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(newImage.getContentType());
			metadata.setContentLength(newImage.getSize());
			amazonS3.putObject(new PutObjectRequest(bucket, newKey, newImage.getInputStream(), metadata)
				.withCannedAcl(CannedAccessControlList.PublicRead));

			String newFileUrl = getUrlFromBucket(newKey);
			log.info("[S3] 신규 이미지 업로드 완료: newFileUrl = {}", newFileUrl);

			return newFileUrl;
		} catch (Exception e) {
			throw new S3Exception(S3_UPDATE_FAILED);
		}
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

	// 파일명을 난수화하기 위해 UUID를 활용
	private String createFileName(String fileName, String dirName) {
		return dirName + "/" + UUID.randomUUID().toString().concat(getFileExtension(fileName));
	}

	// "."의 존재 유무만 판단 (잘못된 형식이면 S3Exception 발생)
	private String getFileExtension(String fileName) {
		try {
			return fileName.substring(fileName.lastIndexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
			log.error("[S3] 잘못된 파일 형식: 파일명 = {}", fileName);
			throw new S3Exception(S3_INVALID_FILE);
		}
	}

	private String getUrlFromBucket(String fileName) {
		return s3Client.getUrl(bucket, fileName).toString();
	}

	public String extractPathWithoutFileName(String oldKey) {
		int lastSlashIndex = oldKey.lastIndexOf('/');
		if (lastSlashIndex != -1) {
			return oldKey.substring(0, lastSlashIndex);
		}
		return oldKey;
	}

	/**
	 * URL이 http:// 또는 https:// 로 시작하지 않는 경우, 기본적으로 "https://"를 추가하여 정규화 합니다.
	 */
	private String normalizeUrl(String imageUrl) {
		if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
			// 필요에 따라 "https://" 프로토콜이 보장된 브런치스토리 URL 처리
			return "https://" + imageUrl;
		}
		return imageUrl;
	}
}
