<template>
    <div class="h-full w-full home_cover rounded-2xl flex p-14 flex">
        <div class="left mt-auto w-1/2">
            <div class="text-gray-500">Classroom v1.0.0</div>
            <div class="font-bold text-5xl mt-2 leading-[55px] font-source-code-pro">Bridging the gap<br />between teachers and<br />students</div>
            <div class="flex mt-8 gap-5">
                <button class="flex items-center gap-3 bg-white/80 text-nord0 rounded-lg p-3 px-8 justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-download-cloud"><polyline points="8 17 12 21 16 17"></polyline><line x1="12" y1="12" x2="12" y2="21"></line><path d="M20.88 18.09A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.29"></path></svg>
                    Download APK
                </button>
                <button class="flex items-center gap-3 bg-white/80 text-nord0 rounded-lg p-3 px-8 justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" width="24" height="24"><path d="M420.55,301.93a24,24,0,1,1,24-24,24,24,0,0,1-24,24m-265.1,0a24,24,0,1,1,24-24,24,24,0,0,1-24,24m273.7-144.48,47.94-83a10,10,0,1,0-17.27-10h0l-48.54,84.07a301.25,301.25,0,0,0-246.56,0L116.18,64.45a10,10,0,1,0-17.27,10h0l47.94,83C64.53,202.22,8.24,285.55,0,384H576c-8.24-98.45-64.54-181.78-146.85-226.55"/></svg>
                    Get it from F-Droid
                </button>
            </div>
        </div>
        <div class="right">
            <div class="mobile -translate-y-10 translate-x-10 w-[400px] rotate-[25deg] flex flex-col bg-white rounded-3xl h-[700px]">
                <div class="status_bar flex items-center justify-center p-4 px-6 text-gray-400">
                    <div class="time text-sm font-bold">9:41</div>
                    <div class="items flex ml-auto items-center gap-1 justify-center">
                    </div>
                </div>
                <div class="mx-6">
                    <div class="title font-extrabold text-xl text-gray-800">The Great Gatsby</div>
                    <div class="author flex items-center mt-2 justify-center w-fit gap-3">
                        <img src="https://avatars.githubusercontent.com/u/115910279?v=4" alt="Author pfp" class="rounded-full h-7 w-7">
                        <div class="font-bold text-gray-600">kyeboard</div>
                    </div>
                    <hr class="my-3 border-gray-300" />
                    <div class="description text-gray-600 text-[14px]">Read the novel "The Great Gatsby" by F. Scott Fitzgerald and identify at least three significant symbols used in the story. Write a well-structured essay analyzing the symbolism and its significance in the overall narrative. Focus on the meaning of each symbol, its contribution to themes and character development, and use specific examples from the novel to support your analysis.</div>
                </div>
                <div class="sumbission_item bg-gray-200/40 p-5 rounded-t-2xl flex flex-col mt-auto">
                    <div class="title">Your work</div>
                    <button class="w-full bg-gray-200 p-2 rounded-lg mt-4">Add files</button>
                </div>
            </div>
        </div>
    </div>
</template>
