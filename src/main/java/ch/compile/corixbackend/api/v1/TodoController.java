package ch.compile.corixbackend.api.v1;

import java.util.List;
import java.util.UUID;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.compile.corixbackend.api.v1.EntityPolicyChecker.PolicyViolationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/todo")
public class TodoController {

    private static final String DEFAULT_UUID = "2307300d-c743-4636-ac98-ddee681eaee7";
    private final TodoRepository todoRepository;
    private final EntityPolicyChecker policyChecker;

    @Bean
    public GroupedOpenApi todoApi() {
        return GroupedOpenApi.builder()
                .group("todo-api")
                .pathsToMatch("/api/v1/todo/**")
                .build();
    }

    @PostConstruct
    void fixtures() {
        todoRepository.save(new Todo(UUID.fromString(DEFAULT_UUID), "Hold a workshop", "Do your best!", "", "NEW"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns all todos")
    @ApiResponse(responseCode = "200", description = "Returns all todos", content = @Content(mediaType = "application/json"))
    public List<Todo> getTodos() {
        return todoRepository.findAll();
    }

    @GetMapping(
        path = "{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
        )
    @Operation(summary = "Returns one todo")
    @ApiResponse(responseCode = "200", description = "Returns all todos", content = @Content(mediaType = "application/json"))
    public Todo getTodo(@PathVariable @Parameter(example = DEFAULT_UUID) UUID id) {
        return todoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a new todo")
    @ApiResponse(responseCode = "200", description = "Creates a new todo", content = @Content(mediaType = "application/json"))
    public Todo createTodo(
        @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject("""
            {
            "id": "f214f5a3-4e75-4126-8e3f-20d2c0c39dfc",
            "title": "My new TODO",
            "description": "Some non-descript description",
            "postMortemNotes": "",
            "state": "NEW"
            }
        """)))
        @org.springframework.web.bind.annotation.RequestBody
        Todo newTodo
        ) {
        return todoRepository.save(newTodo);
    }

    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates a single field of a todo")
    @ApiResponse(responseCode = "200", description = "Returns updated Todo", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "Policy violation", content = @Content(mediaType = "application/json"))
    public Todo patchTodo(
        @Parameter(example = DEFAULT_UUID, required = true)
        UUID id, 
        @Parameter(example = "title", required = true)
        String field, 
        @Parameter(example = "My new Title", required = true)
        String value
        ) {
        Todo oldTodo = todoRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Todo newTodo = new Todo(oldTodo.getId(), oldTodo.getTitle(), oldTodo.getDescription(), "", oldTodo.getState());
        try {
            Todo.class.getDeclaredField(field).set(newTodo, value);
            policyChecker.checkPolicyViolation(oldTodo, newTodo);
            todoRepository.save(newTodo);
            return newTodo;
        } catch (NoSuchFieldException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field not found");
        } catch (IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not access field");
        } catch (PolicyViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy violation: " + e.getMessage());
        }
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates a todo")
    @ApiResponse(responseCode = "200", description = "The updated Todo", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "Policy violation", content = @Content(mediaType = "application/json"))
    public Todo updateTodo(
        @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject("""
            {
            "id": "2307300d-c743-4636-ac98-ddee681eaee7",
            "title": "Updated Title",
            "description": "Updated Descrption",
            "postMortemNotes": "",
            "state": "DONE"
            }
        """)))
        @org.springframework.web.bind.annotation.RequestBody
        Todo newTodo
        ) {
        Todo oldTodo = todoRepository
                .findById(newTodo.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        try {
            policyChecker.checkPolicyViolation(oldTodo, newTodo);
        } catch (PolicyViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Policy violation: " + e.getMessage());
        }
        todoRepository.save(newTodo);

        return newTodo;
    }
}
