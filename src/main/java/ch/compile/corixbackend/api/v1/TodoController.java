package ch.compile.corixbackend.api.v1;

import java.util.List;
import java.util.UUID;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/todo")
public class TodoController {

    private final TodoRepository todoRepository;
    private final PolicyChecker policyChecker;

    @Bean
    public GroupedOpenApi todoApi() {
        return GroupedOpenApi.builder()
                .group("todo-api")
                .pathsToMatch("/api/v1/todo/**")
                .build();
    }

    @PostConstruct
    void fixtures() {
        todoRepository.save(new Todo("corix", "Hold a workshop", "Do your best!", "NEW"));
    }

    @GetMapping
    public List<Todo> getTodo() {
        return todoRepository.findAll();
    }

    @PostMapping
    public Todo createTodo(String title, String description) {
        return todoRepository.save(new Todo(UUID.randomUUID().toString(), title, description, "NEW"));
    }

    @PatchMapping
    public Todo patchTodo(String id, String field, String value) {
        Todo oldTodo = todoRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Todo newTodo = new Todo(oldTodo.getId(), oldTodo.getTitle(), oldTodo.getDescription(), oldTodo.getState());
        try {
            Todo.class.getField(field).set(newTodo, value);
            policyChecker.checkPolicyViolation(oldTodo, newTodo);
            todoRepository.save(newTodo);
            return newTodo;
        } catch (NoSuchFieldException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field not found");
        } catch (IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not access field");
        } catch (CorixEditablePolicyViolation e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy violation: " + e.getMessage());
        }
    }

    @PutMapping
    public Todo updateTodo(String id, String title, String description, String state) {
        Todo oldTodo = todoRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Todo newTodo = new Todo(id, title, description, state);
        try {
            policyChecker.checkPolicyViolation(oldTodo, newTodo);
        } catch (CorixEditablePolicyViolation e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy violation: " + e.getMessage());
        }
        todoRepository.save(newTodo);

        return newTodo;
    }
}
