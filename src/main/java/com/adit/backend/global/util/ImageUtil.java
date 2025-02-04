package com.adit.backend.global.util;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
			URL url = new URL(imageUrl);
			String contentType = url.openConnection().getContentType();
			String fileName = extractFileName(imageUrl);

			byte[] content;
			try (InputStream inputStream = url.openStream();
				 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				IOUtils.copy(inputStream, outputStream);
				content = outputStream.toByteArray();
			}

			return new CustomMultipartFile(content, fileName, contentType);
		} catch (IOException e) {
			log.error("URL을 MultipartFile로 변환하는 데 실패했습니다.");
			throw new ImageException(IMAGE_EXTRACTION_FAILED);
		}
	}

	private static String extractFileName(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			StringBuilder fileName = new StringBuilder(new File(url.getPath()).getName());
			String query = url.getQuery();
			if (query != null) {
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