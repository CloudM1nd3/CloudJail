package org.cloudplugins.cloudJail.objects;

import java.util.ArrayList;
import java.util.List;

public class PageDisplay {
    private final List<Punishment> entries;
    private final int totalPages;
    private final int total;

    public PageDisplay(List<Punishment> entries, int totalPages, int total) {
        this(null, entries, totalPages, total);
    }

    public PageDisplay(Punishment active, List<Punishment> entries, int totalPages, int total) {
        this.entries = entries;
        this.totalPages = totalPages;
        this.total = total;
    }

    public static PageDisplay of(List<Punishment> all, int page) {
        int total = all.size();
        int totalPages = Math.max(1, (total + 5 - 1) / 5);
        page = Math.min(Math.max(page, 1), totalPages);

        int from = (page - 1) * 5;
        int to = Math.min(from + 5, total);

        return new PageDisplay(new ArrayList<>(all.subList(from, to)), totalPages, total);
    }

    public List<Punishment> getEntries(){
        return entries;
    }

    public int getTotalPages(){
        return totalPages;
    }

    public int getTotal(){
        return total;
    }
}


