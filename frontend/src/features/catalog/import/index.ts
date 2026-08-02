/**
 * 公共入口：仅导出 ImportDialog，避免 barrel 同时 re-export composable
 * 与 ImportDialog→useProductImport 形成 Vite 循环/空模块（BUG-WSC-304-004）。
 * composable / guards 请从相对路径直接导入。
 */
export { default as ImportDialog } from './ImportDialog.vue'
