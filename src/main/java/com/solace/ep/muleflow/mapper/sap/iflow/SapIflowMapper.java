/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.solace.ep.muleflow.mapper.sap.iflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.di.BPMNDiagram;
import org.omg.spec.bpmn._20100524.di.BPMNEdge;
import org.omg.spec.bpmn._20100524.di.BPMNPlane;
import org.omg.spec.bpmn._20100524.di.BPMNShape;
import org.omg.spec.bpmn._20100524.model.ObjectFactory;
import org.omg.spec.bpmn._20100524.model.TBaseElement;
import org.omg.spec.bpmn._20100524.model.TCallActivity;
import org.omg.spec.bpmn._20100524.model.TCollaboration;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import org.omg.spec.bpmn._20100524.model.TEndEvent;
import org.omg.spec.bpmn._20100524.model.TFlowElement;
import org.omg.spec.bpmn._20100524.model.TMessageFlow;
import org.omg.spec.bpmn._20100524.model.TParticipant;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
import org.omg.spec.bpmn._20100524.model.TStartEvent;
import org.omg.spec.dd._20100524.dc.Bounds;
import org.omg.spec.dd._20100524.dc.Point;

import com.solace.ep.muleflow.mapper.model.MapFlow;
import com.solace.ep.muleflow.mapper.model.MapMuleDoc;
import com.solace.ep.muleflow.mapper.model.MapSubFlowEgress;
import com.solace.ep.muleflow.mapper.sap.iflow.model.bpmn_ifl_ext.TSapIflowProperty;
import com.solace.ep.muleflow.mapper.sap.iflow.model.config.SapIflowExtensionConfig;
import com.solace.ep.muleflow.mapper.sap.iflow.model.config.SapIflowExtensionConfig.ProcessExt;

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SapIflowMapper {

    private static final String
            EXT_ELEMENTS_CONFIG_FILE = "src/main/resources/sap/iflow/extension-elements.yaml";

    private static final double
            BPMN_START_BOUNDARY_X = 40d,
            BPMN_START_BOUNDARY_Y = 40d,
            BPMN_STATIC_PARTICIPANT_W = 100d,
            BPMN_STATIC_PARTICIPANT_H = 140d,
            BPMN_PART_PROC_H = 200d,
            BPMN_PROC_SEP_H = 60d,
            BPMN_START_END_EVENT_H = 32d,
            BPMN_START_END_EVENT_W = 32d,
            BPMN_FLOW_ELT_SEP_X = 60d,
            BPMN_CALL_ACT_H = 60,
            BPMN_CALL_ACT_W = 150,
            BPMN_MESH_PARTICIPANT_PROCESS_SEP_X = 100d,
            BPMN_MESH_PARTICIPANT_PROCESS_SEP_Y = 60d;

    private double
            processBoundaryX = BPMN_START_BOUNDARY_X,
            processBoundaryY = BPMN_START_BOUNDARY_Y,
            processBoundaryMaxX = BPMN_START_BOUNDARY_X;

    private long objectIncrementer = 1;

    private final ObjectFactory bpmnFactory = new ObjectFactory();

    private final org.omg.spec.bpmn._20100524.di.ObjectFactory bpmnDiFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

    private final org.omg.spec.dd._20100524.dc.ObjectFactory dcFactory = new org.omg.spec.dd._20100524.dc.ObjectFactory();

    private final com.solace.ep.muleflow.mapper.sap.iflow.model.bpmn_ifl_ext.ObjectFactory propFactory = new com.solace.ep.muleflow.mapper.sap.iflow.model.bpmn_ifl_ext.ObjectFactory();

    private final List<TParticipant> participants = new ArrayList<>();

    private final List<TProcess> processes = new ArrayList<>();

    private final List<TMessageFlow> messageFlows = new ArrayList<>();

    private final Map<String, BPMNShape> shapes = new LinkedHashMap<>();

    private final List<BPMNEdge> edges = new ArrayList<>();

    private TDefinitions rootElement = null;

    private MapMuleDoc inputSource = null;

    private SapIflowExtensionConfig extConfigs;

    /**
     * Constructor requires object of type MapMuleDoc
     * @param input
     * @throws Exception
     */
    public SapIflowMapper( final MapMuleDoc input ) throws Exception {
        extConfigs = SapIflowUtils.parseExtensionConfig( EXT_ELEMENTS_CONFIG_FILE );
        if ( input == null ) {
            throw new Exception( String.format( "Input type [%s] cannot be null", MapMuleDoc.class.getName() ) );
        }
        this.inputSource = input;
    }
    
    public JAXBElement<TDefinitions> createSapIflowAsJAXBElement() {
        return bpmnFactory.createDefinitions( createSapIflow() );
    }

    public TDefinitions createSapIflow() {

        if ( this.rootElement != null ) {
            log.info( "Returning previously constructed SAP BPMN2 IFlow object" );
            return this.rootElement;
        }

        BPMNShape   bpmnDestinationSystemReceiver = null, bpmnEventMeshReceiver = null;
        String startParticipantId = "", endParticipantId = "";

        // Create input mesh participants (static)
        if ( inputSource.getMapFlows().size() > 0 ) {
            log.info("There are {} Consumer(s) found in input", inputSource.getMapFlows().size());
            TParticipant eventMeshSender = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SEND );
            addExtensionProperties(eventMeshSender, extConfigs.getParticipant().getEventMeshSender());

            TParticipant sourceSystem = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SRC );
            addExtensionProperties(sourceSystem, extConfigs.getParticipant().getSourceSystem());
            
            startParticipantId = eventMeshSender.getId();

            participants.add(eventMeshSender);
            participants.add(sourceSystem);

            addBpmnShapeFromStaticParticipant(
                eventMeshSender, 
                BPMN_START_BOUNDARY_X, 
                BPMN_START_BOUNDARY_Y
            );
            addBpmnShapeFromStaticParticipant(
                sourceSystem, 
                BPMN_START_BOUNDARY_X, 
                ( 
                    BPMN_START_BOUNDARY_Y + 
                    BPMN_STATIC_PARTICIPANT_H + 
                    BPMN_MESH_PARTICIPANT_PROCESS_SEP_Y
                )
            );
            processBoundaryX += BPMN_STATIC_PARTICIPANT_W + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X;
        } else {
            log.info( "No Consumers found in input" );
        }

        // Create output mesh participants (static)
        if ( inputSource.getMapEgressSubFlows().size() > 0 ) {
            log.info( "There are {} Publication Flows found in the input", inputSource.getMapEgressSubFlows().size() );
            TParticipant eventMeshReceiver = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_RECV );
            addExtensionProperties(eventMeshReceiver, extConfigs.getParticipant().getEventMeshReceiver());

            TParticipant destinationSystemReceiver = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_DEST );
            addExtensionProperties(destinationSystemReceiver, extConfigs.getParticipant().getDestinationSystemReceiver());
            
            endParticipantId = eventMeshReceiver.getId();

            participants.add(eventMeshReceiver);
            participants.add(destinationSystemReceiver);

            bpmnEventMeshReceiver = addBpmnShapeFromStaticParticipant(
                eventMeshReceiver, 
                BPMN_START_BOUNDARY_X, 
                BPMN_START_BOUNDARY_Y
            );
            bpmnDestinationSystemReceiver = addBpmnShapeFromStaticParticipant(
                destinationSystemReceiver, 
                BPMN_START_BOUNDARY_X, 
                ( 
                    BPMN_START_BOUNDARY_Y + 
                    BPMN_STATIC_PARTICIPANT_H + 
                    BPMN_MESH_PARTICIPANT_PROCESS_SEP_Y
                )
            );
            processBoundaryX += BPMN_STATIC_PARTICIPANT_W + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X;
        } else {
            log.info( "No Publication Flows found in input" );
        }

        // Create 
        for ( MapFlow ingress : inputSource.getMapFlows() ) {
            mapIngressToIflow(ingress, startParticipantId);
        }

        for ( MapSubFlowEgress egress : inputSource.getMapEgressSubFlows() ) {
            mapEgressToIflow(egress, endParticipantId);
        }

        if ( bpmnDestinationSystemReceiver != null && bpmnEventMeshReceiver != null ) { 
            updateBpmnShapePositionX(bpmnEventMeshReceiver, ( processBoundaryMaxX + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X ) );
            updateBpmnShapePositionX(bpmnDestinationSystemReceiver, ( processBoundaryMaxX + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X ) );
        }

        for ( TMessageFlow flow : messageFlows ) {
            createBpmnEdgeFromMessageFlow(flow);
        }

        TCollaboration collaboration = bpmnFactory.createTCollaboration();
        collaboration.setId( SapIflowUtils.COLLAB_ID_PREFIX + "1" );
        String collaborationName;
        if ( inputSource.getGlobalProperties() != null && 
            inputSource.getGlobalProperties().containsKey( "epApplicationVersion" ) &&
            inputSource.getGlobalProperties().get( "epApplicationVersion" ).length() > 0 &&
            inputSource.getGlobalProperties().containsKey( "epApplicationVersionTitle" ) &&
            inputSource.getGlobalProperties().get( "epApplicationVersionTitle" ).length() > 0
        ) {
            collaborationName = 
                inputSource.getGlobalProperties().get( "epApplicationVersionTitle" ) +
                "_" +
                inputSource.getGlobalProperties().get( "epApplicationVersion" );
        } else {
            collaborationName = SapIflowUtils.COLLAB_NAME_DEFAULT;
        }
        collaboration.setName( collaborationName );
        addExtensionProperties(collaboration, extConfigs.getCollaboration());
        collaboration.getParticipant().addAll(participants);
        collaboration.getMessageFlow().addAll(messageFlows);

        TDefinitions definitions = bpmnFactory.createTDefinitions();
        definitions.setId( SapIflowUtils.DEFINITIONS_PREFIX + "1" );
        definitions.getRootElement().add( bpmnFactory.createCollaboration(collaboration) );
        processes.forEach( process -> {
            definitions.getRootElement().add( bpmnFactory.createProcess( process ) );
        });

        BPMNPlane plane = bpmnDiFactory.createBPMNPlane();
        plane.setId( "BPMNPlane_1" );
        plane.setBpmnElement( new QName( collaboration.getId() ) );
        for ( Map.Entry<String, BPMNShape> shape : shapes.entrySet() ) {
            plane.getDiagramElement().add( bpmnDiFactory.createBPMNShape( shape.getValue() ) );
        }
        for( BPMNEdge edge : edges ) {
            plane.getDiagramElement().add( bpmnDiFactory.createBPMNEdge(edge) );
        }

        BPMNDiagram diagram = bpmnDiFactory.createBPMNDiagram();
        diagram.setId( "BPMNDiagram_1" );
        diagram.setName( collaborationName + " Diagram" );
        diagram.setBPMNPlane(plane);

        definitions.getBPMNDiagram().add(diagram);

        this.rootElement = definitions;
        return this.rootElement;
    }

    private void mapIngressToIflow( MapFlow ingress, String startParticipantId ) {

        String eventName = ingress.isDirectConsumer() ? ingress.getFlowDesignation() : ingress.getQueueListenerAddress();

        TProcess receiverProcess = createReceiverProcess(ingress);
        TProcess businessLogicProcess = createBusinessLogicProcess(ingress);

        // Add call from receiverProcess to businessLogicProcess
        addCallActivityBeforeEndEvent(
            receiverProcess, 
            createCallActivityToProcess( 
                String.format( SapIflowUtils.ACT_BL_TEMPLATE_NAME, eventName ),
                businessLogicProcess.getId()
            )
        );

        generateSequences( receiverProcess );
        generateSequences( businessLogicProcess );

        TParticipant inboundParticipantProc = createGenericParticipant( 
            receiverProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            receiverProcess.getName(),
            receiverProcess.getId() );

        TParticipant businessLogicParticipant = createGenericParticipant(
            businessLogicProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            businessLogicProcess.getName(),
            businessLogicProcess.getId() );
        
        TMessageFlow messageFlow = createSubscribeMessageFlow(
            ingress, 
            startParticipantId, 
            receiverProcess );
        
        processes.add( receiverProcess );
        processes.add( businessLogicProcess );
        participants.add( inboundParticipantProc );
        participants.add( businessLogicParticipant );
        messageFlows.add( messageFlow );

        createBpmnShapesForParticipantAndProcess( inboundParticipantProc, receiverProcess );
        createBpmnShapesForParticipantAndProcess( businessLogicParticipant, businessLogicProcess);
    }

    private TProcess createReceiverProcess( MapFlow ingress ) {

        final String eventSource = ingress.isDirectConsumer() ? ingress.getFlowDesignation() : ingress.getQueueListenerAddress();
        final String consumerOrQueue = ingress.isDirectConsumer() ? "Topic Consumer" : "Queue";
        final String processName = String.format( SapIflowUtils.PROCESS_INB_NAME_TEMPLATE, eventSource ) + " " + consumerOrQueue;

        final TProcess receiverProcess = createGenericProcess(
            processName,
            "Receive Event", 
            "Send to Receiver Destination" );

        //
        addEmptyMessageEventDefinitions(receiverProcess);       // Conform with samples
        addExtensionProperties(receiverProcess, extConfigs.getInboundProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(receiverProcess, extConfigs.getInboundProcess());

        final String validateSchemaName = getSchemaName(ingress.getJsonSchemaReference());
        final String validateCaName = "Validate inbound event against schema " + validateSchemaName;
        final TCallActivity validateSchemaCallActivity = 
                createValidateSchemaCallActivity(
                    validateCaName, 
                    validateSchemaName
        );
        addCallActivityBeforeEndEvent( receiverProcess, validateSchemaCallActivity );

        return receiverProcess;
    }

    private TProcess createBusinessLogicProcess( MapFlow ingress ) {

        String eventName = ingress.isDirectConsumer() ? ingress.getFlowDesignation() : ingress.getQueueListenerAddress();

        TProcess businessLogicProcess = createGenericProcess(
            String.format(
                SapIflowUtils.PROCESS_INB_BL_NAME_TEMPLATE, 
                eventName
            ),
            String.format( SapIflowUtils.ACT_BL_START_NAME_TEMPLATE, eventName ), 
            String.format( SapIflowUtils.ACT_BL_END_NAME_TEMPLATE, eventName )
        );

        //
        addExtensionProperties(businessLogicProcess, extConfigs.getCalledProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(businessLogicProcess, extConfigs.getCalledProcess());

        final String mapToSchemaName = getSchemaName(ingress.getJsonSchemaReference());
        final String stubMapCa = String.format("Stub map from %s to destination format", mapToSchemaName);
        final TCallActivity stubMapCallActivity = 
            createInboundStubMapCallActivity(
                    stubMapCa, 
                    mapToSchemaName
            );
        addCallActivityBeforeEndEvent(businessLogicProcess, stubMapCallActivity);

        return businessLogicProcess;
    }

    private TMessageFlow createSubscribeMessageFlow(
        MapFlow ingress,
        String sourceRef,
        TProcess targetProcess
    ) {

        String targetRef = "";
        for ( JAXBElement<? extends TFlowElement> jaxbElt : targetProcess.getFlowElement() ) {
            if ( jaxbElt.getValue() instanceof TStartEvent ) {
                TStartEvent startEvent = ( TStartEvent )( jaxbElt.getValue() );
                targetRef = startEvent.getId();
                break;
            }
        }

        TMessageFlow subscribeMessageFlow = 
            createGenericMessageFlow(
                "Ingress Flow from " + ( ingress.isDirectConsumer() ? "Direct Consumer: " : "Queue: " ) + ingress.getFlowDesignation(), 
                sourceRef, 
                targetRef
        );
        addExtensionProperties(subscribeMessageFlow, extConfigs.getMessageFlow().getSubscription() );

        addExtensionProperty(
            subscribeMessageFlow, 
            "ackMode", 
            ( ingress.isDirectConsumer() ? "AUTOMATIC_IMMEDIATE" : 
                    ( ( ingress.getQueueListenerAckMode() != null && ingress.getQueueListenerAckMode().length() > 0 ) ?
                        ingress.getQueueListenerAckMode() : "AUTOMATIC_ON_EXCHANGE_COMPLETE" ) ) );
        addExtensionProperty(
            subscribeMessageFlow, 
            "consumerMode", 
            ( ingress.isDirectConsumer() ? "DIRECT" : "GUARANTEED" ) );
        // addExtensionProperty( subscribeMessageFlow, "Description", "");
        // addExtensionProperty( subscribeMessageFlow, "direction", "Sender");
        addExtensionProperty( subscribeMessageFlow, "Name", ingress.getFlowDesignation() );
        addExtensionProperty(
            subscribeMessageFlow, 
            "queueName", 
            ( ingress.isDirectConsumer() ? "" : ingress.getQueueListenerAddress() ) );
        // addExtensionProperty( subscribeMessageFlow, "system", "EventMeshSender");

        // Add topic list as extension property
        StringBuilder topicBuilder = new StringBuilder();
        if ( ingress.isDirectConsumer() && ingress.getDirectListenerTopics() != null ) {
            // topicBuilder.append( "<row>" );
            for ( String topic : ingress.getDirectListenerTopics() ) {
                topicBuilder.append( "<row><cell id='listObjectValue'>" );
                topicBuilder.append( topic );
                topicBuilder.append( "</cell></row>");
            }
            // topicBuilder.append( "</row>" );
        }
        addExtensionProperty( subscribeMessageFlow, "topicSubscriptions", topicBuilder.toString());

        return subscribeMessageFlow;
    }

    private void mapEgressToIflow( MapSubFlowEgress egress, String endParticipantId ) {

        String eventName = egress.getMessageName();

        TProcess senderProcess = createSenderProcess(egress);
        TProcess eventGeneratorProcess = createEventGeneratorProcess(egress);

        // Add Call Activity from sender to 
        addCallActivityBeforeEndEvent(senderProcess, 
            createCallActivityToProcess(
                String.format( SapIflowUtils.ACT_SEND_GENERATE_NAME_TEMPLATE, eventName ), 
                eventGeneratorProcess.getId()
            )
        );

        TParticipant sendParticipant = createGenericParticipant( 
            senderProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            senderProcess.getName(),
            senderProcess.getId() );

        TParticipant eventGeneratoParticipant = createGenericParticipant(
            eventGeneratorProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            eventGeneratorProcess.getName(),
            eventGeneratorProcess.getId() );

        TMessageFlow messageFlow = createPublishMessageFlow(
            egress, 
            senderProcess,
            endParticipantId );

        generateSequences( senderProcess );
        generateSequences( eventGeneratorProcess );
        
        processes.add( senderProcess );
        processes.add( eventGeneratorProcess );
        participants.add( sendParticipant );
        participants.add( eventGeneratoParticipant );
        messageFlows.add( messageFlow );

        createBpmnShapesForParticipantAndProcess(eventGeneratoParticipant, eventGeneratorProcess);
        createBpmnShapesForParticipantAndProcess(sendParticipant, senderProcess);
    }

    private TProcess createSenderProcess( MapSubFlowEgress egress ) {

        String eventName = egress.getMessageName();

        TProcess senderProcess = createGenericProcess(
            String.format( SapIflowUtils.PROCESS_OUT_SEND_NAME_TEMPLATE, eventName ),
            SapIflowUtils.ACT_SEND_START_NAME, 
            SapIflowUtils.ACT_SEND_END_NAME_TEMPLATE );

        //
        addEmptyMessageEventDefinitions(senderProcess);     // Conform with samples
        addExtensionProperties(senderProcess, extConfigs.getOutboundProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(senderProcess, extConfigs.getOutboundProcess());

        final String stubMapSchemaName = getSchemaName(egress.getJsonSchemaReference());
        final String stubMapCaName = "Stub map for transforming source data to " + stubMapSchemaName;
        TCallActivity stubMapCallActivity = createOutboundStubMapCallActivity(stubMapCaName, stubMapSchemaName, egress.getMessageName() );
        addCallActivityBeforeEndEvent( senderProcess, stubMapCallActivity );

        return senderProcess;
    }

    private TProcess createEventGeneratorProcess( MapSubFlowEgress egress ) {

        String eventName = egress.getMessageName();

        TProcess eventGeneratorProcess = createGenericProcess(
            String.format( SapIflowUtils.PROCESS_OUT_GEN_NAME_TEMPLATE, eventName ), 
            String.format( SapIflowUtils.ACT_GEN_START_NAME_TEMPLATE, eventName ),
            String.format( SapIflowUtils.ACT_GEN_END_NAME_TEMPLATE, eventName )
        );
        //
        addExtensionProperties(eventGeneratorProcess, extConfigs.getCalledProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(eventGeneratorProcess, extConfigs.getCalledProcess());

        // TODO - Verify this logic
        for ( Map.Entry<String, String> setVariableEntry : egress.getSetVariables().entrySet() ) {
            addCallActivityBeforeEndEvent(
                eventGeneratorProcess,
                createCallActivityExtractTopicVariable( setVariableEntry.getKey() ));
        }

        // TODO - How to build composed topic?
        addCallActivityBeforeEndEvent(
            eventGeneratorProcess, 
            createCallActivityGenerateComposedTopic( egress.getPublishAddress() ));

        final String validateSchemaName = getSchemaName(egress.getJsonSchemaReference());
        final String validateCaName = "Validate outbound event against schema " + validateSchemaName;
        final TCallActivity validateSchemaCallActivity = 
                createValidateSchemaCallActivity(
                    validateCaName, 
                    validateSchemaName
        );
        addCallActivityBeforeEndEvent( eventGeneratorProcess, validateSchemaCallActivity );
    
        return eventGeneratorProcess;
    }

    private TMessageFlow createPublishMessageFlow( 
        MapSubFlowEgress egress,
        TProcess sourceProcess,
        String targetRef ) {

        String sourceRef = "";
        for ( JAXBElement<? extends TFlowElement> jaxbElt : sourceProcess.getFlowElement() ) {
            if ( jaxbElt.getValue() instanceof TEndEvent ) {
                TEndEvent endEvent = ( TEndEvent )( jaxbElt.getValue() );
                sourceRef = endEvent.getId();
                break;
            }
        }

        //
        TMessageFlow publishMessageFlow = 
            createGenericMessageFlow(
                "Egress Flow Publish event: " + egress.getMessageName(), 
                sourceRef, 
                targetRef
            );
        addExtensionProperties(publishMessageFlow, extConfigs.getMessageFlow().getPublication() );
        
        // addExtensionProperty(publishMessageFlow, "deliveryMode", "DIRECT");
        // addExtensionProperty(publishMessageFlow, "Description", "");
        addExtensionProperty(publishMessageFlow, "Name", egress.getMessageName());

        return publishMessageFlow;
    }

    private TCallActivity createCallActivityExtractTopicVariable( final String variableName ) {
        // TODO - How to create extract variable block?
        final String EXTRACT_VARIABLE_TEMPLATE = "<row><cell>%s</cell><cell></cell><cell>expression</cell><cell>%s</cell><cell>local</cell></row>";
        final String extractVariableXml = String.format( EXTRACT_VARIABLE_TEMPLATE, variableName, ( "$." + variableName ) );
        TCallActivity ca = createGenericCallActivity( SapIflowUtils.ACT_GEN_EXTRACT_TOPIC_PREFIX + variableName);
        addExtensionProperties(ca, extConfigs.getCallActivity().getTopicAssembly());
        addExtensionProperty( ca, "variable", extractVariableXml );
        return ca;
    }

    private TCallActivity createCallActivityGenerateComposedTopic( final String topicPattern ) {
        // TODO - How to create composed topic block?
        TCallActivity ca = createGenericCallActivity( SapIflowUtils.ACT_GEN_COMPOSED_TOPIC_NAME );
        addExtensionProperties(ca, extConfigs.getCallActivity().getTopicAssembly());
        addExtensionProperty( ca, "variable", topicPattern );
        return ca;
    }

    private TCallActivity createCallActivityToProcess( String formattedName, String processToCall ) {

        TCallActivity ca = createGenericCallActivity(formattedName);
        // TODO Add process ref elements
        addExtensionProperty(ca, "processId", processToCall);
        addExtensionProperties(ca, extConfigs.getCallActivity().getCallProcess());
        return ca;
    }

    private TCallActivity createValidateSchemaCallActivity( 
        final String name,
        final String schemaName ) {

        final TCallActivity ca = createGenericCallActivity( name );
        final String mappingName = getValidateMappingName(schemaName);
        addExtensionProperties(ca, extConfigs.getCallActivity().getMapping());
        addExtensionProperty(ca, "mappingname", mappingName);
        addExtensionProperty(ca, "mappingpath", getMappingPath(mappingName));
        addExtensionProperty(ca, "mappinguri", getMappingUri(mappingName));

        return ca;
    }

    private TCallActivity createInboundStubMapCallActivity(
        final String name,
        final String schemaName
    ) {

        final TCallActivity ca = createGenericCallActivity( name );
        final String mappingName = getInboundStubMappingName(schemaName);
        addExtensionProperties(ca, extConfigs.getCallActivity().getMapping());
        addExtensionProperty(ca, "mappingname", mappingName);
        addExtensionProperty(ca, "mappingpath", getMappingPath(mappingName));
        addExtensionProperty(ca, "mappinguri", getMappingUri(mappingName));

        return ca;
    }

    private TCallActivity createOutboundStubMapCallActivity(
        final String name,
        final String schemaName,
        final String messageName
    ) {

        final TCallActivity ca = createGenericCallActivity( name );
        final String mappingName = getOutboundStubMappingName(schemaName, messageName);
        addExtensionProperties(ca, extConfigs.getCallActivity().getMapping());
        addExtensionProperty(ca, "mappingname", mappingName);
        addExtensionProperty(ca, "mappingpath", getMappingPath(mappingName));
        addExtensionProperty(ca, "mappinguri", getMappingUri(mappingName));

        return ca;
    }

    private String getSchemaName( String schemaReference ) {
        String schemaName;
        try {
            schemaName = inputSource.getSchemaMap().get( schemaReference ).getName();
        } catch ( NullPointerException npexc ) {
            schemaName = "SCHEMA_NOT_FOUND";
        }
        return schemaName;
    }

    private String getValidateMappingName( String schemaName ) {
        return "Validate" + ( schemaName != null ? schemaName : "NULL" );
    }

    private String getInboundStubMappingName( final String schemaName ) {
        return ( schemaName != null ? schemaName : "NULL" ) + "ToDestinationFormat";
    }

    private String getOutboundStubMappingName( final String schemaName, final String messageName ) {
        return ( messageName != null ? messageName : "NULL_MESSAGE" ) + "SourceTo" + ( schemaName != null ? schemaName : "NULL" );
    }

    private String getMappingPath( String mappingName ) {
        return "src/main/resources/mapping/" + ( mappingName != null ? mappingName : "NULL" );
    }

    private String getMappingUri( String mappingName ) {
        return mappingName != null ? String.format("dir://mmap/src/main/resources/mapping/%s.mmap", mappingName) : "NULL";
    }

    private void addExtensionProperties( TBaseElement target, List<SapIflowExtensionConfig.ExtProperty> properties ) {
        if ( target.getExtensionElements() == null ) {
            target.setExtensionElements( bpmnFactory.createTExtensionElements() );
        }
        for ( SapIflowExtensionConfig.ExtProperty prop : properties ) {
            target.getExtensionElements().getAny().add(
                createPropertyInstance(
                    prop.getKey(), 
                    ( prop.getValue() == null ? "" : prop.getValue() ) 
                ) 
            );
        }
    }

    private void addExtensionProperty( TBaseElement target, String key, String value ) {
        if ( target.getExtensionElements() == null ) {
            target.setExtensionElements( bpmnFactory.createTExtensionElements() );
        }
        target.getExtensionElements().getAny().add( createPropertyInstance( key, value) );
    }

    private JAXBElement<TSapIflowProperty> createPropertyInstance( String key, String value ) {

        TSapIflowProperty property = propFactory.createTSapIflowProperty();
        property.setKey(key);
        property.setValue(value);

        return propFactory.createProperty(property);
    }

    private void addExtensionPropertiesToStartAndEndEvents( TProcess process, ProcessExt processExtProperties )  {
        for (JAXBElement<? extends TFlowElement> event : process.getFlowElement()) {
            if ( event.getValue() instanceof TStartEvent ) {
                addExtensionProperties(event.getValue(), processExtProperties.getStartEvent());
                continue;
            }
            if ( event.getValue() instanceof TEndEvent ) {
                addExtensionProperties(event.getValue(), processExtProperties.getEndEvent());
                break;
            }
        }
    }

    private void addCallActivityBeforeEndEvent( TProcess process, TCallActivity activity ) {

        process.getFlowElement().add(
            process.getFlowElement().size() - 1, 
            bpmnFactory.createCallActivity(activity)
        );
    }

    /**
     * Create sequence flows for a given process.
     * The process StartEvent, Call Activity(ies) 1-N, and EndEvent must be in sequence
     * Also adds incoming + outgoing events for all flow events
     * @param process
     */
    private void generateSequences( TProcess process ) {

        boolean firstItem = true;
        String  currentSeqFlowId = "", lastSeqFlowId = "";
        Object  lastEventObject = null;

        List<TSequenceFlow> sequenceFlows = new ArrayList<>();

        Iterator<JAXBElement<? extends TFlowElement>> i = process.getFlowElement().iterator();

        while( i.hasNext() ) {

            TFlowElement fe = i.next().getValue();

            if ( firstItem ) {

                firstItem = false;
                currentSeqFlowId = SapIflowUtils.SEQ_FLOW_PREFIX + objectIncrementer++;

                if ( fe instanceof TStartEvent ) {
                    TStartEvent se = ( TStartEvent )fe;
                    se.getOutgoing().add( new QName( currentSeqFlowId ) );
                }

                lastSeqFlowId = currentSeqFlowId;
                lastEventObject = fe;

                continue;
            }

            if ( i.hasNext() ) {

                currentSeqFlowId = SapIflowUtils.SEQ_FLOW_PREFIX + objectIncrementer++;

                if ( fe instanceof TCallActivity ) {
                    TCallActivity ca = ( TCallActivity )fe;
                    ca.getIncoming().add( new QName( lastSeqFlowId ) );
                    ca.getOutgoing().add( new QName( currentSeqFlowId ) );
                }

                TSequenceFlow sf = bpmnFactory.createTSequenceFlow();
                sf.setId( lastSeqFlowId );
                sf.setSourceRef( lastEventObject );
                sf.setTargetRef( fe );

                sequenceFlows.add(sf);

                lastSeqFlowId = currentSeqFlowId;
                lastEventObject = fe;

            } else {

                if ( fe instanceof TEndEvent ) {
                    TEndEvent ee = ( TEndEvent )fe;
                    ee.getIncoming().add( new QName( lastSeqFlowId ) );
                }

                TSequenceFlow sf = bpmnFactory.createTSequenceFlow();
                sf.setId( lastSeqFlowId );
                sf.setSourceRef( lastEventObject );
                sf.setTargetRef( fe );
               
                sequenceFlows.add(sf);
            }
        }

        i = null;
        for ( TSequenceFlow sf : sequenceFlows ) {
            process.getFlowElement().add( bpmnFactory.createSequenceFlow(sf) );
        }

    }

    private TCallActivity createGenericCallActivity( String formattedName ) {
        TCallActivity ca = bpmnFactory.createTCallActivity();
        ca.setId( SapIflowUtils.ACT_CALL_PREFIX + objectIncrementer++ );
        ca.setName(formattedName);
        return ca;
    }

    private TMessageFlow createGenericMessageFlow(
        String name,
        String sourceRef,
        String targetRef
    ) {

        TMessageFlow messageFlow = bpmnFactory.createTMessageFlow();
        messageFlow.setId( SapIflowUtils.MSGFLOW_ID_PREFIX + objectIncrementer++ );
        messageFlow.setName( name );
        messageFlow.setSourceRef( new QName( sourceRef ) );
        messageFlow.setTargetRef( new QName( targetRef ) );
        addExtensionProperties(messageFlow, extConfigs.getMessageFlow().getAllMessageFlows() );

        return messageFlow;
    }

    private void addEmptyMessageEventDefinitions( TProcess process ) {
        process.getFlowElement().forEach( jaxbElt -> {
            if ( jaxbElt.getValue() instanceof TStartEvent ) {
                TStartEvent se = ( TStartEvent )( jaxbElt.getValue() );
                se.getEventDefinition().add( bpmnFactory.createMessageEventDefinition( bpmnFactory.createTMessageEventDefinition() ) );
            } 
            if ( jaxbElt.getValue() instanceof TEndEvent ) {
                TEndEvent ee = ( TEndEvent )( jaxbElt.getValue() );
                ee.getEventDefinition().add( bpmnFactory.createMessageEventDefinition( bpmnFactory.createTMessageEventDefinition() ) );
            } 
        } );
    }

    private TProcess createGenericProcess( 
        String procName, 
        String startEventName,
        String endEventName ) {

        final long processId = objectIncrementer++;
        final long startEndEventId = objectIncrementer++;

        final TProcess process = bpmnFactory.createTProcess();
        final TStartEvent startEvent = bpmnFactory.createTStartEvent();
        final TEndEvent endEvent = bpmnFactory.createTEndEvent();

        process.setId( SapIflowUtils.PROCESS_ID_PREFIX + processId );
        process.setName( procName );
        startEvent.setId( SapIflowUtils.ACT_START_EVENT_PREFIX + startEndEventId );
        startEvent.setName( startEventName );
        // TODO - The following is only to sync with sample data
        // startEvent.getEventDefinition( ).add( bpmnFactory.createMessageEventDefinition( bpmnFactory.createTMessageEventDefinition() ) );

        // startEvent.getEventDefinition().add( bpmnFactory.createMessageEventDefinition( bpmnFactory.createTMessageEventDefinition() ) );
        endEvent.setId( SapIflowUtils.ACT_END_EVENT_PREFIX + startEndEventId );
        endEvent.setName( endEventName );
        // TODO - The following is only to sync with sample data
        // endEvent.getEventDefinition( ).add( bpmnFactory.createMessageEventDefinition( bpmnFactory.createTMessageEventDefinition() ) );

        process.getFlowElement().add( bpmnFactory.createStartEvent( startEvent ) );
        process.getFlowElement().add( bpmnFactory.createEndEvent( endEvent ) );

        return process;
    }

    private TParticipant createGenericParticipant( 
        String type,
        String name
    ) {
        return createGenericParticipant(
            String.valueOf( objectIncrementer++ ), 
            type, 
            name);
    }

    private TParticipant createGenericParticipant(
        String processId,
        String type,
        String name
    ) {
        return createGenericParticipant(processId, type, name, null);
    }

    private TParticipant createGenericParticipant(
        String processId,
        String type,
        String name,
        String processRef
    ) {
        TParticipant participant = bpmnFactory.createTParticipant();
        participant.setId( SapIflowUtils.PARTICIPANT_ID_PREFIX + processId );
        // TODO - IFL:TYPE
        participant.setName( name );
        if ( processRef != null ) {
            participant.setProcessRef( new QName( processRef) );
        }
        participant.getOtherAttributes().put( propFactory.getSapIflowType_QName() , type );
        if ( participant.getExtensionElements() == null ) {
            participant.setExtensionElements( bpmnFactory.createTExtensionElements() );
        }

        return participant;
    }

    /**
     *  BPMNShape Handling methods 
     */

    private BPMNShape addBpmnShapeFromStaticParticipant( final TParticipant participant, double x, final double y ) {
        final double h = BPMN_STATIC_PARTICIPANT_H, w = BPMN_STATIC_PARTICIPANT_W;
        BPMNShape shape = createGenericBPMNShape( participant.getId(), h, w, x, y );
        shapes.put(shape.getId(), shape);
        return shape;
    }

    private BPMNShape addBpmnShapeFromFlowElement( final TFlowElement flowElement, final double x, final double y ) {
        BPMNShape shape = null;
        if ( flowElement instanceof TCallActivity ) {
            shape = addBpmnShapeFromCallActivity( (TCallActivity)flowElement, x, y);
        } else if ( flowElement instanceof TStartEvent || flowElement instanceof TEndEvent ) {
            shape = addBpmnShapeFromStartEndEvent(flowElement, x, y);
        }
        if (shape != null) {
            shapes.put(shape.getId(), shape);
        }
        return shape;
    }

    private BPMNShape addBpmnShapeFromStartEndEvent( final TFlowElement flowElement, final double x, final double y ) {
        final double h = BPMN_START_END_EVENT_H, w = BPMN_START_END_EVENT_W;
        BPMNShape shape = createGenericBPMNShape( flowElement.getId(), h, w, x, y );
        return shape;
    }

    private BPMNShape addBpmnShapeFromCallActivity( final TCallActivity callActivity, final double x, final double y) {
        final double h = BPMN_CALL_ACT_H, w = BPMN_CALL_ACT_W;
        BPMNShape shape = createGenericBPMNShape(callActivity.getId(), h, w, x, y);
        return shape;
    }

    private BPMNShape addBpmnShapeFromParticipantProcess( 
        final TParticipant participant, final double x, final double y, final double w )
    {
        double h = BPMN_PART_PROC_H;
        BPMNShape shape = createGenericBPMNShape(participant.getId(), h, w, x, y);
        shapes.put(shape.getId(), shape);
        return shape;
    }

    private BPMNShape createGenericBPMNShape( final String id, final double h, final double w, final double x, final double y ) {
        BPMNShape shape = bpmnDiFactory.createBPMNShape();
        shape.setBpmnElement( new QName( id ) );
        shape.setId( "BPMNShape_" + id );
        shape.setBounds( createBounds(h, w, x, y) );
        return shape;
    }

    private Bounds createBounds( final double h, final double w, final double x, final double y ) {
        Bounds bounds = dcFactory.createBounds();
        bounds.setHeight( h );
        bounds.setWidth( w );
        bounds.setX(x);
        bounds.setY(y);
        return bounds;
    }

    private void updateBpmnShapePositionX(final BPMNShape bpmnShape, final double x) {
        bpmnShape.getBounds().setX(x);
    }

    private void createBpmnShapesForParticipantAndProcess( TParticipant participant, TProcess process ) {
    
        double xPos = processBoundaryX;

        for ( JAXBElement<? extends TFlowElement> jaxbElement : process.getFlowElement() ) {
            TFlowElement elt = jaxbElement.getValue();
            if ( !(elt instanceof TStartEvent || elt instanceof TEndEvent || elt instanceof TCallActivity ) ) {
                continue;
            }
            double x = getFlowEltX(xPos);
            double y = getFlowEltY(elt, processBoundaryY);
            addBpmnShapeFromFlowElement(elt, x, y);
            xPos += advanceXPos(elt);
        }

        // Create BPMN Shapes for Sequence Flows
        // Can only do this after all of the shapes to join have been created
        for ( JAXBElement<? extends TFlowElement> jaxbElement : process.getFlowElement() ) {
            TFlowElement elt = jaxbElement.getValue();
            if ( (elt instanceof TSequenceFlow ) ) {
                TSequenceFlow sf = ( TSequenceFlow )elt;
                createBpmnEdgeFromSequenceFlow(sf);
            }
        }

        addBpmnShapeFromParticipantProcess(participant, processBoundaryX, processBoundaryY, ( xPos - processBoundaryX ) );

        processBoundaryY += BPMN_PART_PROC_H + BPMN_PROC_SEP_H;
        processBoundaryMaxX = Math.max(xPos, processBoundaryMaxX);
    }

    private double getFlowEltY( TFlowElement elt, double boundaryY ) {
        double dimY = ( elt instanceof TCallActivity ? BPMN_CALL_ACT_H : BPMN_START_END_EVENT_H);
        return boundaryY + ( BPMN_PART_PROC_H / 2 ) - ( dimY / 2 );
    }

    private double getFlowEltX( double boundaryX ) {
        return boundaryX + BPMN_FLOW_ELT_SEP_X;
    }

    private double advanceXPos( TFlowElement elt ) {
        double advanceShapeX = ( elt instanceof TCallActivity ? BPMN_CALL_ACT_W : BPMN_START_END_EVENT_W ); 
        return advanceShapeX + BPMN_FLOW_ELT_SEP_X;
    }

    /**
     * BPMNEdge Handling methods
     * These methods are used to connect BPMNShape objects
     */

    private void createBpmnEdgeFromSequenceFlow( TSequenceFlow sequenceFlow ) {
        BPMNEdge edge = bpmnDiFactory.createBPMNEdge();
        edge.setBpmnElement( new QName(sequenceFlow.getId()) );
        edge.setId( "BPMNEdge_" + sequenceFlow.getId() );
        String bpmnSourceRefId = "BPMNShape_" + getSourceRefIdFromSequenceFlow(sequenceFlow);
        String bpmnTargetRefId = "BPMNShape_" + getTargetRefIdFromSequenceFlow(sequenceFlow);
        edge.setSourceElement( new QName( bpmnSourceRefId ) );
        edge.setTargetElement( new QName( bpmnTargetRefId ) );

        BPMNShape leftShape = shapes.get(bpmnSourceRefId);
        BPMNShape rightShape = shapes.get(bpmnTargetRefId);
        connectShapesLeftToRight(edge, leftShape, rightShape);
        edges.add(edge);
    }

    private void createBpmnEdgeFromMessageFlow( TMessageFlow messageFlow ) {
        BPMNEdge edge = bpmnDiFactory.createBPMNEdge();
        edge.setBpmnElement( new QName(messageFlow.getId()) );
        edge.setId( "BPMNEdge_" + messageFlow.getId() );
        String bpmnSourceRefId = "BPMNShape_" + messageFlow.getSourceRef().getLocalPart();
        String bpmnTargetRefId = "BPMNShape_" + messageFlow.getTargetRef().getLocalPart();
        edge.setSourceElement( new QName( bpmnSourceRefId ) );
        edge.setTargetElement( new QName( bpmnTargetRefId ) );

        BPMNShape leftShape = shapes.get(bpmnSourceRefId);
        BPMNShape rightShape = shapes.get(bpmnTargetRefId);
        connectShapesLeftToRight(edge, leftShape, rightShape);
        edges.add(edge);
    }

    private void connectShapesLeftToRight( BPMNEdge edge, BPMNShape leftShape, BPMNShape rightShape ) {
        double leftX = leftShape.getBounds().getX() + leftShape.getBounds().getWidth();
        double leftY = leftShape.getBounds().getY() + ( leftShape.getBounds().getHeight() / 2 );
        double rightX = rightShape.getBounds().getX();
        double rightY = rightShape.getBounds().getY() + ( rightShape.getBounds().getHeight() / 2 );
        
        Point leftPoint = dcFactory.createPoint();
        leftPoint.setX(leftX);
        leftPoint.setY(leftY);
        
        Point rightPoint = dcFactory.createPoint();
        rightPoint.setX(rightX);
        rightPoint.setY(rightY);

        edge.getWaypoint().add(leftPoint);
        edge.getWaypoint().add(rightPoint);
    }

    private String getSourceRefIdFromSequenceFlow( Object sequenceFlowObject ) {
        if ( sequenceFlowObject instanceof TSequenceFlow ) {
            TSequenceFlow sf = ( TSequenceFlow )sequenceFlowObject;
            return getReferencedObjectId(sf.getSourceRef());
        }
        return "";
    }

    private String getTargetRefIdFromSequenceFlow( Object sequenceFlowObject ) {
        if ( sequenceFlowObject instanceof TSequenceFlow ) {
            TSequenceFlow sf = ( TSequenceFlow )sequenceFlowObject;
            return getReferencedObjectId(sf.getTargetRef());
        }
        return "";
    }

    private String getReferencedObjectId( Object baseElementObject ) {
        if ( baseElementObject instanceof TBaseElement ) {
            TBaseElement baseElement = ( TBaseElement )baseElementObject;
            return baseElement.getId();
        }
        log.warn( "Object of class: [{}] is not valid to use as a reference" );
        return "";
    }
}
