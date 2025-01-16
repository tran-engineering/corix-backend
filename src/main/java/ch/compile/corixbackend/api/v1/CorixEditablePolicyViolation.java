package ch.compile.corixbackend.api.v1;

import java.util.Set;
import java.util.stream.Collectors;

import ch.compile.corixbackend.api.v1.PolicyChecker.Violation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 * CorixEditablePolicyViolation is thrown when a policy has been violated.
 */
@RequiredArgsConstructor
@Getter
public class CorixEditablePolicyViolation extends Exception {
    private final Set<Violation> violations;

    @Override
    public String getMessage() {
        return "CorixEditablePolicyViolation: "
                + violations.stream().map(Violation::toString).collect(Collectors.joining(", "));
    }
}
