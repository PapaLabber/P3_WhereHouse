import React, { useEffect, useRef, useState } from "react";
import { ChevronDown, Check } from "lucide-react";

export type Option<T extends string = string> = {
  label: string;
  value: T;
  disabled?: boolean;
};

type Props<T extends string> = {
  label: string;
  value: T | null;
  options: Option<T>[];
  placeholder?: string;
  onChange: (next: T) => void;
  className?: string;
};

export function DropdownSelect<T extends string>({
  label,
  value,
  options,
  placeholder = "Select…",
  onChange,
  className = "",
}: Props<T>) {
  const [open, setOpen] = useState(false);
  const btnRef = useRef<HTMLButtonElement | null>(null);
  const listRef = useRef<HTMLUListElement | null>(null);

  // close on outside click / Esc
  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (!open) return;
      const t = e.target as Node;
      if (!btnRef.current?.contains(t) && !listRef.current?.contains(t))
        setOpen(false);
    };
    const onKey = (e: KeyboardEvent) => {
      if (open && e.key === "Escape") {
        setOpen(false);
        btnRef.current?.focus();
      }
    };
    document.addEventListener("mousedown", onDocClick);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDocClick);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const current = value ? options.find((o) => o.value === value) : undefined;

  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      <span className="text-sm text-gray-700">{label}</span>
      <div className="relative">
        <button
          ref={btnRef}
          type="button"
          aria-haspopup="listbox"
          aria-expanded={open}
          onClick={() => setOpen((v) => !v)}
          className="w-full inline-flex items-center justify-between rounded-lg border bg-white px-3 py-2 text-left shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2"
        >
          <span className="truncate">
            {current ? (
              current.label
            ) : (
              <span className="text-gray-400">{placeholder}</span>
            )}
          </span>
          <ChevronDown className="ml-2 h-4 w-4" />
        </button>

        {open && (
          <ul
            ref={listRef}
            role="listbox"
            tabIndex={-1}
            className="absolute left-0 right-0 z-50 mt-2 max-h-56 w-full overflow-auto rounded-lg border bg-white p-1 shadow-2xl"
          >
            {options.map((o) => {
              const selected = o.value === value;
              return (
                <li
                  key={o.value}
                  role="option"
                  aria-selected={selected}
                  className={`flex cursor-pointer items-center justify-between rounded-md px-2 py-2 text-sm hover:bg-gray-100 ${
                    o.disabled ? "opacity-50 cursor-not-allowed" : ""
                  }`}
                  onClick={() => {
                    if (o.disabled) return;
                    onChange(o.value);
                    setOpen(false);
                    btnRef.current?.focus();
                  }}
                >
                  <span>{o.label}</span>
                  {selected && <Check className="h-4 w-4" />}
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
}
