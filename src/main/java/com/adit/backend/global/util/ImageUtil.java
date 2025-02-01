package com.adit.backend.global.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
	public static MultipartFile convertUrlToMultipartFile(String imageUrl) {
		try (InputStream inputStream = new URL(imageUrl).openStream();
			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			if (bufferedImage == null) {
				log.error("[Image] URL이 유효한 이미지 형식이 아닙니다: {}", imageUrl);
				throw new IllegalArgumentException();
			}
			ImageIO.write(bufferedImage, "jpg", outputStream);

			byte[] multipartFileBytes = outputStream.toByteArray();

			return new MockMultipartFile("file", "image.jpg", "image/jpeg", multipartFileBytes);
		} catch (MalformedURLException e) {
			log.error("유효하지 않은 URL입니다: {}", imageUrl);
			throw new IllegalArgumentException();
		} catch (IOException e) {
			log.error("이미지 변환 중 오류가 발생했습니다: {}", imageUrl);
			throw new RuntimeException();
		}
	}
}