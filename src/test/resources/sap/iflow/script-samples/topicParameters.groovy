/**
 * topicParameters.groovy
 * ----------------------
 * EDIT this script to define sources for published topic variables
 * - One function is generated per published event
 * - Define entry for variable source as JSON Path (payload) or Set Value (explicit)
 * - The JSON Path will be evaluated first, the Set Value can be used as a default
 *   if the field specified in JSON Path is not found in the payload
 **/
import com.sap.gateway.ip.core.customdev.util.Message;

TV_JSON_PATH_PROPERTY = "__topicVarsJsonPath__";
TV_SET_VALUE_PROPERTY = "__topicVarsSetValue__";

/**
 * Define Topic Variables for Event:
 *     >>$$__EVENT_NAME__$$<<
 *
 * Topic Address Pattern:
 *     >>$$__TOPIC_ADDRESS_PATTERN__$$<<
 *
 * Topic Variables:
>>$$__TOPIC_VARS_LIST__$$<<
 * - orderId
 * - regionId
 * - customerId
 **/
def Message defineTopicParams_>>$$__FX_INSTANCE__$$<<(Message message) {

    def topicVarsJsonPath = [:]   // Map of topicVariables -> JSON path locations
    def topicVarsSetValue = [:]   // Map of topicVariables -> Explicit values

    // Uncomment property value to set JSON Path location in payload
    // DO NOT use $. prefix for JSON Path specification
>>$$__TOPIC_VARS_JSON_PATH__$$<<
//    topicVarsJsonPath.regionId     = "SET.JSON.PATH"
    topicVarsJsonPath.orderId      = "order_id"
//    topicVarsJsonPath.customerId   = "SET.JSON.PATH"

    // Uncomment property entry below to set a topic value directly
>>$$__TOPIC_VARS_SET_VALUE__$$<<
    topicVarsSetValue.regionId      = "PENNSYLVANIA"
//    topicVarsSetValue.orderId        = "VALUE1"
//    topicVarsSetValue.customerId     = "VALUE2"

    message.setProperty(TV_JSON_PATH_PROPERTY, topicVarsJsonPath)
    message.setProperty(TV_SET_VALUE_PROPERTY, topicVarsSetValue)

    return message;
}
