import { Client, Storage, InputFile } from "node-appwrite"
import fs from "fs"

interface Request {
    variables: Variables
}

interface EventData {
    $id: string
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
const avatars_index = ["648148a8162d4b833e4d", "648148b31e4ba73c1fb6", "648148de33045aacfb5b", "648148e93d358fb1dbd9"]

module.exports = async function(req: Request, res: Response) {
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
        const raw = await storage.getFileDownload("64814877e40fa803a48b", avatars_index[selected_index])

        // Write to the file
        fs.writeFile("profile.png", raw, () => {

        })

        // Upload it back to the user profiles bucket
        await storage.createFile("646ef17593d213adfcf2", data.$id, InputFile.fromPath("profile.png", "profile.png"))

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
