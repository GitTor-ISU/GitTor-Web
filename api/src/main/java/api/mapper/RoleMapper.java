package api.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import api.dtos.RoleDto;
import api.entities.Authority;
import api.entities.Role;
import api.services.AuthorityService;

/**
 * {@link RoleMapper}.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class RoleMapper {
    @Autowired
    private AuthorityService authorityService;

    /**
     * Convert to DTO.
     *
     * @param role Role model
     * @return {@link RoleDto}
     */
    @Mapping(target = "authorityIds", source = "authorities", qualifiedByName = "AuthoritiestoAuthorityIds")
    public abstract RoleDto toDto(Role role);

    /**
     * Convert to entity.
     *
     * @param roleDto Role DTO
     * @return {@link Role}
     */
    @Mapping(target = "authorities", source = "authorityIds", qualifiedByName = "AuthorityIdstoAuthorities")
    public abstract Role toEntity(RoleDto roleDto);

    /**
     * Update {@link Role} info with {@link RoleDto}.
     *
     * @param role Role to update
     * @param roleDto Update information
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(
        target = "authorities",
        source = "authorityIds",
        qualifiedByName = "AuthorityIdstoAuthorities",
        conditionQualifiedByName = "notNull"
    )
    public abstract void update(@MappingTarget Role role, RoleDto roleDto);

    /**
     * Ensure object is not null.
     *
     * @param value object
     * @return true if object is not null
     */
    @Named("notNull")
    @Condition
    protected boolean notNull(Object value) {
        return value != null;
    }

    /**
     * Convert {@link Set} of {@link Authority} to {@link Set} of {@link Integer} ids.
     *
     * @param authorities {@link Set} of {@link Authority}
     * @return {@link Set} of {@link Integer} ids
     */
    @Named("AuthoritiestoAuthorityIds")
    protected List<Integer> toAuthorityIds(Set<Authority> authorities) {
        if (authorities == null) {
            return new ArrayList<>();
        }
        return authorities.stream()
            .map(Authority::getId)
            .collect(Collectors.toList());
    }

    /**
     * Convert {@link Set} of {@link Integer} ids to {@link Set} of {@link Authority}.
     *
     * @param ids {@link Set} of {@link Integer} ids
     * @return {@link Set} of {@link Authority}
     */
    @Named("AuthorityIdstoAuthorities")
    protected Set<Authority> toAuthorities(List<Integer> ids) {
        if (ids == null) {
            return new HashSet<>();
        }
        return new HashSet<>(authorityService.get(ids));
    }
}
