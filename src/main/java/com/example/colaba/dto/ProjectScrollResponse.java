package com.example.colaba.dto;

import java.util.List;

public class ProjectScrollResponse {

    private List<ProjectResponse> projects;
    private boolean hasNext;
    private long total;

    public ProjectScrollResponse() {}

    public ProjectScrollResponse(List<ProjectResponse> projects, boolean hasNext, long total) {
        this.projects = projects;
        this.hasNext = hasNext;
        this.total = total;
    }

    public List<ProjectResponse> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectResponse> projects) {
        this.projects = projects;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
