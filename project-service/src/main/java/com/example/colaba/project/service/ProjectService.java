package com.example.colaba.project.service;

import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.shared.dto.project.CreateProjectRequest;
import com.example.colaba.shared.dto.project.ProjectResponse;
import com.example.colaba.shared.dto.project.ProjectScrollResponse;
import com.example.colaba.shared.dto.project.UpdateProjectRequest;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.exception.project.DuplicateProjectNameException;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    //    private final UserRepository userRepository;  // TODO
    private final ProjectMapper projectMapper;

    @Transactional
    public Mono<ProjectResponse> createProject(CreateProjectRequest request) {
        // TODO: check user and add owner
        return Mono.fromCallable(() -> {
            if (projectRepository.existsByName(request.name())) {
                throw new DuplicateProjectNameException(request.name());
            }

            Project project = Project.builder()
                    .name(request.name())
                    .description(request.description())
                    .build();

            Project saved = projectRepository.save(project);
            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectResponse> getProjectById(Long id) {
        return Mono.fromCallable(() -> projectRepository.findById(id)
                        .map(projectMapper::toProjectResponse)
                        .orElseThrow(() -> new ProjectNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Project> getProjectEntityById(Long id) {
        return Mono.fromCallable(() -> projectRepository.findById(id)
                        .orElseThrow(() -> new ProjectNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<ProjectResponse>> getAllProjects() {
        return Mono.fromCallable(projectRepository::findAll)
                .map(projectMapper::toProjectResponseList)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ProjectResponse> updateProject(Long id, UpdateProjectRequest request) {
        return Mono.fromCallable(() -> {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new ProjectNotFoundException(id));

            boolean hasChanges = false;
            if (request.name() != null && !request.name().isBlank() && !request.name().equals(project.getName())) {
                if (projectRepository.existsByNameAndIdNot(request.name(), id)) {
                    throw new DuplicateProjectNameException(request.name());
                }
                project.setName(request.name());
                hasChanges = true;
            }

            if (request.description() != null && !request.description().equals(project.getDescription())) {
                project.setDescription(request.description());
                hasChanges = true;
            }

            Project saved = hasChanges ? projectRepository.save(project) : project;
            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // TODO: user service
//    @Transactional
//    public Mono<ProjectResponse> changeProjectOwner(Long projectId, Long newOwnerId) {
//        return Mono.zip(
//                getProjectEntityById(projectId),
//                userServiceClient.getUserEntityById(newOwnerId)
//                        .switchIfEmpty(Mono.error(new UserNotFoundException(newOwnerId)))
//        ).flatMap(tuple -> {
//            Project project = tuple.getT1();
//            UserJpa newOwner = tuple.getT2();
//
//            return Mono.fromCallable(() -> {
//                project.setOwner(newOwner);
//                Project saved = projectRepository.save(project);
//                return projectMapper.toProjectResponse(saved);
//            }).subscribeOn(Schedulers.boundedElastic());
//        });
//    }

    @Transactional
    public Mono<Void> deleteProject(Long id) {
        return Mono.fromRunnable(() -> {
            if (!projectRepository.existsById(id)) {
                throw new ProjectNotFoundException(id);
            }
            projectRepository.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<List<ProjectResponse>> getProjectByOwnerId(Long ownerId) {
        // TODO: check owner
        return Mono.fromCallable(() -> projectRepository.findByOwnerId(ownerId))
                .map(projectMapper::toProjectResponseList)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectScrollResponse> scroll(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PageRequest.of(page, size);
            Page<Project> projectPage = projectRepository.findAll(pageable);

            List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
            boolean hasNext = projectPage.hasNext();
            long total = projectPage.getTotalElements();

            return new ProjectScrollResponse(projects, hasNext, total);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
