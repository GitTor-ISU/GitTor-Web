package api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import api.dtos.CreateTorrentDto;
import api.dtos.TorrentDto;
import api.dtos.UpdateTorrentDto;
import api.entities.Torrent;

/**
 * {@link TorrentMapper}.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TorrentMapper {
    /**
     * Convert to DTO.
     *
     * @param torrent Torrent model
     * @return {@link TorrentDto}
     */
    @Mapping(target = "fileSize", source = "file.size")
    @Mapping(target = "uploaderId", source = "uploader.id")
    @Mapping(target = "uploaderUsername", source = "uploader.username")
    TorrentDto toDto(Torrent torrent);

    /**
     * Update {@link Torrent} metadata with {@link UpdateTorrentDto}.
     *
     * @param torrent Torrent to update
     * @param updateDto Update information
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "file", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget Torrent torrent, UpdateTorrentDto updateDto);

    /**
     * Convert {@link CreateTorrentDto} to {@link Torrent}. Note: file and uploader must be set
     * separately.
     *
     * @param createDto Create DTO
     * @return {@link Torrent}
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "file", ignore = true)
    @Mapping(target = "uploader", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Torrent toEntity(CreateTorrentDto createDto);
}
