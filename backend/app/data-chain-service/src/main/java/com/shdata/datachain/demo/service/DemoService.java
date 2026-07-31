package com.shdata.datachain.demo.service;

import com.querydsl.core.types.Predicate;
import com.shdata.datachain.common.exception.ResourceNotFoundException;
import com.shdata.datachain.demo.entity.DemoEntity;
import com.shdata.datachain.demo.entity.QDemoEntity;
import com.shdata.datachain.demo.model.DemoRequest;
import com.shdata.datachain.demo.repository.DemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DemoService {

    private final DemoRepository demoRepository;

    @Transactional
    public DemoEntity create(DemoRequest request) {
        DemoEntity entity = DemoEntity.builder()
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .build();
        return demoRepository.save(entity);
    }

    public DemoEntity get(Long id) {
        return demoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demo 不存在，id=" + id));
    }

    public Page<DemoEntity> page(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (!StringUtils.hasText(keyword)) {
            return demoRepository.findAll(pageable);
        }

        QDemoEntity demo = QDemoEntity.demoEntity;
        Predicate predicate = demo.name.containsIgnoreCase(keyword.trim())
                .or(demo.description.containsIgnoreCase(keyword.trim()));
        return demoRepository.findAll(predicate, pageable);
    }

    @Transactional
    public DemoEntity update(Long id, DemoRequest request) {
        DemoEntity entity = get(id);
        entity.setName(request.getName().trim());
        entity.setDescription(trimToNull(request.getDescription()));
        return demoRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        DemoEntity entity = get(id);
        demoRepository.delete(entity);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

