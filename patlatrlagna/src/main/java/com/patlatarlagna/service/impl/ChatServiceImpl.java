package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.MessageDto;
import com.patlatarlagna.entity.Message;
import com.patlatarlagna.entity.Profile;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.mapper.MessageMapper;
import com.patlatarlagna.repository.*;
import com.patlatarlagna.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(MessageRepository messageRepository, MatchRepository matchRepository,
                           BlockRepository blockRepository, UserRepository userRepository,
                           ProfileRepository profileRepository, MessageMapper messageMapper,
                           SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.matchRepository = matchRepository;
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public MessageDto sendMessage(Long senderId, Long receiverId, String content) {
        if (blockRepository.existsByBlockerIdAndBlockedId(receiverId, senderId)) {
            throw new BadRequestException("You cannot send messages to this user");
        }

        // Restrict messaging to mutually matched users
        if (matchRepository.findMatchBetween(senderId, receiverId).isEmpty()) {
            throw new BadRequestException("You can only message profiles you are mutually matched with");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .read(false)
                .sentAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        MessageDto dto = populateMessageNames(saved);

        // Broadcast to WebSocket destination: /queue/messages-{userId}
        try {
            messagingTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/messages",
                    dto
            );
        } catch (Exception e) {
            System.err.println("WebSocket dispatch failed: " + e.getMessage());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> getChatHistory(Long userOneId, Long userTwoId) {
        // Automatically mark all messages from userTwo to userOne as read
        List<Message> history = messageRepository.findChatHistory(userOneId, userTwoId);
        
        history.stream()
               .filter(msg -> msg.getReceiver().getId().equals(userOneId) && !msg.isRead())
               .forEach(msg -> {
                   msg.setRead(true);
                   msg.setReadAt(LocalDateTime.now());
                   messageRepository.save(msg);
               });

        return history.stream()
                .map(this::populateMessageNames)
                .collect(Collectors.toList());
    }

    private MessageDto populateMessageNames(Message message) {
        MessageDto dto = messageMapper.toDto(message);
        
        String senderName = profileRepository.findByUserId(message.getSender().getId())
                .map(Profile::getName).orElse("User " + message.getSender().getId());
        String receiverName = profileRepository.findByUserId(message.getReceiver().getId())
                .map(Profile::getName).orElse("User " + message.getReceiver().getId());

        dto.setSenderName(senderName);
        dto.setReceiverName(receiverName);
        
        return dto;
    }
}
