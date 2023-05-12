package org.nrg.jobtemplates.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Constraint {

    public enum Operator {
        IN,
        NOT_IN,
    }

    private String key;
    private Set<String> values;
    private Operator operator;

    /**
     * Convert to list of Docker constraint strings. Example: ["key==value1", "key==value2"]
     * @return List of Docker constraint strings
     */
    public List<String> toList() {
        List<String> list = new ArrayList<>();

        values.forEach((value) -> {
            final String operatorString;

            switch (operator) {
                case IN:
                    operatorString = "==";
                    break;
                case NOT_IN:
                    operatorString = "!=";
                    break;
                default:
                    throw new RuntimeException("Unknown constraint operator: " + operator);
            }

            list.add(key + operatorString + value);
        });

        return list;
    }

}
