import react from "@vitejs/plugin-react-swc";
import dns from "dns";
import path from "path";
import { defineConfig, loadEnv } from "vite";

//running on localhost instead of IP 127.0.0.1
// https://vitejs.dev/config/server-options.html#server-host
dns.setDefaultResultOrder("verbatim");

// https://vitejs.dev/config/
// https://v2.vitejs.dev/config/#environment-variables
export default defineConfig(({ command, mode }) => {
  // Load env file based on `mode` in the current working directory.
  // Set the third parameter to '' to load all env regardless of the `VITE_` prefix.
  const env = loadEnv(mode, process.cwd(), "");
  return {
    plugins: [
      react(),
      // visualizer() as PluginOption
    ],
    server: {
      port: parseInt(env.PORT),
    },
    preview: {
      port: parseInt(env.PORT),
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src/"),
        components: `${path.resolve(__dirname, "./src/components/")}`,
        styles: `${path.resolve(__dirname, "./src/styles/")}`,
        config: `${path.resolve(__dirname, "./src/config/")}`,
        pages: `${path.resolve(__dirname, "./src/pages/")}`,
      },
    },
  };
});
