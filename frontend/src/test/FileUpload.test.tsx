import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, fireEvent } from "@testing-library/react";
import { FileUpload } from "../components/FileUpload";

describe("FileUpload", () => {
  it("renders default state when no file is selected", () => {
    const { getByText } = render(
      <FileUpload selectedFile={null} onFileSelect={() => {}} />
    );

    expect(getByText("Drop your XLSX file here")).toBeInTheDocument();
    expect(getByText("or click to browse")).toBeInTheDocument();
  });

  it("calls onFileSelect when a .xlsx file is selected via input", () => {
    const onFileSelect = vi.fn();

    render(<FileUpload selectedFile={null} onFileSelect={onFileSelect} />);

    const input = document.getElementById("file-input") as HTMLInputElement;

    const file = new File(["test"], "test.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    fireEvent.change(input, { target: { files: [file] } });

    expect(onFileSelect).toHaveBeenCalledTimes(1);
    expect(onFileSelect).toHaveBeenCalledWith(file);
  });

  it("does NOT call onFileSelect when a non-xlsx file is selected", () => {
    const onFileSelect = vi.fn();

    render(<FileUpload selectedFile={null} onFileSelect={onFileSelect} />);

    const input = document.getElementById("file-input") as HTMLInputElement;

    const badFile = new File(["test"], "image.png", { type: "image/png" });

    fireEvent.change(input, { target: { files: [badFile] } });

    expect(onFileSelect).not.toHaveBeenCalled();
  });

  it("accepts .xlsx file via drag and drop", () => {
    const onFileSelect = vi.fn();

    const { getByText } = render(
      <FileUpload selectedFile={null} onFileSelect={onFileSelect} />
    );

    const dropArea = getByText("Drop your XLSX file here").parentElement!;
    const file = new File(["test"], "drag.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    fireEvent.drop(dropArea, {
      dataTransfer: {
        files: [file],
      },
    } as unknown as DragEvent);

    expect(onFileSelect).toHaveBeenCalledTimes(1);
    expect(onFileSelect).toHaveBeenCalledWith(file);
  });

  it("shows file info when selectedFile is provided", () => {
    const file = new File(["123"], "myFile.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const { getByText } = render(
      <FileUpload selectedFile={file} onFileSelect={() => {}} />
    );

    expect(getByText("Selected file:")).toBeInTheDocument();
    expect(getByText("myFile.xlsx")).toBeInTheDocument();
  });
});
