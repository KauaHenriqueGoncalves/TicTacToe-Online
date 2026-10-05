package com.tic.tac.toe.domain.entity.pk;

import com.tic.tac.toe.domain.entity.Room;
import com.tic.tac.toe.domain.entity.User;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "room_players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_id_user_id", columnNames = {"room_id", "user_id"})
        }

)
public class RoomPlayer {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_playing", nullable = false)
    private Boolean isPlaying;

    @Column(name = "ready", nullable = false)
    private Boolean ready;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RoomPlayer() {
    }

    public RoomPlayer(UUID id, Room room, User user, Boolean isPlaying, Boolean ready, LocalDateTime createdAt) {
        this.id = id;
        this.room = room;
        this.user = user;
        this.isPlaying = isPlaying;
        this.ready = ready;
        this.createdAt = createdAt;
    }

    public static RoomPlayer init(Room room, User user) {
        if (room == null || user == null) {
            throw new RuntimeException("Room or user is required");
        }
        return new RoomPlayer(
                UUID.randomUUID(),
                room,
                user,
                false,
                false,
                LocalDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getPlaying() {
        return isPlaying;
    }

    public void setPlaying(Boolean playing) {
        isPlaying = playing;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RoomPlayer roomPlayer = (RoomPlayer) o;
        return Objects.equals(id, roomPlayer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
