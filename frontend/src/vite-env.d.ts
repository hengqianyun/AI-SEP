/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_BASE?: string
  readonly VITE_DEV_PORT?: string
  readonly VITE_DEV_PROXY_TARGET?: string
  readonly VITE_PREVIEW_PORT?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}
