package com.patlatarlagna.service;

import com.patlatarlagna.dto.MessageDto;
import java.util.List;

public interface ChatService {
    MessageDto sendMessage(Long senderId, Long receiverId, String content);
    List<MessageDto> getChatHistory(Long userOneId, Long userTwoId);
}
