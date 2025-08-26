package api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import api.dtos.AuthorityDto;
import api.entities.Authority;

/**
 * {@link AuthorityMapper}.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthorityMapper {
    /**
     * Convert to DTO.
     *
     * @param authority Authority model
     * @return {@link AuthorityDto}
     */
    public AuthorityDto toDto(Authority authority);
}
