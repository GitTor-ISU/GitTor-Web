package api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import api.dtos.ErrorDto;
import api.dtos.TorrentDto;
import api.entities.Torrent;
import api.entities.User;
import api.mapper.TorrentMapper;
import api.services.TorrentService;

/**
 * {@link TorrentController}.
 */
@RestController
@RequestMapping("/torrents")
@Tag(name = "Torrents", description = "Torrents represent a repository stored in the system.")
public class TorrentController {

    @Autowired
    private TorrentService torrentService;

    @Autowired
    private TorrentMapper torrentMapper;

    @Value("${pagination.default-page-size:10}")
    private int defaultPageSize;

    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

    private static final String TORRENT_MIME_TYPE = "application/x-bittorrent";

    /**
     * Upload new torrent. Client sends multipart/form-data with: - "metadata" part (application/json) -
     * "file" part (application/x-bittorrent)
     *
     * @param user Current user
     * @param metadata Torrent metadata
     * @param file Torrent file
     * @return {@link TorrentDto}
     * @throws IOException if file cannot be read
     */
    // region
    @Operation(summary = "Upload Torrent", description = "Upload a new torrent file with metadata.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TorrentDto.class), mediaType = "application/json")),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json"))})
    // endregion
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TorrentDto uploadTorrent(@AuthenticationPrincipal User user, @RequestPart("metadata") TorrentDto metadata,
        @RequestPart("file") MultipartFile file) throws IOException {
        Torrent torrent = torrentService.create(metadata.getName(), metadata.getDescription(), user, file);
        return torrentMapper.toDto(torrent);
    }

    /**
     * Get all torrents with pagination.
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of {@link TorrentDto}
     */
    // region
    @Operation(summary = "List Torrents", description = "Get all torrents with pagination.")
    @ApiResponses({@ApiResponse(responseCode = "200",
        content = @Content(schema = @Schema(implementation = TorrentDto.class), mediaType = "application/json"))})
    // endregion
    @GetMapping("")
    public List<TorrentDto> getAllTorrents(@RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) Integer size) {
        int requestedSize = size != null ? size : defaultPageSize;
        int safeSize = Math.min(requestedSize, maxPageSize);

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());

        return torrentService.getAll(pageable).map(torrentMapper::toDto).getContent();
    }

    /**
     * Get torrent by id.
     *
     * @param id Torrent id
     * @return {@link TorrentDto}
     */
    // region
    @Operation(summary = "Get Torrent", description = "Get torrent metadata by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TorrentDto.class), mediaType = "application/json")),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json"))})
    // endregion
    @GetMapping("/{id}")
    public TorrentDto getTorrent(@PathVariable Long id) {
        Torrent torrent = torrentService.get(id);
        return torrentMapper.toDto(torrent);
    }

    /**
     * Update torrent metadata.
     *
     * @param id Torrent id
     * @param updateDto Update data
     * @return {@link TorrentDto}
     */
    // region
    @Operation(summary = "Update Torrent Metadata", description = "Update torrent name and/or description.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TorrentDto.class), mediaType = "application/json")),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json"))})
    // endregion
    @PutMapping("/{id}")
    public TorrentDto updateTorrent(@PathVariable Long id, @RequestPart("metadata") TorrentDto updateDto) {
        Torrent torrent = torrentService.updateMetadata(id, updateDto.getName(), updateDto.getDescription());
        return torrentMapper.toDto(torrent);
    }

    /**
     * Delete torrent.
     *
     * @param id Torrent id
     */
    // region
    @Operation(summary = "Delete Torrent", description = "Delete a torrent and its file.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = Void.class), mediaType = "application/json")),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json"))})
    // endregion
    @DeleteMapping("/{id}")
    public void deleteTorrent(@PathVariable Long id) {
        torrentService.delete(id);
    }

    /**
     * Download torrent file.
     *
     * @param id Torrent id
     * @return Torrent file
     * @throws IOException if file cannot be read
     */
    // region
    @Operation(summary = "Download Torrent File", description = "Download the actual .torrent file.")
    @ApiResponses({@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/x-bittorrent")),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json"))})
    // endregion
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getTorrentFile(@PathVariable Long id) throws IOException {
        Torrent torrent = torrentService.getWithFile(id);

        byte[] bytes;
        try (InputStream in = torrentService.downloadTorrentFile(id)) {
            bytes = in.readAllBytes();
        }
        ByteArrayResource resource = new ByteArrayResource(bytes);

        // Suggest filename for download
        String filename = torrent.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".torrent";

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(TORRENT_MIME_TYPE))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentLength(bytes.length).body(resource);
    }

    /**
     * Update torrent file.
     *
     * @param id Torrent id
     * @param file New torrent file
     * @throws IOException if file cannot be read
     */
    // region
    @Operation(summary = "Update Torrent File", description = "Replace the torrent file.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = Void.class), mediaType = "application/json")),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json")),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorDto.class), mediaType = "application/json"))})
    // endregion
    @PutMapping("/{id}/file")
    public void updateTorrentFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        torrentService.updateTorrentFile(id, file);
    }
}
