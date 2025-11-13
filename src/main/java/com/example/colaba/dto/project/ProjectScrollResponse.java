package com.example.colaba.dto.project;

import java.util.List;

public class ProjectScrollResponse {

    private List<ProjectResponse> projects;
    private String nextCursor;
    private boolean hasMore;
    private Long total;         // для page-scroll

    public ProjectScrollResponse() {
    }

    // Constructor for cursor-based scroll
    public ProjectScrollResponse(List<ProjectResponse> projects, String nextCursor, boolean hasMore) {
        this.projects = projects;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }

    // Constructor for page-based scroll
    public ProjectScrollResponse(List<ProjectResponse> projects, boolean hasMore, long total) {
        this.projects = projects;
        this.hasMore = hasMore;
        this.total = total;
    }

    public List<ProjectResponse> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectResponse> projects) {
        this.projects = projects;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
