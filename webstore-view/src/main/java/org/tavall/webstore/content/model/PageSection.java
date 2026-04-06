package org.tavall.webstore.content.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "page_sections")
@Getter
@Setter
public class PageSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_page_id", nullable = false)
    private ContentPage contentPage;

    @Column(nullable = false)
    private String sectionKey;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String sectionType;

    @Column(nullable = false)
    private String placement;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int position = 0;

    @Column(nullable = false)
    private int mobilePosition = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> configuration = new HashMap<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
