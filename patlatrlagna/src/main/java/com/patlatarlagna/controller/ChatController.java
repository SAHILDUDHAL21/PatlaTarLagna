package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.MessageDto;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long receiverId,
            @RequestParam String content) {
        MessageDto message = chatService.sendMessage(userDetails.getId(), receiverId, content);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", message));
    }

    @GetMapping("/history/{receiverId}")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long receiverId) {
        List<MessageDto> history = chatService.getChatHistory(userDetails.getId(), receiverId);
        return ResponseEntity.ok(ApiResponse.success("Chat history retrieved", history));
    }
}
