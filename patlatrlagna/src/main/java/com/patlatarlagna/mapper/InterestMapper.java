package com.patlatarlagna.mapper;

import com.patlatarlagna.dto.InterestDto;
import com.patlatarlagna.entity.Interest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterestMapper {
    
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "senderPhoto", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    @Mapping(target = "receiverPhoto", ignore = true)
    InterestDto toDto(Interest interest);

    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Interest toEntity(InterestDto dto);
}
