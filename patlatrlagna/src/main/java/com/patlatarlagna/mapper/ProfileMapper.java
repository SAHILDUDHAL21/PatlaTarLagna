package com.patlatarlagna.mapper;

import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PhotoMapper.class})
public interface ProfileMapper {
    
    @Mapping(source = "user.id", target = "userId")
    ProfileDto toDto(Profile profile);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Profile toEntity(ProfileDto dto);
}
