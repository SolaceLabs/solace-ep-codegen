/**
 * composeTopic.groovy
 * ------------------- 
 * This script will build a topic string with variable elements by extracting
 *  values from a JSON message body OR by replacing variables with explicit values
 *  - In general, this script should not require editing
 *  - This script relies on existing message properties:
 *    - composedTopic -- Topic pattern with variables enclosed in braces: {var}
 *    - __topicVarsJsonPath__ -- Map of variable names -> JSON path of value
 *    - __topicVarsSetValue__ -- Map of variable names -> explicit values
**/
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.LinkedHashMap;
import groovy.json.JsonSlurper;

def Message composeTopic(Message message) {
    final COMPOSED_TOPIC_PROPERTY = 'composedTopic';
    final TV_JSON_PATH_PROPERTY = '__topicVarsJsonPath__';
    final TV_SET_VALUE_PROPERTY = '__topicVarsSetValue__';

    def properties = message.getProperties();

    try {
        def composedTopicPattern = properties.get(COMPOSED_TOPIC_PROPERTY);
        
        // No topic pattern, nothing to do
        if (!composedTopicPattern || composedTopicPattern.isEmpty()) {
            // TODO - Error condition?
            return message;
        }
        // No variables in topic pattern, nothing to do
        if (!composedTopicPattern.contains('{')) {
            return message;
        }

        def tvJsonPath = properties.get(TV_JSON_PATH_PROPERTY)
        def tvSetValue = properties.get(TV_SET_VALUE_PROPERTY)

        // Special properties with variable content not present, nothing to do
        if (!tvJsonPath && !tvSetValue) {
            return message;
        } else if (tvJsonPath.size() == 0 && tvSetValue.size() == 0) {
            return message;
        }

        def composedTopicString = composedTopicPattern

        // Set topic variables from payload
        if (tvJsonPath && tvJsonPath.size() > 0) {
            final js = new groovy.json.JsonSlurper()
            final request = js.parseText(message.getBody(java.lang.String) as String)
            tvJsonPath.each { entry ->
                def varName = "{$entry.key}"
                def varValue = groovy.util.Eval.x(request, 'x.' + entry.value)
                if (varName && varValue) {
                    composedTopicString = composedTopicString.replace(varName, varValue)
                }
            }
        }

        // Set topic variables directly
        if (tvSetValue) {
            tvSetValue.each{ entry ->
                def varName = "{$entry.key}"
                def varValue = entry.value
                if (varName && varValue) {
                    composedTopicString = composedTopicString.replace(varName, varValue)
                }
            }
        }

        // Set the composed topic to expected property
        message.setProperty(COMPOSED_TOPIC_PROPERTY, composedTopicString);

    } catch (Exception ex) {
        message.setProperty('composedTopic_error', ex.getMessage());
    }
    return message;
}