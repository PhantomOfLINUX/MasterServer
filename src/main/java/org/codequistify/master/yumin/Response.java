package org.codequistify.master.yumin;

import java.util.List;

public record Response(String author, List<Task> taskList) {
}
