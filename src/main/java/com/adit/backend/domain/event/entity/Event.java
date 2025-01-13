package com.adit.backend.domain.event.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.adit.backend.domain.image.entity.Image;
import com.adit.backend.domain.notification.entity.Notification;
import com.adit.backend.domain.place.entity.CommonPlace;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private CommonPlace place;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String memo;
    private Boolean visited;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // 연관관계 메서드
    public void addNotification(Notification notification) {
        this.notifications.add(notification);
        notification.setEvent(this);
    }

    public void addImage(Image image) {
        this.images.add(image);
        image.setEvent(this);
    }

    // 팩토리 메서드
    public static Event createEvent(String name, String category, LocalDateTime startDate, LocalDateTime endDate, String memo, Boolean visited) {
        Event event = new Event();
        event.setName(name);
        event.setCategory(category);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setMemo(memo);
        event.setVisited(visited);
        return event;
    }
}
