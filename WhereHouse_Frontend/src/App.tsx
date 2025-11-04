import React, { useState } from 'react';
import { FileUpload } from './components/FileUpload';
import { PromptInputs } from './components/PromptInputs';
import { ResultsDownload } from './components/ResultsDownload';
import { processData, type ProcessedResult } from './utils/csvProcessor';
import { Warehouse } from 'lucide-react';

export default function App() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [prompts, setPrompts] = useState<[string, string, string]>(['', '', '']);
  const [isProcessing, setIsProcessing] = useState(false);
  const [result, setResult] = useState<ProcessedResult | null>(null);

  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
    setResult(null); // Clear previous results
  };

  const handleProcess = async () => {
    if (!selectedFile || prompts.every(p => !p.trim())) return;
    
    setIsProcessing(true);
    try {
      const processedResult = await processData(selectedFile, prompts);
      setResult(processedResult);
    } catch (error) {
      console.error('Processing failed:', error);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDownload = () => {
    if (!result) return;
    
    const blob = new Blob([result.csvData], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = result.filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-[#001965] text-white shadow-lg">
        <div className="max-w-6xl mx-auto px-6 py-6">
          <div className="flex items-center space-x-3">
            <Warehouse className="h-8 w-8" />
            <h1 className="text-3xl font-bold">WhereHouse</h1>
          </div>
          <p className="mt-2 text-blue-100">Intelligent CSV data processing and location mapping</p>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column */}
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold text-[#001965] mb-4">Step 1: Upload CSV File</h2>
              <FileUpload 
                onFileSelect={handleFileSelect}
                selectedFile={selectedFile}
              />
            </div>
            
            <div>
              <h2 className="text-xl font-semibold text-[#001965] mb-4">Step 2: Specify Data Processing</h2>
              <PromptInputs
                prompts={prompts}
                setPrompts={setPrompts}
                onProcess={handleProcess}
                isProcessing={isProcessing}
                disabled={!selectedFile}
              />
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold text-[#001965] mb-4">Step 3: Download Results</h2>
              <ResultsDownload
                result={result}
                onDownload={handleDownload}
              />
            </div>

            {/* Info Card */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 className="font-semibold text-[#001965] mb-2">How it works</h3>
              <ul className="text-sm text-gray-700 space-y-1">
                <li>• Upload your CSV file with any data structure</li>
                <li>• Enter up to 3 prompts to specify data filtering and processing</li>
                <li>• WhereHouse will intelligently process your data</li>
                <li>• Location-based prompts will generate Google Maps suggestions</li>
                <li>• Download your processed CSV with the refined dataset</li>
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}