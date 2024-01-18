# Solace Event Portal to Mule Flow Generator

## Design

### Process Description
![AsyncApi to MuleFlow Generator Process](images/Solace-EP-to-Mule-Process.jpg)

The MuleFlow Generator uses a two-step translation process:
1. **Convert the source format into an internal format.**
    - This is a direct mapping of elements into the internal, *intermediate* format. Little knowledge of the final format is required.
2. **Convert the intermediate format into the Mule Flow XML.**
    - In this step, most of the business logic is applied. Logic such as flow names, references, etc. are created.

### Data Models

* AsyncApiAccessor - Uses GSON to parse AsyncApi input document. AsyncApiAccessor provides convenience methods for extracting data elements.
* MapMuleDoc - A simple internal format to facilitate mapping from sources and to insulate source mapping from the business logic required to produce Mule Flow documents
* MuleDoc - A model created for the XML representation of Mule Flows. Unlike the actual mule flow docs used by studio (which uses jaxb), the mapper uses Faster Jackson parsers to serialize the XML data.

## Build

`mvn clean install`

## Implement

The mapper procedure can be called statically using one of four methods in `com.solace.ep.muleflow.MuleFlowGenerator` class. Each of the four methods map AsyncApi to Mule Flow XML the same way. The distinctions are the inputs (AsyncApi File Path or String) and outputs (XML to File or return as String)

|**AsyncAPI &darr;**|**Return XML as String**|**Output XML to File**|
|---|---|---|
|**Input String**|`getMuleDocXmlFromAsyncApiString`|`writeMuleXmlFileFromAsyncApiString`|
|**Input File Path**|`getMuleDocXmlFromAsyncApiFile`|`writeMuleXmlFileFromAsyncApiFile`|

