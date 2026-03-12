module.exports = {
  // Add this block to stop Tailwind from "breaking" Basecoat
  corePlugins: {
    preflight: false,
    transform: false,
  },
  content: [
    './src/**/*',
    './resources/**/*',
  ],
theme: {},
  plugins: [
  ],
}
