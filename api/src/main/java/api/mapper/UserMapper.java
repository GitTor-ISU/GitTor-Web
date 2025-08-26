package api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import api.dtos.UserDto;
import api.entities.User;

/**
 * {@link UserMapper}.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    /**
     * Convert to DTO.
     *
     * @param user User model
     * @return {@link UserDto}
     */
    public UserDto toDto(User user);

    /**
     * Update {@link User} info with {@link UserDto}.
     *
     * @param user User to update
     * @param userDto Update information
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    public void update(@MappingTarget User user, UserDto userDto);
}
