/** @type {import('tailwindcss').Config} */

module.exports = {
  content: ['./public/**/*.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}
