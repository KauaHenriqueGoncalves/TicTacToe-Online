package com.tic.tac.toe.domain.entity;

import com.tic.tac.toe.domain.entity.enums.RoomStatus;
import com.tic.tac.toe.domain.entity.pk.RoomPlayer;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "rooms",
        indexes = {
                @Index(name = "idx_room_id_name", columnList = "id, name")
        }
)
public class Room {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status;

    @Column(name = "current_player_id")
    private UUID currentPlayerId;

    @Column(name = "winner_id_snap")
    private UUID winnerId;

    @Column(name = "winner_name_snap", length = 150)
    private String winnerName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private HashSet<RoomPlayer> players = new HashSet<>();

    public Room() {
    }

    public Room(UUID id, String name, RoomStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Room(UUID id, String name, UUID ownerId, RoomStatus status, UUID currentPlayerId, UUID winnerId, String winnerName, LocalDateTime createdAt, HashSet<RoomPlayer> players) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.status = status;
        this.currentPlayerId = currentPlayerId;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.createdAt = createdAt;
        this.players = players;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public UUID getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(UUID currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public HashSet<RoomPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(HashSet<RoomPlayer> players) {
        this.players = players;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
