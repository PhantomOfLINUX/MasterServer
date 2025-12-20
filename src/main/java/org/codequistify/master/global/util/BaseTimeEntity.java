package org.codequistify.master.global.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeEntity {
  @CreatedDate
  @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
  @Column(name = "created_date", updatable = false, columnDefinition = "DATETIME(0)")
  private LocalDateTime createdDate;

  @LastModifiedDate
  @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
  @Column(name = "modified_date", columnDefinition = "DATETIME(0)")
  private LocalDateTime modifiedDate;
}
