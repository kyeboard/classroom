// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    modules: [
        "@nuxtjs/tailwindcss"
    ],
    tailwindcss: {
        configPath: "./tailwind.config.js",
        cssPath: "./assets/sass/styles.sass"
    }
})
