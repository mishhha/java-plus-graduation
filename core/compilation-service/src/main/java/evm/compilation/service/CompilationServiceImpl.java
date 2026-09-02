package evm.compilation.service;

import evm.compilation.client.EventClient;
import evm.compilation.client.dto.EventShortDto;
import evm.compilation.dto.CompilationDto;
import evm.compilation.dto.NewCompilationDto;
import evm.compilation.dto.UpdateCompilationRequest;
import evm.compilation.mapper.CompilationMapper;
import evm.compilation.model.Compilation;
import evm.compilation.repository.CompilationRepository;
import evm.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation c = mapper.toEntity(dto);
        Compilation saved = compilationRepository.save(c);
        List<EventShortDto> events = getEventsByIds(new ArrayList<>(c.getEventIds()));
        return mapper.toDto(saved, events);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation c = findOrThrow(compId);
        compilationRepository.delete(c);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation c = findOrThrow(compId);

        if (dto.getTitle() != null) {
            c.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            c.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            c.setEventIds(new HashSet<>(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(c);
        List<EventShortDto> events = getEventsByIds(new ArrayList<>(c.getEventIds()));
        return mapper.toDto(saved, events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        int fromVal = (from != null) ? from : 0;
        int sizeVal = (size != null) ? size : 10;

        Pageable pageable = PageRequest.of(fromVal / sizeVal, sizeVal);
        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream()
            .map(c -> {
                List<EventShortDto> events = getEventsByIds(new ArrayList<>(c.getEventIds()));
                return mapper.toDto(c, events);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation c = findOrThrow(compId);
        List<EventShortDto> events = getEventsByIds(new ArrayList<>(c.getEventIds()));
        return mapper.toDto(c, events);
    }

    private List<EventShortDto> getEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return eventClient.getEventsByIds(eventIds);
    }

    private Compilation findOrThrow(Long compId) {
        return compilationRepository.findById(compId)
            .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
    }
}