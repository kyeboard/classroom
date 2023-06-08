import { createWriteStream, writeFile } from "fs"
import https from "https"
import { Client, InputFile, Storage } from "node-appwrite"

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
    APPWRITE_FUNCTION_EVENT_DATA: string
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

    const data: EventData = JSON.parse(req.variables.APPWRITE_FUNCTION_EVENT_DATA)

    const storage = new Storage(client)
    const avatars = ["648148de33045aacfb5b", "648148e93d358fb1dbd9", "648148a8162d4b833e4d", "648148b31e4ba73c1fb6"]

    const selected = Math.floor(Math.random() * 4)
    const buffer = await storage.getFileDownload("64814877e40fa803a48b", avatars[selected])

    writeFile("profile.png", buffer, () => {})

    await storage.createFile("646ef17593d213adfcf2", data.$id, InputFile.fromPath("profile.png", "profile.png"))
}

export default upload_user_profile
