import React, { useState, useCallback, useEffect } from "react";
import { FileUpload } from "./components/FileUpload";
import { PromptInputs } from "./components/PromptInputs";
import { ResultsDownload } from "./components/ResultsDownload";
import { processDataFromBackend, type ProcessedResult } from "./utils/dataProcessor";
import { Warehouse } from "lucide-react";
import { DropdownSelect, type Option } from "./components/DropdownSelect";
import WarehouseDashboardComponent from "./components/WarehouseDashboard";

type Country = "DENMARK" | "SWEDEN" | "NORWAY";
type Year = "2025" | "2026" | "2027";

const COUNTRY_OPTIONS: Option<Country>[] = [
  { label: "Denmark", value: "DENMARK" },
  { label: "Sweden", value: "SWEDEN" },
  { label: "Norway", value: "NORWAY", disabled: true },
];

const YEAR_OPTIONS: Option<Year>[] = [
  { label: "2025", value: "2025" },
  { label: "2026", value: "2026" },
  { label: "2027", value: "2027", disabled: true },
];

export default function App() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [prompts, setPrompts] = useState<[string, string, string]>([
    "",
    "",
    "",
  ]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [country, setCountry] = useState<Country | null>(null);
  const [year, setYear] = useState<Year | null>(null);

  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
    if (country && year) handleProcess();
  };

  const API = "http://localhost:8080";

  const handleProcess = useCallback(async () => {
    if (!selectedFile || !country || !year) return;

    setIsProcessing(true);
    try {
      const form = new FormData();
      form.append("file", selectedFile, selectedFile.name);

      // Tilpas disse to linjer til hvad backend forventer:
      // Eksempel: hvis Country er et objekt med 'code' felt:
      form.append("wantedCountry", (country as any).code ?? String(country));
      form.append("wantedYear", String(year));

      const res = await fetch(`${API}/api/export`, {
        method: "POST",
        body: form,
      });

      if (!res.ok) {
        const text = await res.text().catch(() => "");
        console.error("Export failed", res.status, text);
        alert(`Download fejlede (HTTP ${res.status}). Se console for detaljer.`);
        return;
      }


      const blob = await res.blob();
      if (!blob || blob.size === 0) {
        console.error("Empty blob received");
        alert("Modtog en tom fil fra serveren.");
        return;
      }

      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `AllocatedResult-${(country as any).code ?? country}-${year}.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error("Error while downloading file", err);
      alert("Der opstod en fejl under download. Se console for detaljer.");
    } finally {
      setIsProcessing(false);
    }
  }, [selectedFile, country, year]);

  const handleDownload = () => {
    handleProcess(); // re-download from backend
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
          <p className="mt-2 text-blue-100">Allocation Tool for Distributing Materials Across Warehouses</p>
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
                <li>• Upload your XLSX file following the specified data structure to ensure correct processing.</li>
                <li>• WhereHouse will intelligently process your data</li>
                <li>• Download your processed xlsx with the refined dataset</li>
                <li>• A dashboard will be displayed after the output results have been processed.</li>
              </ul>
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-7">
            <div className="border border-border bg-card rounded-lg p-6">
              <h2 className="text-xl font-semibold text-[#001965] mb-3">
                Step 1: Select Country &amp; Year
              </h2>
              <p className="text-sm text-muted-foreground mb-4">
                Choose the country and year for which you want to allocate the pallets.
              </p>

              <div className="grid grid-cols-2 gap-4">
                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-[#001965]">
                    Country
                  </label>
                  <select
                    className="border border-border rounded-md bg-input-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={country ?? ""}
                    onChange={(e) => setCountry(e.target.value as Country)}
                  >
                    <option value="">Select country</option>
                    <option value="DENMARK">Denmark</option>
                    <option value="SWEDEN">Sweden</option>
                    <option value="NORWAY">Norway</option>
                    {/* dine øvrige lande */}
                  </select>
                </div>

                <div className="flex flex-col gap-1">
                  <label className="text-sm font-medium text-[#001965]">
                    Year
                  </label>
                  <select
                    className="border border-border rounded-md bg-input-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={year ?? ""}
                    onChange={(e) => setYear(e.target.value as Year)}
                  >
                    <option value="">Select year</option>
                    <option value="2025">2025</option>
                    <option value="2026">2026</option>
                    <option value="2027">2027</option>
                    {/* dine øvrige år */}
                  </select>
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
              <ResultsDownload
                filename={selectedFile ? `AllocatedResult${country}${year}.xlsx` : null}
                filesize={selectedFile?.size}
                onDownload={handleDownload}
              />
            </div>
          </div>
        </div>
        
        <section className="mt-10">
        <h2 className="text-xl font-semibold text-[#001965] mb-4">
          Dashboard
        </h2>
        <WarehouseDashboardComponent isProcessing={isProcessing} />
      </section>

    </main>
  </div>
);
}
