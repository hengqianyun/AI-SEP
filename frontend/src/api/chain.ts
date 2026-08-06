/**
 * Chain API — 对齐 contracts/openapi + ChainAttestationPort (wsc-contracts@2.2.0)
 */
import { apiRequest } from './client'

export type ChainVersion = {
  versionId: string
  versionNo: number
  timestamp: string
  metadataHash: string
  ownerDID: string
  certificate: { owner: string; timestamp?: string }
}

export type CatalogSnapshot = {
  versionId: string
  productCode: string
  productName: string
  productType: string
  categoryPath?: string
  basicInfo?: Record<string, unknown>
  supplierInfo?: Record<string, unknown>
  propertyRights?: Record<string, unknown>
  typeSpecific?: Record<string, unknown>
  tags?: string[]
  summary?: string
  scenario?: string
  capturedAt: string
  attestation?: {
    metadataHash?: string
    ownerDID?: string
    timestamp?: string
    certificate?: { owner?: string }
  }
}

export function listChainVersions(productId: string) {
  return apiRequest<{ items: ChainVersion[] }>(`/chain/products/${productId}/versions`)
}

export function getChainSnapshot(versionId: string) {
  return apiRequest<CatalogSnapshot>(`/chain/versions/${versionId}/snapshot`)
}
