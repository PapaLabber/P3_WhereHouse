// Mock CSV processing utility for prototype
export interface ProcessedResult {
  csvData: string;
  filename: string;
  hasLocationData: boolean;
  locationSuggestions: string[];
  recordCount: number;
}

export function parseCsv(csvText: string): string[][] {
  const lines = csvText.split('\n').filter(line => line.trim());
  return lines.map(line => {
    // Simple CSV parsing (doesn't handle quoted commas)
    return line.split(',').map(cell => cell.trim());
  });
}

export function generateCsv(data: string[][]): string {
  return data.map(row => row.join(',')).join('\n');
}

// Mock processing function for prototype
export async function processData(
  csvFile: File, 
  prompts: [string, string]
): Promise<ProcessedResult> {
  // Simulate processing delay
  await new Promise(resolve => setTimeout(resolve, 2000));
  
  const csvText = await csvFile.text();
  const data = parseCsv(csvText);
  
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
    csvData: generateCsv(mockProcessedData),
    filename: `processed_${csvFile.name.replace('.csv', '')}_${Date.now()}.csv`,
    hasLocationData: hasLocationKeywords,
    locationSuggestions,
    recordCount: mockProcessedData.length - 1 // Exclude header
  };
}