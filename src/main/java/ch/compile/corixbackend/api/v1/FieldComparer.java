package ch.compile.corixbackend.api.v1;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FieldComparer {
    public <T> Set<Field> changedFields(T o1, T o2) {

        return Arrays.stream(o1.getClass().getDeclaredFields())
                .filter(field -> {
                    try {
                        return !Objects.equals(field.get(o1), field.get(o2));
                    } catch (Throwable t) {
                        return false;
                    }
                })
                .collect(Collectors.toSet());

    }
}
