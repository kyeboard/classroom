<template>
    <div class="px-20 flex flex-col items-center">
        <NavBar class="py-[2rem] w-full px-20 fixed top-0 left-0 pr-20 bg-[#ffffff]" />
        <div class="content flex w-1/2 pb-20 items-center mt-40 flex-col">
            <img src="/cover.png" class="rounded-2xl" />
            <div class="mt-5 leading-[35px]">
                Classroom is a free, light-weight, open-source Android app that bridges the gap between students and teachers. It provides a bunch of features for teachers and students for a fast, smooth experience.
                <br /><br />
                For teachers, Classroom offers a comprehensive suite of tools to streamline their instructional processes. Effortlessly create and manage virtual classrooms, allowing them to easily distribute assignments, share course materials, and provide timely feedback to their students. With the ability to set deadlines and track progress, teachers can closely monitor their students' development, enabling personalized support and targeted interventions when needed.
            </div>
            <hr class="my-8 border border-gray-300 w-full" />
            <img src="/features_cover.png" class="rounded-2xl" />
            <div class="mt-5 w-full">
                Classroom provides a bunch of features for students and teachers to enhance their experience!

                <div class="features ml-9 leading-[35px] mt-2">
                    <div class="feature">- A lightweight, smooth and fast classroom experience</div>
                    <div class="feature">- Easily create classroom workspaces and invite students</div>
                    <div class="feature">- Easily create announcements and notify every student</div>
                    <div class="feature">- Create assignments and grade students</div>
                    <div class="feature">- Add private comments to each student's submission</div>
                </div>
            </div>
            <hr class="my-8 border border-gray-300 w-full" />
            <img src="/installation_cover.png" class="rounded-2xl" />
            <div class="mt-5 w-full leading-[35px]">
                - Install from official website - <a href="https://classroom.kyeboard.me/download" class="text-nord10 font-bold">https://classroom.kyeboard.me/download</a>
            </div>
            <hr class="my-8 border border-gray-300 w-full" />
            <img src="/notes.png" class="rounded-2xl" />
            <div class="mt-5 w-full">
                A bit of notes and attributions

                <div class="notes ml-9 leading-[35px] mt-2">
                    <div class="note">- Built for Appwrite's 2023 Hackathon</div>
                    <div class="note">- Credits to Project Exilir for the covers (I copied from them)</div>
                    <div class="note">- This project is built with Appwrite and Kotlin for Android</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
useHead({
    title: "About us - klassroom"
})
</script>
