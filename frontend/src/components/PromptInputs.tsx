import React from 'react';
import { Card } from './ui/card';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';
import { Button } from './ui/button';
import { Play } from 'lucide-react';

interface PromptInputsProps {
  prompts: [string, string, string];
  setPrompts: React.Dispatch<React.SetStateAction<[string, string, string]>>;
  onProcess: () => void;
  isProcessing: boolean;
  disabled: boolean;
}

export function PromptInputs({ prompts, setPrompts, onProcess, isProcessing, disabled }: PromptInputsProps) {
  const handlePromptChange = (index: number, value: string) => {
    const newPrompts: [string, string, string] = [...prompts];
    newPrompts[index] = value;
    setPrompts(newPrompts);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && e.ctrlKey && !disabled && !isProcessing) {
      onProcess();
    }
  };

  const promptLabels = [
    'Data Filter Specification 1',
    'Data Filter Specification 2', 
    'Data Filter Specification 3'
  ];

  const promptPlaceholders = [
    'e.g., "Extract all records where the status is active"',
    'e.g., "Include only entries with dates from the last 30 days"',
    'e.g., "Group by location and show addresses for mapping"'
  ];

  return (
    <Card className="p-6">
      <div className="space-y-6">
        <div>
          <h2 className="text-xl font-semibold text-[#001965] mb-2">Data Processing Specifications</h2>
          <p className="text-sm text-gray-600">Enter three prompts to specify how you want the data processed. Press Ctrl+Enter to process.</p>
        </div>
        
        {prompts.map((prompt, index) => (
          <div key={index} className="space-y-2">
            <Label htmlFor={`prompt-${index}`} className="text-[#001965]">
              {promptLabels[index]}
            </Label>
            <Textarea
              id={`prompt-${index}`}
              value={prompt}
              onChange={(e) => handlePromptChange(index, e.target.value)}
              onKeyDown={handleKeyPress}
              placeholder={promptPlaceholders[index]}
              disabled={disabled}
              className="min-h-[80px] resize-none focus:border-[#001965] focus:ring-[#001965]"
            />
          </div>
        ))}

        <Button
          onClick={onProcess}
          disabled={disabled || isProcessing || prompts.every(p => !p.trim())}
          className="w-full bg-[#001965] hover:bg-[#001965]/90 text-white"
        >
          {isProcessing ? (
            <div className="flex items-center space-x-2">
              <div className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></div>
              <span>Processing Data...</span>
            </div>
          ) : (
            <div className="flex items-center space-x-2">
              <Play className="h-4 w-4" />
              <span>Process Data</span>
            </div>
          )}
        </Button>
      </div>
    </Card>
  );
}