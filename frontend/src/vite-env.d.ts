/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  // add more vars here if needed, for example:
  // readonly VITE_OTHER_KEY: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}