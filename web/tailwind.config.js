/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        // Primary: Emerald Green #123C2D
        emerald: {
          deep: "#081C15",
          dark: "#0C291E",
          primary: "#123C2D",
          accent: "#1A5641",
          light: "#267A5D",
          soft: "#EBF4F0",
          container: "#123C2D"
        },
        // Secondary / Accent: Royal Gold #D4AF57
        gold: {
          primary: "#D4AF57",
          accent: "#E5C678",
          dark: "#A68532",
          muted: "#B5923B",
          soft: "#FBF6E9",
          border: "#EBD8A5"
        },
        // Surface: Warm White #FFFDF5
        warmwhite: {
          bg: "#F7F5EC",
          surface: "#FFFDF5",
          elevated: "#F2EFE4",
          border: "#E5DFD1"
        },
        // Text / Contrast: Deep Charcoal #252B27
        charcoal: {
          primary: "#252B27",
          secondary: "#434E47",
          muted: "#6B7870",
          border: "#D1D8D4"
        },
        // Safety Fallback Aliases (Removed all blue; redirects legacy classes to Emerald & Warm White)
        ivory: {
          bg: "#F7F5EC",
          surface: "#FFFDF5",
          variant: "#F2EFE4"
        }
      }
    },
  },
  plugins: [],
};
