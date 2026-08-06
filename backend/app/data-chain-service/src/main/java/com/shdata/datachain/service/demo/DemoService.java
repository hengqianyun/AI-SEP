package com.shdata.datachain.service.demo;

import com.querydsl.core.types.Predicate;
import com.shdata.datachain.common.exception.ResourceNotFoundException;
import com.shdata.datachain.entity.DemoEntity;
import com.shdata.datachain.entity.QDemoEntity;
import com.shdata.datachain.model.DemoRequest;
import com.shdata.datachain.repository.DemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Demo 示例服务，提供基础 CRUD 与 QueryDSL 分页查询。
 * <p>注意：delete 为物理删除，属历史遗留，新建业务表应使用逻辑删除。</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DemoService {

    private final DemoRepository demoRepository;

    /**
     * 新增 Demo 实体。
     *
     * @param request 包含 name（必填）和 description（可选）的请求
     * @return 持久化后的 DemoEntity
     */
    @Transactional
    public DemoEntity create(DemoRequest request) {
        DemoEntity entity = DemoEntity.builder()
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .build();
        return demoRepository.save(entity);
    }

    /**
     * 按主键 ID 查询 Demo。
     *
     * @param id Demo ID
     * @return DemoEntity
     * @throws ResourceNotFoundException 若 ID 不存在
     */
    public DemoEntity get(Long id) {
        return demoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demo 不存在，id=" + id));
    }

    /**
     * 关键字分页查询，关键字匹配 name 与 description（忽略大小写），按 ID 降序。
     *
     * @param keyword 搜索关键字，为空则查全部
     * @param page    页码（0-based）
     * @param size    每页条数
     * @return 分页结果
     */
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

    /**
     * 更新 Demo 的 name 和 description。
     *
     * @param id      Demo ID
     * @param request 新的 name 和 description
     * @return 更新后的 DemoEntity
     * @throws ResourceNotFoundException 若 ID 不存在
     */
    @Transactional
    public DemoEntity update(Long id, DemoRequest request) {
        DemoEntity entity = get(id);
        entity.setName(request.getName().trim());
        entity.setDescription(trimToNull(request.getDescription()));
        return demoRepository.save(entity);
    }

    /**
     * 物理删除 Demo（历史遗留，新业务应使用逻辑删除）。
     *
     * @param id Demo ID
     * @throws ResourceNotFoundException 若 ID 不存在
     */
    @Transactional
    public void delete(Long id) {
        DemoEntity entity = get(id);
        demoRepository.delete(entity);
    }

    /**
     * 若字符串有内容则 trim 返回，否则返回 null。
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
