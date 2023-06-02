// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    pages: true,
    modules: [
        "@nuxtjs/tailwindcss"
    ],
    tailwindcss: {
        configPath: "./tailwind.config.js"
    }
})
