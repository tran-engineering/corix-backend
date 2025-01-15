package ch.compile.corixbackend.api.v1;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Todo {
    @Id
    @CorixEditable("state != 'DONE'")
    String id;
    @CorixEditable("state != 'DONE'")
    String title;
    @CorixEditable("state != 'DONE'")
    String description;
    State state;

    @AllArgsConstructor
    @Getter
    public enum State {
        NEW("NEW"),
        ONGOING("ONGOING"),
        DONE("DONE");

        private final String value;        
    }
}