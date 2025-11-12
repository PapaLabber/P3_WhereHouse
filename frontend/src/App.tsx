import React, { useState } from "react";
import { FileUpload } from "./components/FileUpload";
import { PromptInputs } from "./components/PromptInputs";
import { ResultsDownload } from "./components/ResultsDownload";
import { processData, type ProcessedResult } from "./utils/csvProcessor";
import { Warehouse } from "lucide-react";
import { DropdownSelect, type Option } from "./components/DropdownSelect";

type Country = "DENMARK" | "SWEDEN" | "NORWAY";
type Year = "2024" | "2025" | "2026";

const COUNTRY_OPTIONS: Option<Country>[] = [
  { label: "Denmark", value: "DENMARK" },
  { label: "Sweden", value: "SWEDEN" },
  { label: "Norway", value: "NORWAY", disabled: true },
];

const YEAR_OPTIONS: Option<Year>[] = [
  { label: "2024", value: "2024" },
  { label: "2025", value: "2025" },
  { label: "2026", value: "2026", disabled: true },
];

export default function App() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [prompts, setPrompts] = useState<[string, string, string]>([
    "",
    "",
    "",
  ]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [result, setResult] = useState<ProcessedResult | null>(null);

  const [country, setCountry] = useState<Country | null>(null);
  const [year, setYear] = useState<Year | null>(null);

  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
    setResult(null); // Clear previous results

    if(country && year) handleProcess();
  };

  const API = import.meta.env.VITE_API_URL ?? "http://localhost:3000";

  const handleProcess = async () => {
    if (!selectedFile || !country || !year) return;

    setIsProcessing(true);
    try {
      const form = new FormData();
      form.append("file", selectedFile, selectedFile.name);
      form.append("wantedCountry", country);
      form.append("wantedYear", year.toString());

      const res = await fetch(`${API}/api/export`, {
        method: "POST",
        body: form, // <-- browser sets multipart boundary automatically
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const blob = await res.blob();
      const url = URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = url;
      a.download = `AllocatedResult${country}${year}.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();

      URL.revokeObjectURL(url);
    } catch (err) {
      console.error("Processing failed:", err);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDownload = () => {
    if (!result) return;

    const blob = new Blob([result.csvData], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
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
            <h1 className="text-3xl font-bold">Warehouse</h1>
          </div>
          <p className="mt-2 text-blue-100">XXX</p>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column */}
          <div className="space-y-6">
            {/* Info Card */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 className="font-semibold text-[#001965] mb-2">
                How it works
              </h3>
              <ul className="text-sm text-gray-700 space-y-1">
                <li>• Upload your xlsx file with any data structure</li>
                <li>• WhereHouse will intelligently process your data</li>
                <li>
                  • Location-based prompts will generate Google Maps suggestions
                </li>
                <li>• Download your processed xlsx with the refined dataset</li>
              </ul>
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-7">
            <div>
              <h2 className="text-xl font-semibold text-[#001965] mb-4">
                Step 1: Filter your search
              </h2>

              <div className="grid gap-6 md:grid-cols-3">
                <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                  <h3 className="text-[#001965] font-medium mb-2">Country</h3>
                  <DropdownSelect
                    label=""
                    value={country}
                    onChange={setCountry}
                    options={COUNTRY_OPTIONS}
                    placeholder="Choose a country"
                  />
                </div>

                <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                  <h3 className="text-[#001965] font-medium mb-2">Year</h3>
                  <DropdownSelect
                    label=""
                    value={year}
                    onChange={setYear}
                    options={YEAR_OPTIONS}
                    placeholder="Pick a year"
                  />
                </div>
              </div>
            </div>

            <div>
              <h2 className="text-xl font-semibold text-[#001965] mb-4">
                Step 2: Upload xlsx File
              </h2>
              <FileUpload
                onFileSelect={handleFileSelect}
                selectedFile={selectedFile}
              />
            </div>

            <div>
              <h2 className="text-xl font-semibold text-[#001965] mb-4">
                Step 3: Download Results
              </h2>
              <ResultsDownload result={result} onDownload={handleDownload} />
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
