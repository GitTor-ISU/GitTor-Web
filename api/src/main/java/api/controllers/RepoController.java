package api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RepoController}.
 */
@RestController
@RequestMapping("/repos")
@Tag(
    name = "Repositories",
    description = "Repositories contain the project files and the revision history for those files."
)
public class RepoController {

    @PostMapping("")
    public void uploadRepo() {
        // Implementation goes here
    }

    @PutMapping("/{id}")
    public void updateRepo() {
        // Implementation goes here
    }

    @GetMapping("/{id}")
    public void getRepo() {
        // Implementation goes here
    }
}
