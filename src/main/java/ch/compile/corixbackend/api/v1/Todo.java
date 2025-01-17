package ch.compile.corixbackend.api.v1;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
public class Todo {
    @Id
    UUID id;

    @CorixEditable("state != 'DONE'")
    String title;
    
    @CorixEditable("state != 'DONE'")
    String description;
    
    @Schema(allowableValues = {"NEW", "IN PROGRESS", "DONE"})
    String state;
}