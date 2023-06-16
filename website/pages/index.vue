<template>
    <div class="flex w-screen h-screen p-5">
        <div class="carousel w-1/3 h-full bg-[#F8E8E3] rounded-2xl">

        </div>
        <div class="content w-2/3 pl-16 flex flex-col justify-center">
            <NavBar />
            <div class="text-[45px] mt-auto font-jakarta leading-[55px] font-extrabold mt-10">Briding the gap between<br />students and teachers.</div>
            <div class="text-gray-500 w-1/2 mt-5 leading-[28px]">Effortlessly create and manage virtual classrooms, allowing them to easily distribute assignments, share course materials, and provide timely feedback to their students.</div>
            <div class="features mt-6">
                <div class="font-jakarta font-bold">Features</div>
                <div class="items mt-2 ml-8 text-gray-600 flex flex-col gap-1 leading-[25px]">
                    <div class="item">- Fast, Open Source and Lightweight</div>
                    <div class="item">- Easily create classes and invite your students</div>
                    <div class="item">- Announce anything to all the students</div>
                    <div class="item">- Assign tasks to students and grade them</div>
                </div>
            </div>
            <a href="/download" class="flex gap-10 mt-5 mb-10">
                <button class="bg-nord0 p-3 px-12 rounded-lg text-nord4">Download</button>
            </a>
        </div>
    </div>
</template>

<script setup lang="ts">
useHead({
    title: "Briding the gap between students and teachers | Classroom"
})
</script>
