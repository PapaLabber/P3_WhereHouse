// PromptInputs.test.tsx
// ----------------------
// This test suite validates the behavior of the PromptInputs component:
// - Rendering three labeled text areas
// - Updating prompts via setPrompts when typing
// - Triggering onProcess via button click or Ctrl+Enter
// - Ensuring onProcess does NOT run when disabled or processing

import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { PromptInputs } from "../components/PromptInputs";

describe("PromptInputs", () => {
  // Helper: renders the component with default props unless overridden.
  // This keeps each test clean and consistent.
  const setup = (props = {}) => {
    const defaultProps = {
      prompts: ["", "", ""] as [string, string, string],
      setPrompts: vi.fn(),  // mock function to observe updates
      onProcess: vi.fn(),   // mock function to see if processing is triggered
      isProcessing: false,
      disabled: false,
      ...props,
    };

    render(<PromptInputs {...defaultProps} />);
    return defaultProps;
    // It keeps tests clean and avoids repeating boilerplate
    // It allows overriding props for specific test scenarios
    // It returns the mock functions so tests can assert how they were called
    //
    // setup() saves you from repeating code, lets you change props easily for each test,
    // and gives you access to the mock functions so you can verify component behavior.
  };

  // Test 1: Renders three labeled text areas
  it("renders three labeled text areas", () => {
    setup();

    // Each label must exist and be associated to a <textarea>
    expect(screen.getByLabelText("Data Filter Specification 1")).toBeInTheDocument();
    expect(screen.getByLabelText("Data Filter Specification 2")).toBeInTheDocument();
    expect(screen.getByLabelText("Data Filter Specification 3")).toBeInTheDocument();
    // Labels exist
    // Labels correctly connect to the <textarea> via htmlFor and id
    // The UI is accessible (following ARIA guidelines) --> Dette kan være aria-label="Close" for knapper det fortæller en blind at det er en luk knap.
  });

  // Test 2: Updates prompt text when typing
  it("updates prompt text when typing", () => {
    const props = setup();

    // Simulate typing into the first prompt textarea
    const textarea = screen.getByLabelText("Data Filter Specification 1");
    fireEvent.change(textarea, { target: { value: "Hello world" } });

    // The component should call setPrompts with updated values
    expect(props.setPrompts).toHaveBeenCalled();
  });

  // Test 3: Calls onProcess when clicking button or pressing Ctrl+Enter
  it("calls onProcess when clicking the button", () => {
    // One non-empty prompt required or button is disabled
    const props = setup({
      prompts: ["something", "", ""],
    });

    // Find the "Process Data" button
    const button = screen.getByRole("button", { name: /process data/i });
    fireEvent.click(button);

    // onProcess must run
    expect(props.onProcess).toHaveBeenCalled();
    // When the user clicks the “Process Data” button,
    // the component should call the onProcess function.
  });

  // Test 4: Calls onProcess when pressing Ctrl+Enter
  it("calls onProcess when pressing Ctrl + Enter", () => {
    const props = setup({
      prompts: ["abc", "", ""], // enables processing
    });

    const textarea = screen.getByLabelText("Data Filter Specification 1");

    // Simulate pressing Ctrl+Enter
    fireEvent.keyDown(textarea, {
      key: "Enter",
      ctrlKey: true,
    });

    expect(props.onProcess).toHaveBeenCalled();
    // Keyboard shortcut works
    // UI behaves like a real data-entry tool
  });

  // Test 5A: Does NOT call onProcess when disabled or processing
  it("does NOT call onProcess when disabled", () => {
    const props = setup({
      prompts: ["abc", "", ""],
      disabled: true, // disables all processing actions
    });

    const textarea = screen.getByLabelText("Data Filter Specification 1");

    fireEvent.keyDown(textarea, {
      key: "Enter",
      ctrlKey: true,
    });

    expect(props.onProcess).not.toHaveBeenCalled();
  });

  // Test 5B: Does NOT call onProcess when isProcessing = true
  it("does NOT call onProcess when isProcessing = true", () => {
    const props = setup({
      prompts: ["abc", "", ""],
      isProcessing: true, // component is busy; must ignore input
    });

    const textarea = screen.getByLabelText("Data Filter Specification 1");

    // Ctrl+Enter should be ignored during processing
    fireEvent.keyDown(textarea, {
      key: "Enter",
      ctrlKey: true,
    });

    expect(props.onProcess).not.toHaveBeenCalled();
  });

  // To prevent double execution, accidental processing, or user interaction while processing.
});
