// Mock xlsx processing utility for prototype
export interface ProcessedResult {
  xlsxData: string;
  filename: string;
  hasLocationData: boolean;
  locationSuggestions: string[];
  recordCount: number;
}

export function parseXlsx(xlsxText: string): string[][] {
  const lines = xlsxText.split('\n').filter(line => line.trim());
  return lines.map(line => {
    // Simple xlsx parsing (doesn't handle quoted commas)
    return line.split(',').map(cell => cell.trim());
  });
}

export function generateXlsx(data: string[][]): string {
  return data.map(row => row.join(',')).join('\n');
}

// Mock processing function for prototype
export async function processData(
  xlsxFile: File,
  prompts: [string, string, string]
): Promise<ProcessedResult> {
  // Simulate processing delay
  await new Promise(resolve => setTimeout(resolve, 2000));

  const xlsxText = await xlsxFile.text();
  const data = parseXlsx(xlsxText);

  // Mock processing based on prompts
  const hasLocationKeywords = prompts.some(prompt =>
    prompt.toLowerCase().includes('location') ||
    prompt.toLowerCase().includes('address') ||
    prompt.toLowerCase().includes('map') ||
    prompt.toLowerCase().includes('coordinates')
  );

  // Create mock processed data
  const headers = data[0] || ['id', 'name', 'category', 'location', 'status'];
  const mockProcessedData = [
    headers,
    ['1', 'Sample Record 1', 'Category A', '123 Main St, New York, NY', 'Active'],
    ['2', 'Sample Record 2', 'Category B', '456 Oak Ave, Los Angeles, CA', 'Active'],
    ['3', 'Sample Record 3', 'Category A', '789 Pine Rd, Chicago, IL', 'Pending']
  ];

  const locationSuggestions = hasLocationKeywords ? [
    '123 Main St, New York, NY 10001',
    '456 Oak Ave, Los Angeles, CA 90210',
    '789 Pine Rd, Chicago, IL 60601',
    '321 Elm St, Houston, TX 77001',
    '654 Maple Dr, Phoenix, AZ 85001'
  ] : [];

  return {
    xlsxData: generateXlsx(mockProcessedData),
    filename: `processed_${xlsxFile.name.replace('.xlsx', '')}_${Date.now()}.xlsx`,
    hasLocationData: hasLocationKeywords,
    locationSuggestions,
    recordCount: mockProcessedData.length - 1 // Exclude header
  };
}
