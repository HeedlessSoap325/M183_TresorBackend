package ch.bbw.pr.tresorbackend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SecurityRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    @Override
    public String toString() {
        return this.getValue();
    }
}
