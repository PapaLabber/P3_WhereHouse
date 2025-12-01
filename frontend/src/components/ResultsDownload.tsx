import { FileText, Download } from "lucide-react";
import { Card } from "./ui/card";
import { Button } from "./ui/button";

interface ResultsDownloadProps {
  filename: string | null;   // backend result name
  filesize?: number | null;  // optional size from backend
  onDownload: () => void;    // triggers backend download
}

export function ResultsDownload({ filename, filesize, onDownload }: ResultsDownloadProps) {
  const formatSize = (bytes: number | null | undefined) => {
    if (!bytes) return "";
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  return (
    <Card className="p-8 border-dashed border-2 border-gray-300 text-center">
      {!filename ? (
        <div className="text-gray-500">
          <FileText className="w-10 h-10 mx-auto text-gray-400 mb-3" />
          <p>No results yet</p>
        </div>
      ) : (
        <div className="flex flex-col items-center space-y-3">
          <FileText className="w-10 h-10 text-[#001965]" />

          <div className="text-sm text-gray-700">
            <span className="font-medium block text-[#001965]">
              {filename}
            </span>
            {filesize && (
              <span className="text-xs text-gray-500">
                {formatSize(filesize)}
              </span>
            )}
          </div>

          <Button
            onClick={onDownload}
            className="bg-[#001965] text-white hover:bg-[#001965]/90"
          >
            <Download className="w-4 h-4 mr-2" />
            Download File
          </Button>
        </div>
      )}
    </Card>
  );
}
