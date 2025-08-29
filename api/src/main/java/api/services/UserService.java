package api.services;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import api.dtos.UserDto;
import api.entities.MimeType;
import api.entities.Role;
import api.entities.S3Object;
import api.entities.User;
import api.exceptions.DuplicateEntityException;
import api.exceptions.EntityNotFoundException;
import api.mapper.UserMapper;
import api.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link UserService}.
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private S3ObjectService s3ObjectService;
    @Autowired
    private MimeTypeService mimeTypeService;

    @Value("${api.avatar.max:5000000}")
    private long maxAvatarSize;

    private static final Set<MediaType> ALLOWED_AVATAR_TYPES = Set.of(
        MediaType.IMAGE_PNG,
        MediaType.IMAGE_JPEG,
        MediaType.parseMediaType("image/svg+xml"),
        MediaType.IMAGE_GIF
    );

    /**
     * Load user with authorities by username.
     *
     * @param username Username
     * @return {@link User}
     */
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithAuthorities(username)
            .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' found"));
        return user;
    }

    /**
     * Find user.
     *
     * @param id User id
     * @return {@link Optional} {@link User}
     */
    @Transactional(readOnly = true)
    public Optional<User> find(int id) {
        return userRepository.findById(id);
    }

    /**
     * Find user.
     *
     * @param username Username
     * @return {@link Optional} {@link User}
     */
    @Transactional(readOnly = true)
    public Optional<User> find(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Check if user exists.
     *
     * @param id User id
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean exists(int id) {
        return userRepository.existsById(id);
    }

    /**
     * Check if user exists.
     *
     * @param username Username
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return find(username).isPresent();
    }

    /**
     * Get all users.
     *
     * @param pageable {@link Pageable}
     * @return User ids.
     */
    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Get user.
     *
     * @param id User id
     * @return {@link User}
     */
    @Transactional(readOnly = true)
    public User get(int id) {
        User user = find(id)
            .orElseThrow(() -> new EntityNotFoundException("User " + id + " not found."));
        return user;
    }

    /**
     * Get user.
     *
     * @param username Username
     * @return {@link User}
     */
    @Transactional(readOnly = true)
    public User get(String username) {
        User user = find(username)
            .orElseThrow(() -> new EntityNotFoundException("User '" + username + "' not found."));
        return user;
    }

    /**
     * Update user.
     *
     * @param id User id
     * @param dto Updated info
     * @return {@link User}
     */
    @Transactional
    public User update(int id, UserDto dto) {
        return update(get(id), dto);
    }

    /**
     * Update user.
     *
     * @param user User
     * @param dto Updated info
     * @return {@link User}
     */
    @Transactional
    public User update(User user, UserDto dto) {
        if (dto.getUsername() != null && !StringUtils.hasText(dto.getUsername())) {
            throw new IllegalArgumentException("Username must not be empty.");
        }

        if (StringUtils.hasText(dto.getUsername())
            && !Objects.equals(dto.getUsername(), user.getUsername())
            && exists(dto.getUsername())
        ) {
            throw new DuplicateEntityException("Username '" + dto.getUsername() + "' already exists.");
        }

        userMapper.update(user, dto);
        return save(user);
    }

    /**
     * Save user.
     *
     * @param user User
     * @return {@link User}
     */
    @Transactional
    public User save(User user) {
        User saved = userRepository.save(user);
        log.info("User saved: " + user);
        return saved;
    }

    /**
     * Delete user.
     *
     * @param id User id
     */
    @Transactional
    public void delete(int id) {
        if (!exists(id)) {
            throw new EntityNotFoundException("User " + id + " not found.");
        }
        userRepository.deleteById(id);
        log.info("User deleted: " + id);
    }

    /**
     * Delete user.
     *
     * @param user User
     */
    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
        log.info("User deleted: " + user);
    }

    /**
     * Get user roles.
     *
     * @param id User
     * @return User's roles
     */
    @Transactional(readOnly = true)
    public Set<Role> getRoles(int id) {
        return getRoles(get(id));
    }

    /**
     * Get user roles.
     *
     * @param user User
     * @return User's roles
     */
    @Transactional(readOnly = true)
    public Set<Role> getRoles(User user) {
        Hibernate.initialize(user.getRoles());
        return user.getRoles();
    }

    /**
     * Update user avatar.
     *
     * @param id User id
     * @param avatar Avatar
     * @throws IOException Failed to get input stream
     */
    @Transactional
    public void updateAvatar(int id, MultipartFile avatar) throws IOException {
        updateAvatar(get(id), avatar);
    }

    /**
     * Update user avatar.
     *
     * @param user User
     * @param avatar Avatar
     * @throws IOException Failed to get input stream
     */
    @Transactional
    public void updateAvatar(User user, MultipartFile avatar) throws IOException {
        if (avatar.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }
        if (avatar.getSize() > maxAvatarSize) {
            throw new IllegalArgumentException("File size exceeds limit (" + maxAvatarSize + " bytes).");
        }
        String contentType = avatar.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(MediaType.parseMediaType(contentType))) {
            throw new IllegalArgumentException("Only PNG, JPEG, SVG and GIF images are allowed.");
        }

        final S3Object oldAvatar = user.getAvatar();
        MimeType mimeType = mimeTypeService.getOrCreateByName(contentType);
        S3Object newAvatar = S3Object.builder()
            .key(S3ObjectService.AVATAR_DIR + user.getId() + "_" + UUID.randomUUID())
            .size(avatar.getSize())
            .mimeType(mimeType)
            .build();

        s3ObjectService.save(newAvatar, avatar.getInputStream());
        user.setAvatar(newAvatar);
        save(user);

        if (oldAvatar != null) {
            s3ObjectService.delete(oldAvatar);
        }
    }

    /**
     * Delete user avatar.
     *
     * @param id User id
     */
    @Transactional
    public void deleteAvatar(int id) {
        deleteAvatar(get(id));
    }

    /**
     * Delete user avatar.
     *
     * @param user User
     */
    @Transactional
    public void deleteAvatar(User user) {
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw new EntityNotFoundException("User '" + user.getUsername() +  "' avatar not found.");
        }

        user.setAvatar(null);
        save(user);
        s3ObjectService.delete(s3Object);
    }
}
