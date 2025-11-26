// // Clean XLSX-compatible processing interface (no CSV)
export interface ProcessedResult {
  filename: string;               // Name of the file downloaded from backend
  recordCount: number;            // Number of rows processed (provided by backend or calculated)
  hasLocationData: boolean;       // Whether backend detected location fields
  locationSuggestions: string[];  // Location suggestions (backend-generated)
}

/**
 * This function no longer processes files on the frontend.
 * Instead, it prepares metadata after the backend returns the XLSX file.
 *
 * You will call this with metadata your backend sends back
 * (or remove completely if backend doesn't send metadata).
 */
export async function processDataFromBackend(metadata: any): Promise<ProcessedResult> {
  return {
    filename: metadata.filename ?? "result.xlsx",
    recordCount: metadata.recordCount ?? 0,
    hasLocationData: metadata.hasLocationData ?? false,
    locationSuggestions: metadata.locationSuggestions ?? [],
  };
}
