import React from 'react';
import { Card } from './ui/card';
import { Button } from './ui/button';
import { Download, MapPin, CheckCircle } from 'lucide-react';

interface ProcessedResult {
  xlsxData: string;
  filename: string;
  hasLocationData: boolean;
  locationSuggestions: string[];
  recordCount: number;
}

interface ResultsDownloadProps {
  result: ProcessedResult | null;
  onDownload: () => void;
}

export function ResultsDownload({ result, onDownload }: ResultsDownloadProps) {
  if (!result) {
    return (
      <Card className="p-6">
        <div className="text-center text-gray-500">
          <div className="mb-4">
            <CheckCircle className="h-12 w-12 text-gray-300 mx-auto" />
          </div>
          <p>Process your data to see results here</p>
        </div>
      </Card>
    );
  }

  return (
    <Card className="p-6">
      <div className="space-y-4">
        <div className="flex items-center space-x-2">
          <CheckCircle className="h-6 w-6 text-green-500" />
          <h3 className="text-lg font-semibold text-[#001965]">Processing Complete</h3>
        </div>

        <div className="space-y-2">
          <p className="text-sm text-gray-600">
            <span className="font-medium">Records processed:</span> {result.recordCount}
          </p>
          <p className="text-sm text-gray-600">
            <span className="font-medium">Output file:</span> {result.filename}
          </p>
        </div>

        {result.hasLocationData && result.locationSuggestions.length > 0 && (
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <MapPin className="h-4 w-4 text-[#001965]" />
              <span className="text-sm font-medium text-[#001965]">Location Suggestions Found</span>
            </div>
            <div className="bg-blue-50 p-3 rounded-lg space-y-1">
              {result.locationSuggestions.slice(0, 3).map((location, index) => (
                <p key={index} className="text-xs text-gray-700">• {location}</p>
              ))}
              {result.locationSuggestions.length > 3 && (
                <p className="text-xs text-gray-500">+{result.locationSuggestions.length - 3} more locations in the output file</p>
              )}
            </div>
          </div>
        )}

        <Button
          onClick={onDownload}
          className="w-full bg-[#001965] hover:bg-[#001965]/90 text-white"
        >
          <Download className="h-4 w-4 mr-2" />
          Download Processed xlsx
        </Button>
      </div>
    </Card>
  );
}
