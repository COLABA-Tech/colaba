package com.example.colaba.task.infrastructure.persistence.mapper;

import com.example.colaba.task.domain.entity.Task;
import com.example.colaba.task.infrastructure.persistence.entity.TaskJpa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapperJpa {
    Task toDomain(TaskJpa jpa);

    TaskJpa toJpa(Task domain);
}
