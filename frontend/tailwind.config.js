/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
        display: ['Outfit', 'sans-serif'],
      },
      colors: {
        dark: { '900': '#060a13', '800': '#0c1222', '700': '#141c30', '600': '#1e293b' },
      },
      animation: {
        'float': 'float 8s ease-in-out infinite',
        'float-delayed': 'float 8s ease-in-out 3s infinite',
        'pulse-soft': 'pulse-soft 3s ease-in-out infinite',
        'gradient-orbit': 'gradient-orbit 15s ease-in-out infinite',
        'spin-slow': 'spin-slow 25s linear infinite',
        'slide-in': 'slide-in-right 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
        'slide-out': 'slide-out-right 0.3s ease-in forwards',
      },
    },
  },
  plugins: [],
}
