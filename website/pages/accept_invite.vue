<template>
    <div class="px-20 flex flex-col h-screen w-screen">
        <NavBar  />
        <div class="content w-full h-full flex flex-col items-center justify-center">
            <div class="text" v-if="pending">Accepting the invitation...</div>
            <div class="text" v-if="error">Error while accepting the invitiation! {{ error.message }}</div>
            <div class="text" v-if="!error">Successfully accepted the invitation! Head back to app and see the new class in your class list</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { Client, Teams } from "appwrite"

const route = useRoute()

const client = new Client()
    .setProject("fryday")
    .setEndpoint("https://cloud.appwrite.io/v1")
const teams = new Teams(client)

const { data, pending, error, refresh } = useAsyncData(
    'mountains',
    async () => {
        await teams.updateMembershipStatus(
            route.query.teamId as string,
            route.query.membershipId as string,
            route.query.userId as string,
            route.query.secret as string,
        )
    }
)


console.log(route.query)
</script>
