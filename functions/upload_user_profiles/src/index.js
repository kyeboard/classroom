const sdk = require("node-appwrite");

module.exports = async function (req, res) {
    const client = new sdk.Client();

    if (
        !req.variables['APPWRITE_FUNCTION_ENDPOINT'] ||
        !req.variables['APPWRITE_FUNCTION_API_KEY']
    ) {
        console.warn("Environment variables are not set. Function cannot use Appwrite SDK.");
    }

    console.log(req.payload)

    client
        .setEndpoint(req.variables['APPWRITE_FUNCTION_ENDPOINT'])
        .setProject(req.variables['APPWRITE_FUNCTION_PROJECT_ID'])
        .setKey(req.variables['APPWRITE_FUNCTION_API_KEY'])
        .setSelfSigned(true);

    res.json(req.payload);
};
