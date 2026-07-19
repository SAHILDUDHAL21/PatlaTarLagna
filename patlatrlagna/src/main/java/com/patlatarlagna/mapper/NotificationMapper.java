package com.patlatarlagna.mapper;

import com.patlatarlagna.dto.NotificationDto;
import com.patlatarlagna.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    
    @Mapping(source = "user.id", target = "userId")
    NotificationDto toDto(Notification notification);

    @Mapping(target = "user", ignore = true)
    Notification toEntity(NotificationDto dto);
}
