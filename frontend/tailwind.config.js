/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#244B83',
          50: '#F0F4F9',
          100: '#E3EBF5',
          200: '#BDCEE5',
          500: '#244B83',
          600: '#1D3D6C',
          700: '#172F54',
        },
        navy: {
          DEFAULT: '#101D35',
          dark: '#0B1528',
          light: '#244B83',
        },
        gold: { DEFAULT: '#C49A45', light: '#F5EDDD', dark: '#87661F' },
        ivory: '#F8F6F0',
        feather: '#E8E9EC',
        blue: { 50: '#F0F4F9', 100: '#E3EBF5', 200: '#BDCEE5', 500: '#244B83', 600: '#244B83', 700: '#1D3D6C', 800: '#172F54', 900: '#101D35' },
        purple: { 50: '#FBF7EE', 100: '#F5EDDD', 200: '#E8D8B7', 500: '#C49A45', 600: '#87661F', 700: '#87661F', 800: '#75581B', 900: '#624916' },
        slate: { 50: '#F8F6F0', 100: '#F0F0ED', 200: '#E8E9EC', 400: '#718096', 500: '#59687D', 800: '#24334B', 900: '#101D35' },
        emerald: { 50: '#EFF7F2', 100: '#DAEDE2', 200: '#B5D9C5', 500: '#23865B', 600: '#23865B', 700: '#1D6B49', 800: '#18573C' },
        amber: { 50: '#FCF7EC', 100: '#F8EDCF', 200: '#EED9A3', 500: '#D99A32', 600: '#9A6B16', 800: '#755014', 900: '#624313' },
        red: { 50: '#FCF1F0', 100: '#F8E2DF', 200: '#EFC7C2', 500: '#C64C4C', 600: '#B14343', 700: '#963737' },
        accent: {
          purple: '#C49A45',
          purpleLight: '#F5EDDD',
        },
        success: {
          DEFAULT: '#23865B',
          light: '#eaf7f0',
        },
        warning: {
          DEFAULT: '#D99A32',
          light: '#fef7e6',
        },
        danger: {
          DEFAULT: '#C64C4C',
          light: '#fbeeee',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 8px 28px -12px rgba(16, 29, 53, 0.12), 0 1px 3px rgba(16, 29, 53, 0.03)',
        card: '0 5px 18px -8px rgba(16, 29, 53, 0.1)',
      },
      borderRadius: {
        '2xl': '1.25rem',
      }
    },
  },
  plugins: [],
}
