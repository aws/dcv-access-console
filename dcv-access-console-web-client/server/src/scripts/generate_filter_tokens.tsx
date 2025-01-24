const fs = require("fs") // Need to use require instead of import when writing scripts outside the main application
const YAML = require('yaml')

// This script expects a filepath to a .yaml file, a TypeScript filepath to write to, and the name of a type of request.
// The type of request should be present in components/schemas. The RequestData should contain definitions of filter
// tokens, and each filter token should contain a Value and an Operator.
// The method will output a JSON object containing a simplified representation of the filter tokens. The JSON object
// is essentially a list of each filter token, with the label being the Filter Token name, Value being the type, and
// Operators being the list of acceptable operators (>, =, !=, etc.). Value can also be a list of acceptable strings,
// such as CREATED, DELETED, etc.

const SPECIAL_OPERATOR_MAP: Record<string, string> = {
    "CONTAINS": ":",
    "NOT_CONTAINS": "!:"
}
function generateFilterTokenJson(model_filename: string, request: string) {
    const file = fs.readFileSync(model_filename, 'utf8')
    const model = YAML.parse(file)
    const requestProperties = model.components.schemas[request].properties

    let jsonObj: Record<string, object> = {}

    for (let requestPropertyKey in requestProperties) {
        let property = requestProperties[requestPropertyKey]
        let item_ref = property.items?.["$ref"]

        if (!item_ref) {
            console.log(requestPropertyKey + " isn't a search property. Skipping...")
            continue;
        }

        let filter_token = getRef(item_ref, model)?.properties

        if (!filter_token) {
            console.log("Unable to retrieve ref for " + requestPropertyKey + ". Skipping...")
            continue
        }

        let type = getTypeOfToken(filter_token, model)

        let operator_value_map: Record<string, string | Array<string> | object | null> = {}
        let operator_enum = filter_token.Operator?.enum;
        if (operator_enum) {
            operator_value_map["Value"] = type
            operator_value_map["Operators"] = replaceSpecialOperators(operator_enum)

        } else {
            // If there is no operator, it probably expects an object. Currently, this is only the tags, but may be other
            // search properties in the future.
            let dataObject: Record<string, string> = {}
            for (let filterTokenKey in filter_token) {
                dataObject[filterTokenKey] = filter_token[filterTokenKey].type
            }
            operator_value_map["Value"] = dataObject
            // Set operators to null to indicate that this search property does not take an operator
            operator_value_map["Operators"] = null
        }
        jsonObj[requestPropertyKey] = operator_value_map
    }
    return jsonObj
}

function replaceSpecialOperators(operators: Array<string>) {
    let replacedOperators: Array<string> = []
    operators.forEach(operator => {
        if (operator in SPECIAL_OPERATOR_MAP) {
            replacedOperators.push(SPECIAL_OPERATOR_MAP[operator])
        } else {
            replacedOperators.push(operator)
        }
    });
    return replacedOperators
}

function getTypeOfToken(filter_token: any, model: any) {
    let type: string;
    if (!filter_token.Value.type) {
        // This token doesn't have a primitive type, it is instead a set of possible values
        let filter_token_type_ref = filter_token.Value?.["$ref"]
        let filter_token_type = getRef(filter_token_type_ref, model)
        type = filter_token_type.enum
    } else if (filter_token.Value.format) {
        // The token is a primitive type, but needs to be formatted a certain way. Currently just date-time
        type = filter_token.Value.format
    } else {
        // The token has a primitive type
        type = filter_token.Value.type
    }
    return type
}

function writeJsonToTypescriptFile(filename: string, jsonObject: object) {
    let json = JSON.stringify(jsonObject, null, 2)
    fs.writeFileSync(filename, "export default\n", 'utf8');
    fs.appendFileSync(filename, json, 'utf8');
}

// Follow a reference in the template.
function getRef(ref: string, model: any) {
    let [file, primary, secondary, tertiary] = ref.split("/")
    if (file === "#") {
        // The ref points to the same file
        return model[primary][secondary][tertiary]
    } else try {
        // The ref points to another file
        let other_model_file = fs.readFileSync("model/" + file.substring(0, file.length - 1), 'utf8')
        let other_model = YAML.parse(other_model_file)
        return other_model[primary][secondary][tertiary]
    } catch (error) {
        console.error("Unable to parse file " + file + ". Error: " + error)
    }
}

// Execution starts here
let filepath_in = process.argv[2]
let filepath_out = process.argv[3]
let request_name = process.argv[4]
console.log("Generating search token map for model at " + filepath_in)
let jsonObject = generateFilterTokenJson(filepath_in, request_name)
console.log("Generated json: ", jsonObject)
writeJsonToTypescriptFile(filepath_out, jsonObject)
