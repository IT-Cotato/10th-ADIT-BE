package com.adit.backend.global.util;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import com.adit.backend.domain.image.exception.ImageException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtil {

	/**
	 * 주어진 이미지 URL을 읽어 MultipartFile로 변환하여 반환한다.
	 *
	 * @param imageUrl 이미지의 URL 문자열
	 * @return 변환된 MultipartFile 객체
	 * @throws Exception 이미지 읽기 및 변환 중 예외 발생 시
	 */
	// URL로부터 데이터를 읽어와서 CustomMultipartFile로 변환하여 반환
	public static MultipartFile convertUrlToMultipartFile(String imageUrl) {
		try {
			// URL에 프로토콜이 누락된 경우 "https://"를 추가함
			String normalizedUrl = normalizeUrl(imageUrl);
			URL url = new URL(normalizedUrl);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			String contentType = connection.getContentType();

			String fileName = extractFileName(normalizedUrl);
			byte[] content;

			try (InputStream inputStream = connection.getInputStream();
				 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				IOUtils.copy(inputStream, outputStream);
				content = outputStream.toByteArray();
			}

			return new CustomMultipartFile(content, fileName, contentType);
		} catch (IOException e) {
			log.error("[Image] URL을 MultipartFile로 변환하는 데 실패했습니다.: {}", imageUrl);
			throw new ImageException(IMAGE_EXTRACTION_FAILED);
		}
	}

	private static String extractFileName(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			String query = url.getQuery();
			if (query != null && query.contains("fname=")) {
				for (String param : query.split("&")) {
					if (param.startsWith("fname=")) {
						String fnameValue = param.substring("fname=".length());
						// 디코딩하여 원래의 URL 형태로 복원
						String decodedFname = URLDecoder.decode(fnameValue, StandardCharsets.UTF_8);
						return decodedFname.isEmpty() ? "unknown" : decodedFname;
					}
				}
			}
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

	private static String normalizeUrl(String imageUrl) {
		if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
			return "https://" + imageUrl;
		}
		return imageUrl;
	}

	// MultipartFile 인터페이스를 구현한 커스텀 클래스
	public static class CustomMultipartFile implements MultipartFile {
		private final byte[] content;
		private final String fileName;
		private final String contentType;

		public CustomMultipartFile(byte[] content, String fileName, String contentType) {
			this.content = content;
			this.fileName = fileName;
			this.contentType = contentType;
		}

		@Override
		public String getName() {
			return fileName;
		}

		@Override
		public String getOriginalFilename() {
			return fileName;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public boolean isEmpty() {
			return content == null || content.length == 0;
		}

		@Override
		public long getSize() {
			return content.length;
		}

		@Override
		public byte[] getBytes() {
			return content;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(content);
		}

		@Override
		public void transferTo(File dest) throws IOException, IllegalStateException {
			Files.write(dest.toPath(), content);
		}
	}
}