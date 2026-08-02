/**
 * 批量导入产品（REQ-CAT-008 / TASK-WSC-107）。
 *
 * <p>物理目录为 {@code catalog/import}（对齐 writeSet）；Java 包名为 {@code
 * com.shdata.datachain.catalog.productimport}（{@code import} 为关键字不可用）。
 *
 * <p>通过 DI 只读调用 {@link com.shdata.datachain.catalog.editor.ProductEditorService} 完成写入与存证。
 */
package com.shdata.datachain.catalog.productimport;
