import { test, expect } from '@playwright/test'; // Test framework and assertion library
import * as ExcelJS from 'exceljs'; // Creating and reading Excel files
import fs from 'fs'; // Node.js file system module for file operations
import path from 'path'; // Node.js path module for handling file paths

// This test assumes docker compose will start frontend on 3000 and backend on 8080

test('full system E2E test: upload -> backend -> allocation -> download', async ({ page }) => { 
  // Resolve the path to the fixtures directory relative to the current file's directory
  const fixturesDir = path.resolve(__dirname, '..', 'fixtures'); // __dirname is the directory of the current module
  fs.mkdirSync(fixturesDir, { recursive: true }); // Create the fixtures directory if it doesn't exist, recursively creating parent directories
  
  const filePath = path.join(fixturesDir, 'upload_fixture.xlsx'); // Construct the full path to the fixture Excel file

  // Build a minimal XLSX matching ExcelReader expectations
  const workbook = new ExcelJS.Workbook();
  const worksheet = workbook.addWorksheet('Sheet1'); // Add a worksheet named 'Sheet1' to the workbook
  
  // Add the header row with column names expected by the backend's ExcelReader
  worksheet.addRow([ 
    'Country',        
    'PalletAmount',   
    'Year',           
    'ProductionSite', 
    'Temperature',    
    'ID',             
    'Warehouse',      
    'L&D Capacity (Physical pallet spaces)' 
  ]);

  // Add sample data rows: one request and one capacity entry
  worksheet.addRow(['DENMARK', 10, 2025, 'Hillerød', 'Ambient', 1, 'PS PAC I', 20]); 
  worksheet.addRow(['DENMARK', 5, 2025, 'Hillerød', 'Ambient', 2, 'PS PAC I', 20]);
  await workbook.xlsx.writeFile(filePath);        // Write the workbook to the file path asynchronously
  expect(fs.existsSync(filePath)).toBeTruthy();   // Ensure the fixture file was created by Asserting that the file exists on disk

  // Navigate to the frontend application running on localhost:3000
  await page.goto('http://localhost:3000', { waitUntil: 'networkidle' }); // Wait until the page is fully loaded (no network activity)

  // Select the country and year filters in the UI
  const countrySelect = page.locator('select').first(); // Locate the first select element (country dropdown)
  await countrySelect.selectOption('DENMARK');          // Select 'DENMARK' from the country dropdown
  const yearSelect = page.locator('select').nth(1);     // Locate the second select element (year dropdown)
  await yearSelect.selectOption('2025');                // Select '2025' from the year dropdown

  // Upload the fixture file and wait for backend processing and download
  const responsePromise = page.waitForResponse(response => response.url().includes('/api/export')); // Promise to wait for the HTTP response from /api/export in the backend
  const downloadPromise = page.waitForEvent('download', { timeout: 120000 }); // Promise to wait for the download event triggered by the frontend
  await page.setInputFiles('#file-input', filePath); // Set the file input to the fixture file path
  await page.locator('#file-input').dispatchEvent('change'); // Manually dispatch a 'change' event on the file input to trigger the frontend's upload handler

  // Await the backend response and assert it was successful
  const response = await responsePromise;
  expect(response.status()).toBe(200); //200 = HTTP OK status
  
  const download = await downloadPromise; // Await the download event and save the file
  const downloadPath = path.join(fixturesDir, 'downloaded.xlsx'); // Path to save the downloaded file
  await download.saveAs(downloadPath); // Save the file to the specified path

  // Basic assertions: verify the downloaded file exists and has expected content
  expect(fs.existsSync(downloadPath)).toBeTruthy();   // Check the downloaded file exists
  const readWorkbook = new ExcelJS.Workbook();        // Read the downloaded Excel file
  await readWorkbook.xlsx.readFile(downloadPath);
  const readWorksheet = readWorkbook.getWorksheet(1); // Get the first worksheet
  // Extract all rows into an array:
  const rows: any[] = [];
  readWorksheet?.eachRow((row: any) => {
    rows.push(row.values);
  });

  // The backend's OutputResult creates a header row: ["Warehouse", "Storage Condition", "Amount Stored"]
  const headerRow = rows[0] || [];                  // Get the first row (header)
  expect(headerRow).toContain('Warehouse');         // Assert header contains 'Warehouse'
  expect(headerRow).toContain('Storage Condition'); // Assert header contains 'Storage Condition'
  expect(headerRow).toContain('Amount Stored');     // Assert header contains 'Amount Stored'

  // Assert that the data rows contain expected allocation results
  // Based on the fixture: requests for 10 and 5 pallets Ambient, capacity 20 at PS PAC I
  // Expect at least one row with PS PAC I and Ambient, and amounts adding up to 15 or less
  expect(rows.length).toBeGreaterThan(1); // At least header + one data row
  const dataRows = rows.slice(1);         // Skip header
  // Verify that at least one data row contains the expected warehouse and storage condition.
  // Normalize to lowercase for case-insensitive comparison because the backend writes enums with
  // uppercase values (e.g., "AMBIENT") while the fixture may be mixed-case.
  const foundExpectedRow = dataRows.some(row => {
    // Convert each cell in the row to a lowercase string
    const rowStrings = (row || []).map(cell => (cell || '').toString().toLowerCase());
    return rowStrings.includes('ambient') && rowStrings.includes('ps pac i');
  });
  expect(foundExpectedRow, `Expected a row containing PS PAC I + Ambient; rows: ${JSON.stringify(dataRows)}`).toBeTruthy(); // Check for expected warehouse and condition
  
  // Optionally, check total allocated amount (sum of amounts in data rows)
  // The excel row.values array is 1-based: index 1 -> Warehouse, 2 -> Storage Condition, 3 -> Amount Stored
  const totalAllocated = dataRows.reduce((sum, row) => {
    const amountCell = row[3] || 0; // Use index 3 (1-based) for the numeric amount
    const amount = Number(amountCell) || 0;
    return sum + amount;
  }, 0);
  expect(totalAllocated).toBeGreaterThan(0);  // no precise allocation check, but verifies if any happened
});
