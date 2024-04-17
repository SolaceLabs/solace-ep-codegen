package com.solace.ep.muleflow.mapper.sap.iflow;

public class SapIflowUtils {
    

    public static final String
                DEFINITIONS_PREFIX              = "Defintions_",
                COLLAB_ID_PREFIX                = "Collaboration_",
                COLLAB_NAME_DEFAULT             = "";

    public static final String
                PARTICIPANT_ID_PREFIX           = "Participant_",
                PARTICIPANT_NAME_SEND           = "EventMeshSender",
                PARTICIPANT_NAME_RECV           = "EventMeshReceiver",
                PARTICIPANT_NAME_SRC            = "SourceSystem",
                PARTICIPANT_NAME_DEST           = "DestinationSystemReceiver",
                PARTICIPANT_IFL_SEND            = "EndpointSender",
                PARTICIPANT_IFL_RECV            = "EndpointReceiver",
                PARTICIPANT_IFL_INT             = "IntegrationProcess",
                MSGFLOW_ID_PREFIX               = "MessageFlow_";

    public static final String
                PROCESS_ID_PREFIX               = "Process_",
                PROCESS_INB_NAME_TEMPLATE       = "Process inbound event from %s",
                PROCESS_INB_BL_NAME_TEMPLATE    = "Business Logic for %s event",
                PROCESS_OUT_GEN_NAME_TEMPLATE   = "Generate %s event",
                PROCESS_OUT_SEND_NAME_TEMPLATE  = "Process source data for %s event, send to Event Mesh",
                ACT_START_EVENT_PREFIX          = "StartEvent_",
                ACT_END_EVENT_PREFIX            = "EndEvent_",
                ACT_CALL_PREFIX                 = "CallActivity_",
                ACT_INB_END_NAME                = "Send to Destination Receiver",
                ACT_BL_TEMPLATE_NAME            = PROCESS_INB_BL_NAME_TEMPLATE,
                ACT_VALIDATE_SCHEMA_TEMPLATE    = "Validate %s schema",
                ACT_BL_START_NAME_TEMPLATE      = "From inbound %s process",
                ACT_BL_END_NAME_TEMPLATE        = "Return to %s process",
                ACT_BL_STUBMAP_TEMPLATE         = "Stub map for transforming source data to %s %s",

                ACT_SEND_START_NAME             = "This is where source connectivity and any enrichment would occur",
                ACT_SEND_END_NAME_TEMPLATE      = "Send %s to Event Mesh",
                ACT_SEND_GENERATE_NAME_TEMPLATE = "Generate %s event",
                ACT_SEND_STUBMAP_TEMPLATE       = ACT_BL_STUBMAP_TEMPLATE,

                ACT_GEN_START_NAME_TEMPLATE     = "From %s process",
                ACT_GEN_END_NAME_TEMPLATE       = "Return to %s process",
                ACT_GEN_EXTRACT_TOPIC_PREFIX    = "Extract ",
                ACT_GEN_COMPOSED_TOPIC_NAME     = "Generate composedTopic using runtime variables",
                ACT_GEN_VALIDATE_SCHEMA_TEMPLATE   = ACT_VALIDATE_SCHEMA_TEMPLATE;


    //
    public static final String
                SEQ_FLOW_PREFIX                 = "SequenceFlow_";

    //

}
