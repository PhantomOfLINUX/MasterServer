package org.codequistify.master.global.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record Labels(List<Label> labels) {
  public Labels {
    Objects.requireNonNull(labels, "labels must not be null");
    labels = List.copyOf(labels);
  }

  public static Labels of(Label... labels) {
    return new Labels(List.of(labels));
  }

  public static Labels of(List<Label> labels) {
    return new Labels(labels);
  }

  public Map<String, List<String>> toMultiValueMap() {
    return Collections.unmodifiableMap(labels.stream()
        .collect(Collectors.toMap(
            Label::key, Label::values, (left, right) -> right, LinkedHashMap::new)));
  }

  public Map<String, String> toSingleValueMap() {
    return Collections.unmodifiableMap(labels.stream()
        .map(Label::firstValuePair)
        .collect(Collectors.toMap(
            Pair::first, Pair::second, (left, right) -> right, LinkedHashMap::new)));
  }
}
