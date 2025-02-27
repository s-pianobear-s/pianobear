package kr.pianobear.application.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private Long id;
    private String senderId;
    private String receiverId;
    private Long chatRoomId;
    private String content;
    private String timestamp;
}
