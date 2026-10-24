package org.cloudplugins.cloudJail.objects;

import java.util.List;

public record HistoryPage(Punishment active, List<Punishment> entries, int page, int totalPages, int total) {}
