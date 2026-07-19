package com.patlatarlagna.mapper;

import com.patlatarlagna.dto.MatchPreferenceDto;
import com.patlatarlagna.entity.MatchPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchPreferenceMapper {
    
    @Mapping(source = "user.id", target = "userId")
    MatchPreferenceDto toDto(MatchPreference preference);

    @Mapping(target = "user", ignore = true)
    MatchPreference toEntity(MatchPreferenceDto dto);
}
