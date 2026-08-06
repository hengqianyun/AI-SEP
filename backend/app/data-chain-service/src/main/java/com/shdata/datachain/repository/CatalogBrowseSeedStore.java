package com.shdata.datachain.repository;

import com.shdata.datachain.entity.IndustryCategoryEntity;
import com.shdata.datachain.common.mapper.CatalogEntityMapper;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.entity.DataProductEntity;
import com.shdata.datachain.repository.IndustryCategoryRepository;
import com.shdata.datachain.repository.DataProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 目录数据存储（JPA 版），替代原 CopyOnWriteArrayList 内存实现。
 * <p>读写均通过 Repository 操作数据库；正式环境种子由 Flyway V5 写入。
 * H2 测境常关 Flyway，启动时若分类表为空则补齐与 V5 对齐的 L1/L2/L3 分类树，
 * 避免编辑器 create 因 {@code findCategory} 失败返回 {@code ERR_CATEGORY_LEAF_REQUIRED}。</p>
 */
@Component
@Order(100)
public class CatalogBrowseSeedStore implements ApplicationRunner {

    private final IndustryCategoryRepository categoryRepo;
    private final DataProductRepository productRepo;
    private final AtomicInteger productSeq = new AtomicInteger(200);
    private final AtomicInteger categorySeq = new AtomicInteger(200);

    public CatalogBrowseSeedStore(IndustryCategoryRepository categoryRepo,
                                  DataProductRepository productRepo) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureSeedCategoriesIfEmpty();
    }

    /**
     * 空库补齐行业分类树（与 V5 / 旧内存 seed 一致的 11 条）。
     * Flyway 已写入时 no-op。
     */
    @Transactional
    public synchronized void ensureSeedCategoriesIfEmpty() {
        if (!categoryRepo.findAll().isEmpty()) {
            return;
        }
        seedCategory("cat-l1-health", "医疗卫生", "L1", null);
        seedCategory("cat-l1-finance", "金融服务", "L1", null);
        seedCategory("cat-l2-emr", "电子病历", "L2", "cat-l1-health");
        seedCategory("cat-l2-imaging", "医学影像", "L2", "cat-l1-health");
        seedCategory("cat-l2-credit", "征信评估", "L2", "cat-l1-finance");
        seedCategory("cat-l2-txn", "交易流水", "L2", "cat-l1-finance");
        seedCategory("cat-l3-emr-desense", "脱敏病历", "L3", "cat-l2-emr");
        seedCategory("cat-l3-emr-struct", "结构化病历", "L3", "cat-l2-emr");
        seedCategory("cat-l3-img-ct", "CT 影像", "L3", "cat-l2-imaging");
        seedCategory("cat-l3-credit-score", "征信评分", "L3", "cat-l2-credit");
        seedCategory("cat-l3-txn-retail", "零售支付", "L3", "cat-l2-txn");
    }

    private void seedCategory(String code, String name, String level, String parentCode) {
        categoryRepo.save(CatalogEntityMapper.toEntity(new CatalogCategory(code, name, level, parentCode)));
    }

    // ---- 分类只读 ----

    /** 列出所有未删除的分类（L1/L2/L3 树）。 */
    @Transactional(readOnly = true)
    public List<CatalogCategory> categories() {
        return categoryRepo.findAll().stream()
                .map(CatalogEntityMapper::toCategory)
                .toList();
    }

    /** 按 code 查分类。返回 empty 若 code 为 null。 */
    @Transactional(readOnly = true)
    public Optional<CatalogCategory> findCategory(String code) {
        if (code == null) return Optional.empty();
        return categoryRepo.findAll().stream()
                .filter(e -> code.equals(e.getCode()))
                .findFirst()
                .map(CatalogEntityMapper::toCategory);
    }

    // ---- 产品只读 ----

    /** 列出所有未删除的产品。 */
    @Transactional(readOnly = true)
    public List<CatalogProduct> products() {
        return productRepo.findAll().stream()
                .map(CatalogEntityMapper::toProduct)
                .map(this::restoreOtherTypeSpecific)
                .toList();
    }

    /** 按 DB 主键 ID 查产品（适配旧 String ID 查询）。 */
    @Transactional(readOnly = true)
    public Optional<CatalogProduct> findProduct(String id) {
        try {
            long lid = Long.parseLong(id);
            return productRepo.findById(lid).map(CatalogEntityMapper::toProduct).map(this::restoreOtherTypeSpecific);
        } catch (NumberFormatException e) {
            // 旧自定义 ID（如 "prod-emr-001"），转为按 product_code 查
            return productRepo.findAll().stream()
                    .filter(p -> id.equals(p.getProductCode()))
                    .findFirst()
                    .map(CatalogEntityMapper::toProduct)
                    .map(this::restoreOtherTypeSpecific);
        }
    }

    /** 按产品编码查产品。 */
    @Transactional(readOnly = true)
    public Optional<CatalogProduct> findProductByCode(String productCode) {
        if (productCode == null) return Optional.empty();
        return productRepo.findAll().stream()
                .filter(p -> productCode.equals(p.getProductCode()))
                .findFirst()
                .map(CatalogEntityMapper::toProduct)
                .map(this::restoreOtherTypeSpecific);
    }

    // ---- ID 生成 ----

    public synchronized String nextProductId() {
        return String.valueOf(productSeq.incrementAndGet());
    }

    public synchronized String nextCategoryId() {
        return "cat-gen-" + categorySeq.incrementAndGet();
    }

    // ---- 产品写 ----

    /** 新增或更新产品，返回持久化后的 CatalogProduct（含 DB 真实 id）。 */
    @Transactional
    public synchronized CatalogProduct upsertProduct(CatalogProduct product) {
        return upsertProduct(product, null);
    }

    /**
     * 新增或更新产品。
     *
     * @param actorUserId 写入 create_by（新建）与 update_by；null 则保持/默认空串
     */
    @Transactional
    public synchronized CatalogProduct upsertProduct(CatalogProduct product, String actorUserId) {
        DataProductEntity entity = CatalogEntityMapper.toEntity(product);
        // GB/T 门类落在 CatalogProduct.industryCategory；mapper 仅映射 businessCategory，写入时兜底
        if ((entity.getBusinessCategory() == null || entity.getBusinessCategory().isBlank())
                && product.industryCategory() != null
                && !product.industryCategory().isBlank()) {
            entity.setBusinessCategory(product.industryCategory());
        }
        Optional<DataProductEntity> existing = productRepo.findAll().stream()
                .filter(e -> product.productCode().equals(e.getProductCode()))
                .findFirst();
        if (existing.isPresent()) {
            DataProductEntity old = existing.get();
            entity.setId(old.getId());
            entity.setCreateTime(old.getCreateTime());
            entity.setCreateBy(old.getCreateBy());
        } else if (actorUserId != null && !actorUserId.isBlank()) {
            entity.setCreateBy(actorUserId);
        }
        if (actorUserId != null && !actorUserId.isBlank()) {
            entity.setUpdateBy(actorUserId);
        }
        DataProductEntity saved = productRepo.save(entity);
        CatalogProduct mapped = CatalogEntityMapper.toProduct(saved);
        // mapper 暂不联查链版本；保留写入侧 chainCount / latestVersionNo / typeSpecific
        // （OTHER 载荷暂存 type_api_json，mapper 回读会误标为 api，故优先保留写入侧）
        Map<String, Object> typeSpecific =
                product.typeSpecific() != null ? product.typeSpecific() : mapped.typeSpecific();
        return restoreOtherTypeSpecific(
                new CatalogProduct(
                        mapped.id(),
                        mapped.productCode(),
                        mapped.productName(),
                        mapped.productType(),
                        mapped.l2CategoryId(),
                        mapped.l1CategoryId(),
                        mapped.categoryPath(),
                        product.chainCount(),
                        product.latestVersionNo(),
                        mapped.dataSource(),
                        mapped.deliveryMethod(),
                        mapped.involvesPublicData(),
                        mapped.involvesPersonalInfo(),
                        mapped.summary(),
                        mapped.scenario(),
                        mapped.supplierName(),
                        mapped.supplierCreditCode(),
                        mapped.tags(),
                        typeSpecific,
                        mapped.businessCategory(),
                        mapped.businessSubCategory(),
                        mapped.updateFrequency(),
                        mapped.billingMethod(),
                        mapped.price(),
                        mapped.propertyRightsType(),
                        mapped.l3CategoryId(),
                        mapped.industryCategory() != null && !mapped.industryCategory().isBlank()
                                ? mapped.industryCategory()
                                : product.industryCategory(),
                        mapped.updatedAt() != null ? mapped.updatedAt() : product.updatedAt()));
    }

    /**
     * CatalogEntityMapper 将 OTHER 的 typeSpecific 写入 type_api_json，回读时键名为 api。
     * 读路径按 productType=OTHER 纠正为 other，避免编辑器回归丢 contentDescription。
     */
    private CatalogProduct restoreOtherTypeSpecific(CatalogProduct p) {
        if (p == null || !"OTHER".equals(p.productType())) {
            return p;
        }
        Map<String, Object> ts = p.typeSpecific();
        if (ts == null || ts.isEmpty() || ts.containsKey("other") || !ts.containsKey("api")) {
            return p;
        }
        Map<String, Object> fixed = new LinkedHashMap<>(ts);
        Object otherPayload = fixed.remove("api");
        fixed.put("other", otherPayload);
        return new CatalogProduct(
                p.id(),
                p.productCode(),
                p.productName(),
                p.productType(),
                p.l2CategoryId(),
                p.l1CategoryId(),
                p.categoryPath(),
                p.chainCount(),
                p.latestVersionNo(),
                p.dataSource(),
                p.deliveryMethod(),
                p.involvesPublicData(),
                p.involvesPersonalInfo(),
                p.summary(),
                p.scenario(),
                p.supplierName(),
                p.supplierCreditCode(),
                p.tags(),
                fixed,
                p.businessCategory(),
                p.businessSubCategory(),
                p.updateFrequency(),
                p.billingMethod(),
                p.price(),
                p.propertyRightsType(),
                p.l3CategoryId(),
                p.industryCategory(),
                p.updatedAt());
    }

    /** 按 create_by 过滤产品（我的数据产品）。 */
    @Transactional(readOnly = true)
    public List<CatalogProduct> productsOwnedBy(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        return productRepo.findAll().stream()
                .filter(e -> userId.equals(e.getCreateBy()))
                .map(CatalogEntityMapper::toProduct)
                .map(this::restoreOtherTypeSpecific)
                .toList();
    }

    /** 读取产品归属 create_by。 */
    @Transactional(readOnly = true)
    public Optional<String> findProductOwner(String productId) {
        try {
            long lid = Long.parseLong(productId);
            return productRepo.findById(lid).map(DataProductEntity::getCreateBy);
        } catch (NumberFormatException e) {
            return productRepo.findAll().stream()
                    .filter(p -> productId.equals(p.getProductCode()))
                    .findFirst()
                    .map(DataProductEntity::getCreateBy);
        }
    }

    /**
     * 是否为本人产品：create_by 非空且等于 actorUserId。
     * 空/缺失归属不可冒领（返回 false → 调用方 403）。
     */
    @Transactional(readOnly = true)
    public boolean isOwnedBy(String productId, String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            return false;
        }
        Optional<String> owner = findProductOwner(productId);
        if (owner.isEmpty()) {
            return false;
        }
        String createBy = owner.get();
        return createBy != null && !createBy.isBlank() && actorUserId.equals(createBy);
    }

    /**
     * 测试/维护用：强制写入 create_by（含空串模拟无归属）。
     */
    @Transactional
    public synchronized void forceSetCreateBy(String productId, String createBy) {
        DataProductEntity entity = resolveEntity(productId);
        if (entity == null) {
            return;
        }
        entity.setCreateBy(createBy == null ? "" : createBy);
        productRepo.save(entity);
    }

    private DataProductEntity resolveEntity(String productId) {
        try {
            long lid = Long.parseLong(productId);
            return productRepo.findById(lid).orElse(null);
        } catch (NumberFormatException e) {
            return productRepo.findAll().stream()
                    .filter(p -> productId.equals(p.getProductCode()))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * L2 产品分布：按 l2_category_code 计数，降序。
     */
    @Transactional(readOnly = true)
    public Map<String, Object> l2Distribution() {
        List<DataProductEntity> all = productRepo.findAll();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (DataProductEntity p : all) {
            String code = p.getL2CategoryCode();
            if (code == null || code.isBlank()) {
                continue;
            }
            counts.merge(code, 1L, Long::sum);
        }
        List<Map<String, Object>> items = counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(en -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", en.getKey());
                    row.put(
                            "name",
                            findCategory(en.getKey()).map(CatalogCategory::name).orElse(en.getKey()));
                    row.put("count", en.getValue().intValue());
                    return row;
                })
                .toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalProducts", all.size());
        data.put("items", items);
        return data;
    }

    // ---- 分类写 ----

    /** 新增分类。 */
    @Transactional
    public synchronized void addCategory(CatalogCategory category) {
        IndustryCategoryEntity entity = CatalogEntityMapper.toEntity(category);
        categoryRepo.save(entity);
    }

    /** 替换分类（按 code 匹配更新）。 */
    @Transactional
    public synchronized void replaceCategory(CatalogCategory category) {
        Optional<IndustryCategoryEntity> existing = categoryRepo.findAll().stream()
                .filter(e -> category.id().equals(e.getCode()))
                .findFirst();
        if (existing.isPresent()) {
            IndustryCategoryEntity old = existing.get();
            old.setName(category.name());
            old.setLevel(category.level());
            old.setParentCode(category.parentId());
            categoryRepo.save(old);
        } else {
            addCategory(category);
        }
    }

    /** 逻辑删除分类。 */
    @Transactional
    public synchronized boolean removeCategory(String categoryCode) {
        return categoryRepo.findAll().stream()
                .filter(e -> categoryCode.equals(e.getCode()))
                .findFirst()
                .map(e -> {
                    e.setDelFlag(true);
                    categoryRepo.save(e);
                    return true;
                })
                .orElse(false);
    }

    // ---- 挂载计数 ----

    /** 挂载计数：L3 按 l3CategoryId；L2 含直挂 + 子 L3；L1 按 l1CategoryId。 */
    @Transactional(readOnly = true)
    public int countMountedProducts(CatalogCategory category) {
        List<DataProductEntity> all = productRepo.findAll();
        if ("L1".equals(category.level())) {
            return (int) all.stream()
                    .filter(p -> category.id().equals(p.getL1CategoryCode()))
                    .count();
        }
        if ("L3".equals(category.level())) {
            return (int) all.stream()
                    .filter(p -> category.id().equals(p.getIndustryCategoryCode()))
                    .count();
        }
        // L2：直挂 + 子 L3
        return (int) all.stream()
                .filter(p -> category.id().equals(p.getL2CategoryCode())
                        || (p.getIndustryCategoryCode() != null
                            && findCategory(p.getIndustryCategoryCode())
                                .map(c -> category.id().equals(c.parentId()))
                                .orElse(false)))
                .count();
    }

    // ---- 路径解析（与旧实现一致） ----

    /** 二级路径 "L1 / L2"。 */
    @Transactional(readOnly = true)
    public String resolveCategoryPath(String l2CategoryId) {
        Optional<CatalogCategory> l2 = findCategory(l2CategoryId);
        if (l2.isEmpty()) return "";
        String l1Name = l2.get().parentId() == null ? ""
                : findCategory(l2.get().parentId()).map(CatalogCategory::name).orElse("");
        return l1Name.isEmpty() ? l2.get().name() : l1Name + " / " + l2.get().name();
    }

    /** 三级完整路径 "空间 / 行业 / 子类"。 */
    @Transactional(readOnly = true)
    public String resolveCategoryPathFromL3(String l3CategoryId) {
        List<String> labels = resolvePathLabels(l3CategoryId);
        if (labels.isEmpty()) return "";
        return String.join(" / ", labels);
    }

    /** 从分类节点向上追溯，返回 [L1名, L2名, L3名]。 */
    @Transactional(readOnly = true)
    public List<String> resolvePathLabels(String categoryCode) {
        Optional<CatalogCategory> node = findCategory(categoryCode);
        if (node.isEmpty()) return List.of();
        List<String> labels = new ArrayList<>();
        CatalogCategory cur = node.get();
        labels.add(0, cur.name());
        while (cur.parentId() != null) {
            Optional<CatalogCategory> parent = findCategory(cur.parentId());
            if (parent.isEmpty()) break;
            cur = parent.get();
            labels.add(0, cur.name());
        }
        return labels;
    }

    @Transactional(readOnly = true)
    public String resolveL1Id(String l2CategoryId) {
        return findCategory(l2CategoryId).map(CatalogCategory::parentId).orElse(null);
    }

    @Transactional(readOnly = true)
    public String resolveL2IdFromL3(String l3CategoryId) {
        return findCategory(l3CategoryId)
                .filter(c -> "L3".equals(c.level()))
                .map(CatalogCategory::parentId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String resolveL1IdFromL3(String l3CategoryId) {
        String l2 = resolveL2IdFromL3(l3CategoryId);
        return l2 == null ? null : resolveL1Id(l2);
    }
}
