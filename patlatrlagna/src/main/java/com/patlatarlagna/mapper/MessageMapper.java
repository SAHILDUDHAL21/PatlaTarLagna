package com.patlatarlagna.mapper;

import com.patlatarlagna.dto.MessageDto;
import com.patlatarlagna.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    MessageDto toDto(Message message);

    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    Message toEntity(MessageDto dto);
}
