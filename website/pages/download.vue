<template>
    <div class="flex w-screen h-screen p-5">
        <div class="carousel rounded-2xl ml-auto bg-[#EDF8E4] h-full w-1/3"></div>
        <div class="content w-2/3 mr-5 ml-20">
            <NavBar />
            <div class="font-jakarta text-[45px] font-extrabold mt-12 leading-[55px]">Download classroom for<br />your device.</div>
            <div class="mt-4 text-gray-500 w-1/2 leading-[28px]">Download the variant for your device. If you are unsure of your variant, download the universal one!</div>
            <div class="changelogs mt-6">
                <div class="title font-jakarta font-bold">Changelogs</div>
                <div class="items mt-2 ml-8 text-gray-600 flex flex-col gap-1">
                    <div class="item">- Initial build</div>
                    <div class="item">- Improve performance and reduce apk size</div>
                    <div class="item">- Remove bugs lingering around</div>
                </div>
            </div>
            <div class="title mt-5 font-jakarta font-bold">Download</div>
            <div class="variants mt-2 flex gap-5">
                <div class="variant bg-nord6/60 w-44 p-8 flex flex-col items-center rounded-xl text-nord0">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" fill="#2E3440" width="36" height="36"><path d="M420.55,301.93a24,24,0,1,1,24-24,24,24,0,0,1-24,24m-265.1,0a24,24,0,1,1,24-24,24,24,0,0,1-24,24m273.7-144.48,47.94-83a10,10,0,1,0-17.27-10h0l-48.54,84.07a301.25,301.25,0,0,0-246.56,0L116.18,64.45a10,10,0,1,0-17.27,10h0l47.94,83C64.53,202.22,8.24,285.55,0,384H576c-8.24-98.45-64.54-181.78-146.85-226.55"/></svg>
                    <div class="arch font-jakarta mt-0.5">arm64-v8a</div>
                </div>
                <div class="variant bg-nord6/60 w-44 p-8 flex flex-col items-center rounded-xl text-nord0">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" fill="#2E3440" width="36" height="36"><path d="M420.55,301.93a24,24,0,1,1,24-24,24,24,0,0,1-24,24m-265.1,0a24,24,0,1,1,24-24,24,24,0,0,1-24,24m273.7-144.48,47.94-83a10,10,0,1,0-17.27-10h0l-48.54,84.07a301.25,301.25,0,0,0-246.56,0L116.18,64.45a10,10,0,1,0-17.27,10h0l47.94,83C64.53,202.22,8.24,285.55,0,384H576c-8.24-98.45-64.54-181.78-146.85-226.55"/></svg>
                    <div class="arch font-jakarta mt-0.5">armeabi-v7a</div>
                </div>
                <div class="variant bg-nord6/60 w-44 p-8 flex flex-col items-center rounded-xl text-nord0">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" fill="#2E3440" width="36" height="36"><path d="M420.55,301.93a24,24,0,1,1,24-24,24,24,0,0,1-24,24m-265.1,0a24,24,0,1,1,24-24,24,24,0,0,1-24,24m273.7-144.48,47.94-83a10,10,0,1,0-17.27-10h0l-48.54,84.07a301.25,301.25,0,0,0-246.56,0L116.18,64.45a10,10,0,1,0-17.27,10h0l47.94,83C64.53,202.22,8.24,285.55,0,384H576c-8.24-98.45-64.54-181.78-146.85-226.55"/></svg>
                    <div class="arch font-jakarta mt-0.5">Universal</div>
                </div>
            </div>
            <div class="support">
                <div class="title mt-5 font-jakarta font-bold">Support</div>
                <div class="items mt-2 ml-8 text-gray-600 flex flex-col gap-1">
                    <div class="item">- Join my discord channel - </div>
                    <div class="item">- Came across a bug? Report it here - <a class="text-nord10" href="https://www.github.com/kyeboard/classroom/issues/new">https://www.github.com/kyeboard/classroom/issues/new</a></div>
                </div>
            </div>
            <div class="notes">
                <div class="title mt-5 font-jakarta font-bold">Notes</div>
                <div class="items mt-2 ml-8 text-gray-600 flex flex-col gap-1">
                    <div class="item">- Loved my project? Sponsor me! - <a class="text-nord10" href="https://www.github.com/sponsors/kyeboard">https://www.github.com/sponsors/kyeboard</a></div>
                </div>
            </div>
        </div>
   </div>
</template>
