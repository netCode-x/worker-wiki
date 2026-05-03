package com.codebit.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Comment;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ms_note")
@EntityListeners(AuditingEntityListener.class)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("随记ID")
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    @Comment("随记内容")
    private String content;

    @Column(name = "author_id", nullable = false)
    @Comment("作者ID")
    private Long authorId;

    @Column(name = "is_private", columnDefinition = "tinyint(1) default 0")
    @Comment("是否私密：0-公开，1-仅自己可见")
    private Boolean isPrivate;

    @Column(nullable = false)
    @Comment("状态：0-草稿，1-正常，2-已删除")
    private Integer status;

    @CreatedDate
    @Column(name = "create_date", updatable = false)
    @Comment("创建时间")
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "update_date")
    @Comment("更新时间")
    private LocalDateTime updateDate;
}