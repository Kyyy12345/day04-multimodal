package com.study.day04multimodal.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Service
public class MultimodalService {
    private final ChatClient chatClient;
    
    public MultimodalService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            MimeTypeUtils.IMAGE_JPEG_VALUE, MimeTypeUtils.IMAGE_PNG_VALUE
    );
    
    public String analyzeImage(MultipartFile file, String conversationId) {
        // 적합한지 이미지 검증
        validate(file);
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());

        return chatClient.prompt()
                .user(u -> u.text("업로드 된 영수증 이미지에서 상호명, 총금액, 날짜, 구매항목을 추출해주세요.")
                        .media(mimeType, resource))
                .call()
                .content();

    }

    private static ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 리소스 읽는 중 오류 발생");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IMG 파일 업로드 해주세요.");
        }
        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "JPEG나 PNG 이미지만 지원합니다. 받은 타입: " + contentType);
        }
    }
    
    

}
