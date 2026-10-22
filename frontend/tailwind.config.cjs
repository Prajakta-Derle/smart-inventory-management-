/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Brand palette — indigo primary (kept for accents on the neumorphic base).
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
        },
        accent: {
          50: '#f0fdfa',
          100: '#ccfbf1',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
        },
        // Neumorphic base surface — near-white so it reads as "white" while
        // still allowing the light highlight shadow to show.
        neu: {
          base: '#f4f6fb',
          light: '#ffffff',
          dark: '#d4dae6',
        },
      },
      boxShadow: {
        // Raised (extruded) element: light top-left, dark bottom-right.
        neu: '6px 6px 12px #d4dae6, -6px -6px 12px #ffffff',
        'neu-sm': '4px 4px 8px #d4dae6, -4px -4px 8px #ffffff',
        'neu-lg': '9px 9px 18px #d4dae6, -9px -9px 18px #ffffff',
        // Pressed / inset element.
        'neu-inset': 'inset 4px 4px 8px #d4dae6, inset -4px -4px 8px #ffffff',
        'neu-inset-sm': 'inset 3px 3px 6px #d4dae6, inset -3px -3px 6px #ffffff',
      },
      keyframes: {
        'fade-in': {
          from: { opacity: '0' },
          to: { opacity: '1' },
        },
        'fade-up': {
          from: { opacity: '0', transform: 'translateY(10px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'scale-in': {
          from: { opacity: '0', transform: 'scale(0.95)' },
          to: { opacity: '1', transform: 'scale(1)' },
        },
        'slide-in-right': {
          from: { opacity: '0', transform: 'translateX(16px)' },
          to: { opacity: '1', transform: 'translateX(0)' },
        },
      },
      animation: {
        'fade-in': 'fade-in 0.25s ease-out both',
        'fade-up': 'fade-up 0.35s ease-out both',
        'scale-in': 'scale-in 0.2s ease-out both',
        'slide-in-right': 'slide-in-right 0.3s ease-out both',
      },
    },
  },
  plugins: [require('@tailwindcss/forms')],
};
