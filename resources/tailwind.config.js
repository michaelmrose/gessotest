module.exports = {
  // Add this block to stop Tailwind from "breaking" Basecoat
  corePlugins: {
    preflight: false,
  },
  content: [
    './src/**/*',
    './resources/**/*',
  ],
  theme: {
    extend: {},
  },
  plugins: [
    /* require('@tailwindcss/forms'), */
  ],
}
