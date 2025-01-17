package ch.compile.corixbackend.api.v1;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
}