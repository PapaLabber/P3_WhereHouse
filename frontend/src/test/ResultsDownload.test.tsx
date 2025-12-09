import { describe, it, expect, vi } from "vitest";
import { render, fireEvent } from "@testing-library/react";
import { ResultsDownload } from "../components/ResultsDownload";

describe("ResultsDownload", () => {
  it("shows 'No results yet' when filename is null", () => {
    const { getByText } = render(
      <ResultsDownload filename={null} onDownload={() => {}} />
    );

    expect(getByText("No results yet")).toBeInTheDocument();
  });

  it("renders filename when provided", () => {
    const { getByText } = render(
      <ResultsDownload filename="output.xlsx" onDownload={() => {}} />
    );

    expect(getByText("output.xlsx")).toBeInTheDocument();
  });

  it("renders formatted file size when provided", () => {
    const { getByText } = render(
      <ResultsDownload
        filename="data.xlsx"
        filesize={2048} // 2 KB
        onDownload={() => {}}
      />
    );

    expect(getByText("2.0 KB")).toBeInTheDocument();
  });

  it("calls onDownload when the button is clicked", () => {
    const onDownload = vi.fn();

    const { getByRole } = render(
      <ResultsDownload
        filename="file.xlsx"
        filesize={1500}
        onDownload={onDownload}
      />
    );

    const button = getByRole("button", { name: /download file/i });

    fireEvent.click(button);

    expect(onDownload).toHaveBeenCalledTimes(1);
  });

  it("does not render button or file info when filename is null", () => {
    const { queryByRole, queryByText } = render(
      <ResultsDownload filename={null} onDownload={() => {}} />
    );

    expect(queryByRole("button")).not.toBeInTheDocument();
    expect(queryByText(/KB|MB|B/)).not.toBeInTheDocument();
  });
});
