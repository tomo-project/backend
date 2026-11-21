package com.tomo.todo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/** todos 테이블에 대응하며 다른 Aggregate는 식별자로 참조하는 To-do 엔티티. */
@Entity
@Table(name = "todos", uniqueConstraints = @UniqueConstraint(columnNames = {"todo_id", "users_id"}))
public class Todo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "todo_id")
  private Long id;

  @Column(name = "users_id", nullable = false, length = 36, updatable = false)
  private String userId;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "category_nm", nullable = false, length = 100)
  private String categoryName;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  private TodoPriority priority;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(name = "estimated_minutes", nullable = false)
  private int estimatedMinutes;

  @Column(name = "reminder_at")
  private LocalDateTime reminderAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected Todo() {}

  private Todo(
      String userId,
      String title,
      String categoryName,
      TodoPriority priority,
      LocalDate dueDate,
      int estimatedMinutes,
      LocalDateTime reminderAt,
      LocalDateTime createdAt) {
    this.userId = userId;
    this.title = title;
    this.categoryName = categoryName;
    this.priority = priority;
    this.dueDate = dueDate;
    this.estimatedMinutes = estimatedMinutes;
    this.reminderAt = reminderAt;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  /**
   * 인증 사용자에게 속하는 미완료 To-do를 만든다.
   *
   * @param userId 인증 사용자 식별자
   * @param title 제목
   * @param categoryName 카테고리명
   * @param priority 우선순위
   * @param dueDate 마감일
   * @param estimatedMinutes 예상 학습시간(분)
   * @param reminderAt UTC 알림 시각, 미설정 시 null
   * @param createdAt UTC 생성 시각
   * @return 신규 To-do
   * @throws IllegalArgumentException 예상 학습시간이 양수가 아닌 경우
   */
  public static Todo create(
      String userId,
      String title,
      String categoryName,
      TodoPriority priority,
      LocalDate dueDate,
      int estimatedMinutes,
      LocalDateTime reminderAt,
      LocalDateTime createdAt) {
    if (estimatedMinutes <= 0) {
      throw new IllegalArgumentException("예상 학습시간은 0보다 커야 한다.");
    }

    return new Todo(
        userId, title, categoryName, priority, dueDate, estimatedMinutes, reminderAt, createdAt);
  }

  @PreUpdate
  private void updateTimestamp() {
    updatedAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  /**
   * 지정한 사용자가 이 To-do의 소유자인지 확인한다.
   *
   * @param candidateUserId 확인할 사용자 식별자
   * @return 소유자이면 true
   */
  public boolean isOwnedBy(String candidateUserId) {
    return userId != null && Objects.equals(userId, candidateUserId);
  }

  /**
   * 완료 시각 유무로 완료 여부를 반환한다.
   *
   * @return 완료되었으면 true
   */
  public boolean isCompleted() {
    return completedAt != null;
  }

  /**
   * 삭제 시각 유무로 삭제 여부를 반환한다.
   *
   * @return 삭제되었으면 true
   */
  public boolean isDeleted() {
    return deletedAt != null;
  }

  public Long getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public TodoPriority getPriority() {
    return priority;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public int getEstimatedMinutes() {
    return estimatedMinutes;
  }

  public LocalDateTime getReminderAt() {
    return reminderAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
