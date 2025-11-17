package api.controllers;

import api.services.S3ObjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

	private static final String TORRENT_MIME = "application/x-bittorrent";

	@GetMapping("/{id}")
	public ResponseEntity<Resource> getTorrent(@PathVariable long id) {
		// Implementation goes here
		return ResponseEntity.notFound().build();
	}

	@PostMapping("")
	public void uploadTorrent(@RequestParam MultipartFile file) {
		// Implementation goes here
	}

	@PutMapping("/{id}")
	public void updateTorrent(
		@PathVariable long id,
		@RequestParam MultipartFile file
	) {
		// Implementation goes here
	}
}
