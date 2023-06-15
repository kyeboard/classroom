import { Client, Storage, InputFile, Permission, Role } from "node-appwrite"
import fs from "fs"

interface Request {
    variables: Variables
}

interface EventData {
    userId: string
}

interface Variables {
    APPWRITE_FUNCTION_PROJECT_ID: string,
    APPWRITE_FUNCTION_PROJECT_ENDPOINT: string,
    APPWRITE_FUNCTION_PROJECT_KEY: string,
    APPWRITE_FUNCTION_EVENT_DATA: string
}

interface Response {
    json: (obj: any) => void
}

// All the avatars that current we have
const avatars_index = ["avatar_1", "avatar_2", "avatar_3", "avatar_4"]

export default async function(req: Request, res: Response) {
    try {
        // Initiate client
        const client = new Client()

        // Configure
        client
            .setProject(req.variables.APPWRITE_FUNCTION_PROJECT_ID)
            .setEndpoint(req.variables.APPWRITE_FUNCTION_PROJECT_ENDPOINT)
            .setKey(req.variables.APPWRITE_FUNCTION_PROJECT_KEY)

        // Setup services
        const storage = new Storage(client)

        // Get the event payload (data)
        const data: EventData = JSON.parse(req.variables.APPWRITE_FUNCTION_EVENT_DATA)

        // Select a random avatar for the user
        const selected_index = Math.floor(Math.random() * 4)

        // Get the avatar URL
        const raw = await storage.getFileDownload("648a6e529592517be880", avatars_index[selected_index])

        // Write to the file
        fs.writeFile("profile.png", raw, () => {

        })

        // Upload it back to the user profiles bucket
        await storage.createFile(
            "userpfps",
            data.userId,
            InputFile.fromPath("profile.png", "profile.png"),
            [Permission.read(Role.any())]
        )

        // Delete the file
        fs.unlinkSync("profile.png")

        // Send a response
        res.json({ error: false, message: "Successfully uploaded the user profile <3" })
    }
    catch(err: any) {
        // Send the error back
        res.json({ error: true, message: "Error while uploading the profile to the cloud: " + err.message })
    }
}
