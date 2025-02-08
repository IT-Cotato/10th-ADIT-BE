package com.adit.backend.global.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtil {

	public static final String FILE_NAME_PARAM = "fname=";
	public static final String HTTPS = "https://";
	public static final String HTTP = "http://";

	public static String extractFileName(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			String query = url.getQuery();
			StringBuilder fileName = new StringBuilder(new File(url.getPath()).getName());
			if (query != null && !query.isEmpty()) {
				for (String param : query.split("&")) {
					if (param.startsWith("type=")) {
						fileName.append("?").append(param);
						break;
					}
				}
			}
			return fileName.isEmpty() ? "unknown" : fileName.toString();
		} catch (Exception e) {
			return "unknown";
		}
	}

	public static String normalizeUrl(String imageUrl) {
		if (imageUrl.contains(FILE_NAME_PARAM)) {
			String fileName = imageUrl.substring(imageUrl.indexOf(FILE_NAME_PARAM) + FILE_NAME_PARAM.length());
			fileName = fileName.contains("&") ? fileName.substring(0, fileName.indexOf("&")) : fileName;
			String decoded = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
			if (decoded.startsWith(HTTP) || decoded.startsWith(HTTPS)) {
				return decoded;
			}
		}
		if (imageUrl.startsWith("//")) {
			return "https:" + imageUrl;
		}
		if (!imageUrl.startsWith(HTTP) && !imageUrl.startsWith(HTTPS)) {
			return HTTPS + imageUrl;
		}
		return imageUrl;
	}

	public static String extractPathWithoutFileName(String oldKey) {
		int lastSlashIndex = oldKey.lastIndexOf('/');
		if (lastSlashIndex != -1) {
			return oldKey.substring(0, lastSlashIndex);
		}
		return oldKey;
	}

	// "."의 존재 유무만 판단 (잘못된 형식이면 S3Exception 발생)
	public static String getFileExtension(String fileName, String contentType) {
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex != -1) {
			return fileName.substring(dotIndex);
		} else {
			if (contentType != null) {
				switch (contentType.toLowerCase()) {
					case "image/jpeg":
						return ".jpg";
					case "image/png":
						return ".png";
					case "image/gif":
						return ".gif";
					// 필요한 경우 추가 MIME 타입 처리
					default:
						log.warn("[S3] 인식되지 않은 MIME 타입: {}. 기본 확장자(.jpg)를 사용합니다.", contentType);
						return ".jpg";
				}
			} else {
				log.warn("[S3] 콘텐츠 타입을 확인할 수 없습니다.");
				return ".jpg";
			}
		}
	}

	public static String createFileName(String originalFileName, String dirName, String contentType) {
		return dirName + "/" + UUID.randomUUID() + getFileExtension(originalFileName, contentType);
	}
}