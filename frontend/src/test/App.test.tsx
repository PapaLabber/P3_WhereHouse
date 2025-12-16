// App.test.tsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import App from "../App";

let fetchMock: ReturnType<typeof vi.fn>;

beforeEach(() => {
  // Reset all mocks before every test to avoid cross-test pollution
  vi.clearAllMocks();

  // Fake fetch() so we do NOT call the backend during tests.
  // Instead, fetch returns a mocked Response with a blob().
  fetchMock = vi.fn().mockResolvedValue({
    ok: true,
    blob: () =>
      Promise.resolve(new Blob(["test"], { type: "application/octet-stream" })),
  });

  // Attach the mocked fetch to the global object
  globalThis.fetch = fetchMock as any;

  // App.tsx uses URL.createObjectURL for downloads — mock it so no real URL is created
  globalThis.URL.createObjectURL = vi.fn(() => "mock-url");
});

describe("App.tsx", () => {
  it("renders the Warehouse header", () => {
    // Render the component in a simulated DOM
    render(<App />);

    // Check that the header text exists
    expect(screen.getByText("Warehouse")).toBeInTheDocument();
  });

  it("lets the user select country and year", () => {
    render(<App />);

    // The two <select> elements (Country + Year)
    const selects = screen.getAllByRole("combobox");
    const countrySelect = selects[0];
    const yearSelect = selects[1];

    // Simulate choosing Denmark and 2025
    fireEvent.change(countrySelect, { target: { value: "DENMARK" } });
    fireEvent.change(yearSelect, { target: { value: "2025" } });

    // Verify React state updated correctly
    expect(countrySelect).toHaveValue("DENMARK");
    expect(yearSelect).toHaveValue("2025");
  });

  it("lets the user upload a file", () => {
    render(<App />);

    // Create a fake XLSX file object
    const file = new File(["content"], "example.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    // App's file input is given id="file-input"
    const input = document.getElementById("file-input") as HTMLInputElement;

    // Simulate uploading
    fireEvent.change(input, { target: { files: [file] } });

    // FileUpload component shows the filename after upload
    expect(screen.getByText("example.xlsx")).toBeInTheDocument();
  });

  it("shows Step 3 section", () => {
    render(<App />);

    // Simple check: this step should always exist
    expect(screen.getByText("Step 3: Download Results")).toBeInTheDocument();
  });

  it("calls fetch once when clicking Download File button after selecting inputs", async () => {
    render(<App />);
  
    // Select Country + Year to satisfy prerequisites for processing
    const selects = screen.getAllByRole("combobox");
    fireEvent.change(selects[0], { target: { value: "DENMARK" } });
    fireEvent.change(selects[1], { target: { value: "2025" } });
  
    // Create and upload a fake file
    const file = new File(["x"], "data.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const input = document.getElementById("file-input") as HTMLInputElement;
    fireEvent.change(input, { target: { files: [file] } });
  
    // ResultsDownload renders a Download button once file and inputs exist
    const downloadButton = await screen.findByRole("button", {
      name: /download file/i,
    });
  
    // Capture how many times fetch has been called *before* the click
    const callsBeforeClick = fetchMock.mock.calls.length;
  
    // Click the download button manually
    fireEvent.click(downloadButton);
  
    // Assert exactly ONE new fetch call happened due to the click
    expect(fetchMock).toHaveBeenCalledTimes(callsBeforeClick + 1);
  });
});
