package com.patlatarlagna.mapper;

import com.patlatarlagna.dto.PhotoDto;
import com.patlatarlagna.entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoDto toDto(Photo photo);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Photo toEntity(PhotoDto dto);
}
