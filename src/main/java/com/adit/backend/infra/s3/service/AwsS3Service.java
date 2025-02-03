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
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.util.ImageUtil;
import com.adit.backend.infra.s3.exception.S3Exception;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
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

	public List<Image> uploadFile(List<String> imageUrlList, User user) {
		List<Image> imageList = new ArrayList<>();
		imageUrlList.forEach(imageurl -> {
			MultipartFile file = ImageUtil.convertUrlToMultipartFile(imageurl);
			String originalFilename = file.getOriginalFilename();
			String fileName = createFileName(originalFilename, user.getId());

			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(file.getSize());
			objectMetadata.setContentType(file.getContentType());

			try (InputStream inputStream = file.getInputStream()) {
				amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));
				log.info("[S3] 파일 업로드 성공: 파일명 = {}", fileName);
			} catch (IOException e) {
				log.error("[S3] 파일 업로드 실패: 원본 파일명 = {}, userId = {}", originalFilename, user.getId());
				throw new S3Exception(S3_UPLOAD_FAILED);
			}
			String imageUrl = getUrlFromBucket(fileName);
			Image image = Image.builder().url(imageUrl).build();
			imageList.add(image);
		});
		return imageList;
	}

	// 파일명을 난수화하기 위해 UUID를 활용
	private String createFileName(String fileName, Long dirName) {
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

	public void deleteFile(String fileName) {
		amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
		log.info("[S3] 파일 삭제 완료: 파일명 = {}, 버킷 이름 = {}", fileName, bucket);
	}
}
