import { describe, it, expect, vi } from "vitest";
import { render, fireEvent } from "@testing-library/react";
import { ResultsDownload } from "../components/ResultsDownload";

describe("ResultsDownload", () => {

  // Test 1: Component should show a placeholder message when no result file exists
  it("shows 'No results yet' when filename is null", () => {
    const { getByText } = render(
      <ResultsDownload filename={null} onDownload={() => {}} />
    );

    // If no file has been generated yet, we expect the placeholder text
    expect(getByText("No results yet")).toBeInTheDocument();
  });

  // Test 2: If filename is provided, it should appear in the UI
  it("renders filename when provided", () => {
    const { getByText } = render(
      <ResultsDownload filename="output.xlsx" onDownload={() => {}} />
    );

    // The filename should be displayed to the user
    expect(getByText("output.xlsx")).toBeInTheDocument();
  });

  // Test 3: Component correctly formats and displays file size
  it("renders formatted file size when provided", () => {
    const { getByText } = render(
      <ResultsDownload
        filename="data.xlsx"
        filesize={2048} // = 2 KB
        onDownload={() => {}}
      />
    );

    // 2048 bytes → "2.0 KB"
    expect(getByText("2.0 KB")).toBeInTheDocument();
  });

  // Test 4: The Download button triggers the callback when clicked
  it("calls onDownload when the button is clicked", () => {
    const onDownload = vi.fn(); // mock callback (spy)

    const { getByRole } = render(
      <ResultsDownload
        filename="file.xlsx"
        filesize={1500}
        onDownload={onDownload}
      />
    );

    // Find the Download button (accessible by role)
    const button = getByRole("button", { name: /download file/i });

    // Simulate user clicking the button
    fireEvent.click(button);

    // onDownload should fire exactly one time
    expect(onDownload).toHaveBeenCalledTimes(1);
  });

  // Test 5: When filename is null, the component should NOT show a button or filesize UI
  it("does not render button or file info when filename is null", () => {
    const { queryByRole, queryByText } = render(
      <ResultsDownload filename={null} onDownload={() => {}} />
    );

    // No "button" role should exist because download is impossible
    expect(queryByRole("button")).not.toBeInTheDocument();

    // No file sizes should be visible
    expect(queryByText(/KB|MB|B/)).not.toBeInTheDocument();
  });
});
