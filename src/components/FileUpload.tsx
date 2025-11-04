import React, { useCallback } from 'react';
import { Upload, FileText } from 'lucide-react';
import { Card } from './ui/card';

interface FileUploadProps {
  onFileSelect: (file: File) => void;
  selectedFile: File | null;
}

export function FileUpload({ onFileSelect, selectedFile }: FileUploadProps) {
  const handleDrop = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    const files = Array.from(e.dataTransfer.files);
    const csvFile = files.find(file => file.name.endsWith('.csv'));
    if (csvFile) {
      onFileSelect(csvFile);
    }
  }, [onFileSelect]);

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
  }, []);

  const handleFileInput = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file && file.name.endsWith('.csv')) {
      onFileSelect(file);
    }
  }, [onFileSelect]);

  return (
    <Card className="p-8">
      <div
        className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-[#001965] transition-colors cursor-pointer"
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onClick={() => document.getElementById('file-input')?.click()}
      >
        <input
          id="file-input"
          type="file"
          accept=".csv"
          onChange={handleFileInput}
          className="hidden"
        />
        
        {selectedFile ? (
          <div className="flex items-center justify-center space-x-2">
            <FileText className="h-8 w-8 text-[#001965]" />
            <div>
              <p className="text-sm text-gray-600">Selected file:</p>
              <p className="font-medium text-[#001965]">{selectedFile.name}</p>
              <p className="text-xs text-gray-500">{(selectedFile.size / 1024).toFixed(1)} KB</p>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <Upload className="h-12 w-12 text-gray-400 mx-auto" />
            <div>
              <p className="text-lg">Drop your CSV file here</p>
              <p className="text-sm text-gray-500">or click to browse</p>
            </div>
          </div>
        )}
      </div>
    </Card>
  );
}