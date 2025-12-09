import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { PromptInputs } from "../components/PromptInputs";

describe("PromptInputs", () => {
  const setup = (props = {}) => {
    const defaultProps = {
      prompts: ["", "", ""] as [string, string, string],
      setPrompts: vi.fn(),
      onProcess: vi.fn(),
      isProcessing: false,
      disabled: false,
      ...props,
    };

    render(<PromptInputs {...defaultProps} />);
    return defaultProps;
  };

  it("renders three labeled text areas", () => {
    setup();

    expect(screen.getByLabelText("Data Filter Specification 1")).toBeInTheDocument();
    expect(screen.getByLabelText("Data Filter Specification 2")).toBeInTheDocument();
    expect(screen.getByLabelText("Data Filter Specification 3")).toBeInTheDocument();
  });

  it("updates prompt text when typing", () => {
    const props = setup();

    const textarea = screen.getByLabelText("Data Filter Specification 1");
    fireEvent.change(textarea, { target: { value: "Hello world" } });

    expect(props.setPrompts).toHaveBeenCalled();
  });

  it("calls onProcess when clicking the button", () => {
    const props = setup({
      prompts: ["something", "", ""], // button must be enabled
    });

    const button = screen.getByRole("button", { name: /process data/i });
    fireEvent.click(button);

    expect(props.onProcess).toHaveBeenCalled();
  });

  it("calls onProcess when pressing Ctrl + Enter", () => {
    const props = setup({
      prompts: ["abc", "", ""],
    });

    const textarea = screen.getByLabelText("Data Filter Specification 1");

    fireEvent.keyDown(textarea, {
      key: "Enter",
      ctrlKey: true,
    });

    expect(props.onProcess).toHaveBeenCalled();
  });

  it("does NOT call onProcess when disabled", () => {
    const props = setup({
      prompts: ["abc", "", ""],
      disabled: true,
    });

    const textarea = screen.getByLabelText("Data Filter Specification 1");

    fireEvent.keyDown(textarea, {
      key: "Enter",
      ctrlKey: true,
    });

    expect(props.onProcess).not.toHaveBeenCalled();
  });

  it("does NOT call onProcess when isProcessing = true", () => {
    const props = setup({
      prompts: ["abc", "", ""],
      isProcessing: true,
    });

    const textarea = screen.getByLabelText("Data Filter Specification 1");

    fireEvent.keyDown(textarea, {
      key: "Enter",
      ctrlKey: true,
    });

    expect(props.onProcess).not.toHaveBeenCalled();
  });
});
