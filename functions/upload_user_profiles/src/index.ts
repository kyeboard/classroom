import { Client, InputFile, Storage } from "node-appwrite"
import fs from "node:fs"

interface Request {
    variables: Variables
}

interface EventData {
    $id: string
}

interface Variables {
    APPWRITE_KEY: string
    APPWRITE_ENDPOINT: string
    APPWRITE_PROJECTID: string
    APPWRITE_FUNCTION_EVENT_DATA: EventData
}

interface Response {
    json: (obj: any) => void
}

const upload_user_profile = async (req: Request, res: Response) => {
    const client = new Client()

    client
        .setKey(req.variables.APPWRITE_KEY)
        .setProject(req.variables.APPWRITE_PROJECTID)
        .setEndpoint(req.variables.APPWRITE_ENDPOINT)

    const storage = new Storage(client)
    const assigned_avatar = Math.floor(Math.random() * (5 - 1) + 1);

    fs.readdir(".", (err, files) => {
        res.json({ err, files })
    })

    await storage.createFile("646ef17593d213adfcf2", req.variables.APPWRITE_FUNCTION_EVENT_DATA.$id, InputFile.fromPath(`build/avatar_4.png`, "avatar.png"))
}

export default upload_user_profile
