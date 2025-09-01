package com.blog.myblog.domain.post.service;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashSet;
import java.util.Set;

@Component
@SessionScope
public class ViewedPostsHolder {
    private final Set<Long> ids = new HashSet<>();

    public boolean contains(Long id) {
        return ids.contains(id);
    }

    public void add(Long id) {
        ids.add(id);
    }
}