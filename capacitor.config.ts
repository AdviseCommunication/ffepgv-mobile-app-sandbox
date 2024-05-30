import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'fr.ffepgv.sandbox',
  appName: 'Sandbox',
  webDir: 'dist',
  plugins: {
    'AudioPlayer': {}
  }
};

export default config;
