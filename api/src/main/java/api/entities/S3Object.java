package api.entities;

import api.components.S3ObjectEntityListener;
import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link S3Object}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(S3ObjectEntityListener.class)
@Table(name = "s3objects")
public class S3Object {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "object_key", unique = true, nullable = false)
    private String key;

    private long size;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mime_type_id", nullable = false)
    private MimeType mimeType;
}
