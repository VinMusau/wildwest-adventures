import { defineConfig, mergeConfig } from "vitest/config";
import viteConfig from "./vite.config";

// Test settings live here so vite.config.ts stays about the app build.
export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: "jsdom",
      include: ["src/**/*.test.{ts,tsx}"],
      restoreMocks: true,
    },
  }),
);
