package api.controllers;

import api.services.S3ObjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link TorrentController}.
 */
@RestController
@RequestMapping("/torrents")
@Tag(
    name = "Torrents",
    description = "Torrents represent a repository stored in the system."
)
public class TorrentController {

    @Autowired
    private S3ObjectService s3;

    @PostMapping("")
    public void uploadTorrent() {
        // Implementation goes here
    }

    @PutMapping("/{id}")
    public void updateTorrent() {
        // Implementation goes here
    }

    @GetMapping("/{id}")
    public void getTorrent() {
        // Implementation goes here
    }
}
