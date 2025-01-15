package ch.compile.corixbackend.api.v1;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import ch.compile.corixbackend.api.v1.PolicyChecker.Violation;
import ch.compile.corixbackend.api.v1.Todo.State;
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
        todoRepository.save(new Todo("corix", "Hold a workshop", "Do your best!", State.NEW));
    }

    @GetMapping
    public List<Todo> getTodo() {
        return todoRepository.findAll();
    }

    @PostMapping
    public Todo createTodo(String title, String description) {
        return todoRepository.save(new Todo(UUID.randomUUID().toString(), title, description, State.NEW));
    }

    @PutMapping
    public Todo updateTodo(String id, String title, String description, State state) {
        Todo oldTodo = todoRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Todo newTodo = new Todo(id, title, description, state);

        Set<Violation> policyViolations = policyChecker.checkPolicyViolation(oldTodo, newTodo);

        log.info("{}", policyViolations);

        todoRepository.save(newTodo);
        
        return newTodo;
    }

}
