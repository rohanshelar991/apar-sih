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
          DEFAULT: '#0070C0',
          50: '#f0f7ff',
          100: '#e0effe',
          200: '#bae0fd',
          500: '#0070C0',
          600: '#005fa3',
          700: '#004c82',
        },
        navy: {
          DEFAULT: '#1F497D',
          dark: '#143054',
          light: '#2a63a8',
        },
        accent: {
          purple: '#7F5FA8',
          purpleLight: '#f3eef9',
        },
        success: {
          DEFAULT: '#2E9E5B',
          light: '#eaf7f0',
        },
        warning: {
          DEFAULT: '#F2A100',
          light: '#fef7e6',
        },
        danger: {
          DEFAULT: '#D64545',
          light: '#fbeeee',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 4px 20px -2px rgba(31, 73, 125, 0.08), 0 2px 6px -1px rgba(31, 73, 125, 0.04)',
        card: '0 2px 12px rgba(0, 0, 0, 0.04), 0 1px 3px rgba(0, 0, 0, 0.02)',
      },
      borderRadius: {
        '2xl': '1rem',
      }
    },
  },
  plugins: [],
}
