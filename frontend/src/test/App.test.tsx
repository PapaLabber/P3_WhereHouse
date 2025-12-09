import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import App from "../App";

let fetchMock: ReturnType<typeof vi.fn>;

beforeEach(() => {
  vi.clearAllMocks();

  fetchMock = vi.fn().mockResolvedValue({
    ok: true,
    blob: () =>
      Promise.resolve(new Blob(["test"], { type: "application/octet-stream" })),
  });

  // Mock fetch and URL.createObjectURL so no real network / download happens
  globalThis.fetch = fetchMock as any;
  globalThis.URL.createObjectURL = vi.fn(() => "mock-url");
});

describe("App.tsx", () => {
  it("renders the Warehouse header", () => {
    render(<App />);
    expect(screen.getByText("Warehouse")).toBeInTheDocument();
  });

  it("lets the user select country and year", () => {
    render(<App />);

    const selects = screen.getAllByRole("combobox"); // [country, year]
    const countrySelect = selects[0];
    const yearSelect = selects[1];

    fireEvent.change(countrySelect, { target: { value: "DENMARK" } });
    fireEvent.change(yearSelect, { target: { value: "2025" } });

    expect(countrySelect).toHaveValue("DENMARK");
    expect(yearSelect).toHaveValue("2025");
  });

  it("lets the user upload a file", () => {
    render(<App />);

    const file = new File(["content"], "example.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const input = document.getElementById("file-input") as HTMLInputElement;
    fireEvent.change(input, { target: { files: [file] } });

    expect(screen.getByText("example.xlsx")).toBeInTheDocument();
  });

  it("shows Step 3 section", () => {
    render(<App />);
    expect(screen.getByText("Step 3: Download Results")).toBeInTheDocument();
  });

  it("calls fetch once when clicking Download File button after selecting inputs", async () => {
    render(<App />);

    // Select country & year
    const selects = screen.getAllByRole("combobox");
    const countrySelect = selects[0];
    const yearSelect = selects[1];

    fireEvent.change(countrySelect, { target: { value: "DENMARK" } });
    fireEvent.change(yearSelect, { target: { value: "2025" } });

    // Upload file
    const file = new File(["x"], "data.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const input = document.getElementById("file-input") as HTMLInputElement;
    fireEvent.change(input, { target: { files: [file] } });

    // After upload, ResultsDownload should show a button
    const downloadButton = await screen.findByRole("button", {
      name: /download file/i,
    });

    // With current App logic, fetch has NOT yet been called at this point
    expect(fetchMock).toHaveBeenCalledTimes(0);

    // Click Download → handleDownload → handleProcess → fetch
    fireEvent.click(downloadButton);

    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
});
