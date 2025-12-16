// FileUpload.test.tsx
// --------------------
// This file tests the FileUpload component by simulating:
// - default rendering
// - selecting files via <input type="file">
// - drag-and-drop uploading
// - verifying correct callback behavior
// - ensuring invalid file types are ignored

import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom"; // Extends expect() with DOM matchers
import { render, fireEvent } from "@testing-library/react";
import { FileUpload } from "../components/FileUpload";

describe("FileUpload", () => {
  // Test 1 : Default rendering with no file selected
  it("renders default state when no file is selected", () => {
    // Render component with no selected file
    const { getByText } = render(
      <FileUpload selectedFile={null} onFileSelect={() => {}} />
    );

    // Expect default UI text to appear
    expect(getByText("Drop your XLSX file here")).toBeInTheDocument();
    expect(getByText("or click to browse")).toBeInTheDocument();
    // Ensures initial state is correct
    // Ensures the UI tells the user what to do
  });

  // Test 2 : Selecting a valid .xlsx file via the hidden input
  it("calls onFileSelect when a .xlsx file is selected via input", () => {
    // Spy function to verify callback execution
    const onFileSelect = vi.fn();

    // Render upload component
    render(<FileUpload selectedFile={null} onFileSelect={onFileSelect} />);

    // Get the hidden file input
    const input = document.getElementById("file-input") as HTMLInputElement;

    // Simulate a valid XLSX file
    const file = new File(["test"], "test.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    // Trigger the change event
    fireEvent.change(input, { target: { files: [file] } });

    // Assert callback was called correctly
    expect(onFileSelect).toHaveBeenCalledTimes(1);
    expect(onFileSelect).toHaveBeenCalledWith(file);
    // Ensures the component behaves correctly
    // Ensures only valid file types are accepted
  });

  // Test 3 : Selecting an invalid file type
  it("does NOT call onFileSelect when a non-xlsx file is selected", () => {
    const onFileSelect = vi.fn();

    render(<FileUpload selectedFile={null} onFileSelect={onFileSelect} />);

    const input = document.getElementById("file-input") as HTMLInputElement;

    // Invalid file type
    const badFile = new File(["test"], "image.png", { type: "image/png" });

    fireEvent.change(input, { target: { files: [badFile] } });

    // Callback must NOT run
    expect(onFileSelect).not.toHaveBeenCalled();
    //Rejects wrong file types
    // Prevents accidental uploads
  });

  // Test 4 : Drag-and-drop a valid .xlsx file
  it("accepts .xlsx file via drag and drop", () => {
    const onFileSelect = vi.fn();

    const { getByText } = render(
      <FileUpload selectedFile={null} onFileSelect={onFileSelect} />
    );

    // The drop zone is the parent <div> around the text
    const dropArea = getByText("Drop your XLSX file here").parentElement!;

    const file = new File(["test"], "drag.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    // fireEvent.drop simulates dropping a file onto the element
    fireEvent.drop(dropArea, {
      dataTransfer: {
        files: [file],
      },
    } as unknown as DragEvent);

    expect(onFileSelect).toHaveBeenCalledTimes(1);
    expect(onFileSelect).toHaveBeenCalledWith(file);
    // drag-and-drop works
    // the callback receives the correct file
  });

  // Test 5 : Rendering with a selected file
  it("shows file info when selectedFile is provided", () => {
    // Component should display file info when prop is passed in
    const file = new File(["123"], "myFile.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const { getByText } = render(
      <FileUpload selectedFile={file} onFileSelect={() => {}} />
      // The test ensures correct rendering.
    );

    expect(getByText("Selected file:")).toBeInTheDocument();
    expect(getByText("myFile.xlsx")).toBeInTheDocument();
    // Displays the selected file name
  });
});
