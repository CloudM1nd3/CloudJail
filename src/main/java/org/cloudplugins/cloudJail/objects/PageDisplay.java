package org.cloudplugins.cloudJail.objects;

import java.util.List;

public class PageDisplay {
    private final Punishment active;
    private final List<Punishment> entries;
    private final int totalPages;
    private final int total;

    public PageDisplay(List<Punishment> entries, int totalPages, int total) {
        this(null, entries, totalPages, total);
    }

    public PageDisplay(Punishment active, List<Punishment> entries, int totalPages, int total) {
        this.active = active;
        this.entries = entries;
        this.totalPages = totalPages;
        this.total = total;
    }

    public Punishment getActive(){
        return active;
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


