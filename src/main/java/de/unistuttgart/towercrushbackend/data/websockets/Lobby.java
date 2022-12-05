package de.unistuttgart.towercrushbackend.data.websockets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
public class Lobby {
    private Set<String> teamA = new HashSet<>();
    private Set<String> teamB = new HashSet<>();
    private Set<String> members = new HashSet<>();
    private LocalDateTime createdAt;

    public void addTeamAMember(final String member) {
        this.teamA.add(member);
    }

    public void addTeamBMember(final String member) {
        this.teamB.add(member);
    }

    public void removeTeamAMember(final String member) {
        this.teamA.remove(member);
    }

    public void removeTeamBMember(final String member) {
        this.teamB.remove(member);
    }

    public void addMember(final String member) {
        this.members.add(member);
    }

    public void removeMember(final String member) {
        this.members.remove(member);
    }

    public void setCreationDate() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreationDate() {
        return this.createdAt;
    }
}
