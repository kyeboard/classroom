const sdk = require("node-appwrite");
const fetch = require("node-fetch")

module.exports = async function (req, res) {
    const client = new sdk.Client();
    const bucket = new sdk.Storage(client);

    if (
        !req.variables['APPWRITE_FUNCTION_ENDPOINT'] ||
        !req.variables['APPWRITE_FUNCTION_API_KEY']
    ) {
        console.warn("Environment variables are not set. Function cannot use Appwrite SDK.");
    }

    const users = new sdk.Users(client);
    const payload = JSON.parse(req.variables["APPWRITE_FUNCTION_EVENT_DATA"]);

    client
        .setEndpoint(req.variables['APPWRITE_FUNCTION_ENDPOINT'])
        .setProject(req.variables['APPWRITE_FUNCTION_PROJECT_ID'])
        .setKey(req.variables['APPWRITE_FUNCTION_API_KEY'])
        .setSelfSigned(true);

    try {
        const user = await users.get(payload["userId"]);
        const google_user = await (await fetch(`https://www.googleapis.com/oauth2/v3/userinfo?access_token=${payload["providerAccessToken"]}`)).json()
        const image_data = await (await fetch(google_user["picture"])).text();
        await bucket.createFile("646ef17593d213adfcf2", user["name"], sdk.InputFile.fromPlainText(image_data, "profile.jpg"))
    } catch(e) {
        res.json({ error: e })
    }
};
