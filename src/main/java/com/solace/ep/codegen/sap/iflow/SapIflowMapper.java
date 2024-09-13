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
package com.solace.ep.codegen.sap.iflow;

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

import com.solace.ep.codegen.internal.model.MapFlow;
import com.solace.ep.codegen.internal.model.MapMuleDoc;
import com.solace.ep.codegen.internal.model.MapSubFlowEgress;
import com.solace.ep.codegen.mule.mapper.MapUtils;
import com.solace.ep.codegen.sap.iflow.model.bpmn_ifl_ext.TSapIflowProperty;
import com.solace.ep.codegen.sap.iflow.model.config.SapIflowExtensionConfig;
import com.solace.ep.codegen.sap.iflow.model.config.SapIflowExtensionConfig.ProcessExt;

import jakarta.xml.bind.JAXBElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to create SAP IFlow BPMN2 document from MapMuleDoc object (created from AsyncApi)
 */
@Slf4j
public class SapIflowMapper {

    // Extension elements configuration file
    private static final String
            EXT_ELEMENTS_CONFIG_FILE = "src/main/resources/sap/iflow/extension-elements.yaml";

    // BPMN Graph constants
    // Object H+W, Desired spacing
    private static final double
            BPMN_START_BOUNDARY_X = 40d,
            BPMN_START_BOUNDARY_Y = 40d,
            BPMN_STATIC_PARTICIPANT_W = 100d,
            BPMN_STATIC_PARTICIPANT_H = 140d,
            BPMN_PART_PROC_H = 200d,
            BPMN_PROC_SEP_Y = 60d,
            BPMN_START_END_EVENT_H = 32d,
            BPMN_START_END_EVENT_W = 32d,
            BPMN_FLOW_ELT_SEP_X = 60d,
            BPMN_CALL_ACT_H = 80d,
            BPMN_CALL_ACT_W = 150d,
            BPMN_MESH_PARTICIPANT_PROCESS_SEP_X = 100d,
            BPMN_MESH_PARTICIPANT_PROCESS_SEP_Y = 60d;

    // BPMN Graph position state variables
    private double
            processBoundaryX = BPMN_START_BOUNDARY_X,
            processBoundaryY = BPMN_START_BOUNDARY_Y,
            processBoundaryMaxX = BPMN_START_BOUNDARY_X;

    // BPMN Object ID Incrementer
    private long objectIncrementer = 1L;

    // Enumerate message flow names
    private long messageFlowCounter = 1L;

    // Object Factories
    private final ObjectFactory bpmnFactory = new ObjectFactory();

    private final org.omg.spec.bpmn._20100524.di.ObjectFactory 
                    diFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

    private final org.omg.spec.dd._20100524.dc.ObjectFactory 
                    dcFactory = new org.omg.spec.dd._20100524.dc.ObjectFactory();

    private final com.solace.ep.codegen.sap.iflow.model.bpmn_ifl_ext.ObjectFactory 
                    iflFactory = new com.solace.ep.codegen.sap.iflow.model.bpmn_ifl_ext.ObjectFactory();

    // BPMN Objects
    private final List<TParticipant> participants = new ArrayList<>();

    private final List<TProcess> processes = new ArrayList<>();

    private final List<TMessageFlow> messageFlows = new ArrayList<>();

    // BPMN Graph Objects
    private final Map<String, BPMNShape> shapes = new LinkedHashMap<>();

    private final List<BPMNEdge> edges = new ArrayList<>();

    // Input Object
    private MapMuleDoc inputSource;

    // Output Object
    private TDefinitions rootElement = null;

    // SAP IFlow Extensions for BPMN
    private SapIflowExtensionConfig extConfigs;

    @Data
    @AllArgsConstructor
    private class ParticipantAndProcess {

        TParticipant participant;

        TProcess process;

    }

    // Slightly redundant construct used to maintain input participant-process relationships
    private final List<ParticipantAndProcess> inputParticipantsAndProcesses = new ArrayList<>();

    // Slightly redundant construct used to maintain output participant-process relationships
    private final List<ParticipantAndProcess> outputParticipantsAndProcesses = new ArrayList<>();

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
    
     /**
      * Builds the SAP IFlow using the MapMuleDoc object passed to the constructor.
      * @return root element of type JAXBElement<TDefinitions>
      */
    public JAXBElement<TDefinitions> createSapIflowAsJAXBElement() throws Exception {
        return bpmnFactory.createDefinitions( createSapIflow() );
    }

    /**
     * Builds the SAP IFlow using the MapMuleDoc object passed to the constructor.
     * Returns root element of type TDefinitions which must be wrapped in a JAXB Element
     * for serialization
     * @return
     */
    /**
    public TDefinitions createSapIflow(boolean alt) throws Exception {

        if ( this.rootElement != null ) {
            log.info( "Returning previously constructed SAP BPMN2 IFlow object" );
            return this.rootElement;
        }

        log.info("START - Create SAP IFlow BPMN2 Document");
        BPMNShape   bpmnDestinationSystemReceiver = null, bpmnEventMeshReceiver = null;
        String startParticipantId = "", endParticipantId = "";

        // Create input mesh participants (static)
        // Only if there are consumer flows defined
        if ( inputSource.getMapFlows().size() > 0 ) {
            log.info("There are {} Consumer(s) found in input", inputSource.getMapFlows().size());
            final TParticipant eventMeshSender = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SEND );
            addExtensionProperties(eventMeshSender, extConfigs.getParticipant().getEventMeshSender());

            final TParticipant sourceSystem = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SRC );
            addExtensionProperties(sourceSystem, extConfigs.getParticipant().getSourceSystem());
            
            startParticipantId = eventMeshSender.getId();

            participants.add(eventMeshSender);
            participants.add(sourceSystem);

            // Add objects to the graph
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

            // Set the leftX start boundary to include Sender participants
            processBoundaryX += BPMN_STATIC_PARTICIPANT_W + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X;
            processBoundaryMaxX = processBoundaryX;
            log.debug( "EventMeshSender Participants Created" );
        } else {
            log.info( "No Consumers found in input" );
        }

        // Create output mesh participants (static)
        // Only if there are publisher flows defined
        if ( inputSource.getMapEgressSubFlows().size() > 0 ) {
            log.info( "There are {} Publication Flows found in the input", inputSource.getMapEgressSubFlows().size() );
            final TParticipant eventMeshReceiver = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_RECV );
            addExtensionProperties(eventMeshReceiver, extConfigs.getParticipant().getEventMeshReceiver());

            final TParticipant destinationSystemReceiver = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_DEST );
            addExtensionProperties(destinationSystemReceiver, extConfigs.getParticipant().getDestinationSystemReceiver());
            
            endParticipantId = eventMeshReceiver.getId();

            participants.add(eventMeshReceiver);
            participants.add(destinationSystemReceiver);

            // Add objects to the graph - initially on the top left
            // The receivers will be moved to the right of the processes after
            // the maximum process width is known
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
            log.debug( "EventMeshReceiver Participants Created" );
        } else {
            log.info( "No Publication Flows found in input" );
        }

        // Create INPUT Proceses (Consumers)
        for ( MapFlow consumer : inputSource.getMapFlows() ) {
            mapConsumerToIflow(consumer, startParticipantId);
        }

        // Create Output Processes (Publishers)
        for ( MapSubFlowEgress publisher : inputSource.getMapEgressSubFlows() ) {
            mapPublisherToIflow(publisher, endParticipantId);
        }

        // Move the Receiver participants to the right of the processes on the graph
        if ( bpmnDestinationSystemReceiver != null && bpmnEventMeshReceiver != null ) { 
            updateBpmnShapePositionX(bpmnEventMeshReceiver, ( processBoundaryMaxX + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X ) );
            updateBpmnShapePositionX(bpmnDestinationSystemReceiver, ( processBoundaryMaxX + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X ) );
        }

        // Add the message flows to the BPMN Edge list
        messageFlows.forEach( flow -> {
            createBpmnEdgeFromMessageFlow(flow);
        } );

        // Create the BPMN Document
        final TCollaboration collaboration = bpmnFactory.createTCollaboration();
        collaboration.setId( SapIflowUtils.COLLAB_ID_PREFIX + "1" );
        String collaborationName;
        if ( inputSource.getGlobalProperties() != null && 
            inputSource.getGlobalProperties().containsKey( MapUtils.GLOBAL_NAME_EP_APP_VERSION ) &&
            inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION ).length() > 0 &&
            inputSource.getGlobalProperties().containsKey( MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE ) &&
            inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE ).length() > 0
        ) {
            collaborationName = 
                inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE ).replace( " ", "_" ) +
                "_" +
                inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION );
        } else {
            collaborationName = SapIflowUtils.COLLAB_NAME_DEFAULT;
        }
        collaboration.setName( collaborationName );
        addExtensionProperties(collaboration, extConfigs.getCollaboration());
        collaboration.getParticipant().addAll(participants);
        collaboration.getMessageFlow().addAll(messageFlows);

        final TDefinitions definitions = bpmnFactory.createTDefinitions();
        definitions.setId( SapIflowUtils.DEFINITIONS_PREFIX + "1" );
        definitions.getRootElement().add( bpmnFactory.createCollaboration(collaboration) );
        processes.forEach( process -> {
            definitions.getRootElement().add( bpmnFactory.createProcess( process ) );
        });

        // Add the graph to the BPMN Document
        final BPMNPlane plane = diFactory.createBPMNPlane();
        plane.setId( "BPMNPlane_1" );
        plane.setBpmnElement( new QName( collaboration.getId() ) );
        shapes.forEach( ( name, shape ) -> {
            plane.getDiagramElement().add( diFactory.createBPMNShape( shape ) );
        } );
        edges.forEach( edge -> {
            plane.getDiagramElement().add( diFactory.createBPMNEdge(edge) );
        } );

        final BPMNDiagram diagram = diFactory.createBPMNDiagram();
        diagram.setId( "BPMNDiagram_1" );
        diagram.setName( collaborationName + " Diagram" );
        diagram.setBPMNPlane(plane);

        definitions.getBPMNDiagram().add(diagram);

        log.info("DONE - Create SAP IFlow BPMN2 Document");
        this.rootElement = definitions;
        return this.rootElement;
    }
    **/

    private String getParticipantNameById( String participantId ) {
        for( TParticipant p : participants ) {
            if ( p.getId().contentEquals(participantId) ) {
                return p.getName();
            }
        }
        return "";
    }

    /**
     * Builds the SAP IFlow using the MapMuleDoc object passed to the constructor.
     * Returns root element of type TDefinitions which must be wrapped in a JAXB Element
     * for serialization
     * @return
     */
    public TDefinitions createSapIflow() throws Exception {

        boolean aemInput = inputSource.getMapFlows().size() > 0;
        boolean aemOutput = inputSource.getMapEgressSubFlows().size() > 0;

        if ( this.rootElement != null ) {
            log.info( "Returning previously constructed SAP BPMN2 IFlow object" );
            return this.rootElement;
        }

        log.info("START - Create SAP IFlow BPMN2 Document");
        // BPMNShape   bpmnDestinationSystemReceiver = null, bpmnEventMeshReceiver = null;
        BPMNShape bpmnReceiver = null;
        String startParticipantId = "", endParticipantId = "";

        // Create Sender / Input
        TParticipant sender;
        if ( aemInput ) {
            // AEM Source System
            log.info("There are {} Consumer(s) found in input", inputSource.getMapFlows().size());
            final TParticipant eventMeshSender = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SEND );
            addExtensionProperties(eventMeshSender, extConfigs.getParticipant().getEventMeshSender());
            sender = eventMeshSender;
        } else {
            // HTTPS Source System
            log.info("Creating HTTPS Sender Source System");
            final TParticipant httpSender = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_SEND,
                // TODO - change sender participant name?
                SapIflowUtils.PARTICIPANT_NAME_SRC
            );
            addExtensionProperties(httpSender, extConfigs.getParticipant().getSourceSystem());
            sender = httpSender;
        }

        startParticipantId = sender.getId();
        participants.add(sender);

        // Add objects to the graph
        addBpmnShapeFromStaticParticipant(
            sender, 
            BPMN_START_BOUNDARY_X, 
            BPMN_START_BOUNDARY_Y
        );

        // Set the leftX start boundary to include Sender participants
        processBoundaryX += BPMN_STATIC_PARTICIPANT_W + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X;
        processBoundaryMaxX = processBoundaryX;
        log.debug( "EventMeshSender Participants Created" );

        // Create output mesh participants (static)
        // Only if there are publisher flows defined
        TParticipant receiver;
        if ( aemOutput ) {
            log.info( "There are {} Publication Flows found in the input", inputSource.getMapEgressSubFlows().size() );
            final TParticipant eventMeshReceiver = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_RECV );
            addExtensionProperties(eventMeshReceiver, extConfigs.getParticipant().getEventMeshReceiver());
            receiver = eventMeshReceiver;
        } else {
            log.info( "Creating HTTP Destination System Receiver" );
            final TParticipant destinationSystemReceiver = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                // TODO - change receiver participant name?
                SapIflowUtils.PARTICIPANT_NAME_DEST );
            addExtensionProperties(destinationSystemReceiver, extConfigs.getParticipant().getDestinationSystemReceiver());
            receiver = destinationSystemReceiver;
        }

        endParticipantId = receiver.getId();
        participants.add(receiver);

        // Add objects to the graph - initially on the top left
        // The receivers will be moved to the right of the processes after
        // the maximum process width is known
        bpmnReceiver = addBpmnShapeFromStaticParticipant(
            receiver, 
            BPMN_START_BOUNDARY_X, 
            BPMN_START_BOUNDARY_Y
        );
        // bpmnDestinationSystemReceiver = addBpmnShapeFromStaticParticipant(
        //     // destinationSystemReceiver, 
        //     BPMN_START_BOUNDARY_X, 
        //     ( 
        //         BPMN_START_BOUNDARY_Y + 
        //         BPMN_STATIC_PARTICIPANT_H + 
        //         BPMN_MESH_PARTICIPANT_PROCESS_SEP_Y
        //     )
        // );
        log.debug( "EventMeshReceiver Participants Created" );

        // Create INPUT Proceses (Consumers)
        for ( MapFlow consumer : inputSource.getMapFlows() ) {
            mapConsumerToIflow(consumer, startParticipantId, endParticipantId, aemOutput);
        }
        // Or HTTP Input if #consumers == 0
        if (inputSource.getMapFlows().size() == 0) {
            mapHttpSourceToIflow(startParticipantId, endParticipantId, aemOutput);
        }

        // Create Output Processes (Publishers)
        // for ( MapSubFlowEgress publisher : inputSource.getMapEgressSubFlows() ) {
        //     mapPublisherToIflow(publisher, endParticipantId);
        // }
        for ( int idx = 0; idx < inputSource.getMapEgressSubFlows().size(); idx++ ) {
            mapPublisherToIflow(inputSource.getMapEgressSubFlows().get(idx), idx, endParticipantId);
        }

        // Add references to publisher sub-flows from consumer flows, if any
        addPublisherRefFromConsumerFlows();

        // Generate sequences for all iFlow processes
        processes.forEach( iFlowProcess -> {
            generateSequences(iFlowProcess);
        } );

        inputParticipantsAndProcesses.forEach( iPAndP  -> {
            createBpmnShapesForParticipantAndProcess(iPAndP.getParticipant(), iPAndP.getProcess());
        });

        outputParticipantsAndProcesses.forEach( oPAndP -> {
            createBpmnShapesForParticipantAndProcess(oPAndP.getParticipant(), oPAndP.getProcess());
        });

        // Move the Receiver participants to the right of the processes on the graph
        if ( bpmnReceiver != null ) { 
            updateBpmnShapePositionX(bpmnReceiver, ( processBoundaryMaxX + BPMN_MESH_PARTICIPANT_PROCESS_SEP_X ) );
        }

        // Add the message flows to the BPMN Edge list
        messageFlows.forEach( flow -> {
            createBpmnEdgeFromMessageFlow(flow);
        } );

        // Create the BPMN Document
        final TCollaboration collaboration = bpmnFactory.createTCollaboration();
        collaboration.setId( SapIflowUtils.COLLAB_ID_PREFIX + "1" );
        String collaborationName;
        if ( inputSource.getGlobalProperties() != null && 
            inputSource.getGlobalProperties().containsKey( MapUtils.GLOBAL_NAME_EP_APP_VERSION ) &&
            inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION ).length() > 0 &&
            inputSource.getGlobalProperties().containsKey( MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE ) &&
            inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE ).length() > 0
        ) {
            collaborationName = 
                inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION_TITLE ).replace( " ", "_" ) +
                "_" +
                inputSource.getGlobalProperties().get( MapUtils.GLOBAL_NAME_EP_APP_VERSION );
        } else {
            collaborationName = SapIflowUtils.COLLAB_NAME_DEFAULT;
        }
        collaboration.setName( collaborationName );
        addExtensionProperties(collaboration, extConfigs.getCollaboration());
        collaboration.getParticipant().addAll(participants);
        collaboration.getMessageFlow().addAll(messageFlows);

        final TDefinitions definitions = bpmnFactory.createTDefinitions();
        definitions.setId( SapIflowUtils.DEFINITIONS_PREFIX + "1" );
        definitions.getRootElement().add( bpmnFactory.createCollaboration(collaboration) );
        processes.forEach( process -> {
            definitions.getRootElement().add( bpmnFactory.createProcess( process ) );
        });

        // Add the graph to the BPMN Document
        final BPMNPlane plane = diFactory.createBPMNPlane();
        plane.setId( "BPMNPlane_1" );
        plane.setBpmnElement( new QName( collaboration.getId() ) );
        shapes.forEach( ( name, shape ) -> {
            plane.getDiagramElement().add( diFactory.createBPMNShape( shape ) );
        } );
        edges.forEach( edge -> {
            plane.getDiagramElement().add( diFactory.createBPMNEdge(edge) );
        } );

        final BPMNDiagram diagram = diFactory.createBPMNDiagram();
        diagram.setId( "BPMNDiagram_1" );
        diagram.setName( collaborationName + " Diagram" );
        diagram.setBPMNPlane(plane);

        definitions.getBPMNDiagram().add(diagram);

        log.info("DONE - Create SAP IFlow BPMN2 Document");
        this.rootElement = definitions;
        return this.rootElement;
    }

    private void addPublisherRefFromConsumerFlows() {

        /**
         * consumerCount --> the number of inputs into the flow:
         * - Either 1 or more AEM inputs OR
         * - 1 HTTP adaptor input
         * publisherCount --> ONLY the number of AEM publishers in the flow
         */
        final int   consumerCount = inputParticipantsAndProcesses.size(),
                    publisherCount = outputParticipantsAndProcesses.size();

        // Nothing to do!
        if ( publisherCount == 0 ) {
            return;
        }

        // publisherCount >= 1
        // If one input flow and one publish flow
        // OR
        // If multiple input flows
        // Then select the first publish event for all consumers
        if ( ( consumerCount == 1 && publisherCount == 1 ) || consumerCount > 1 ) {
            final TProcess publisherProcess = outputParticipantsAndProcesses.get(0).getProcess();
            inputParticipantsAndProcesses.forEach( inputPAndP -> {
                final TProcess consumerProcess = inputPAndP.getProcess();
                final TCallActivity ca = createCallActivityToProcess( "Call " + publisherProcess.getName(), publisherProcess.getId());
                addCallActivityBeforeEndEvent(consumerProcess, ca);
            } );
            return;
        }
        if ( consumerCount == 1 && publisherCount > 1 ) {
            // TODO - Add fanout of consumer to publishers
            final TProcess publisherProcess = outputParticipantsAndProcesses.get(0).getProcess();
            inputParticipantsAndProcesses.forEach( inputPAndP -> {
                final TProcess consumerProcess = inputPAndP.getProcess();
                final TCallActivity ca = createCallActivityToProcess( "Call " + publisherProcess.getName(), publisherProcess.getId());
                addCallActivityBeforeEndEvent(consumerProcess, ca);
            } );
        }
    }

    /**
     * Create input processes, one for each consumer identified in the input
     * @param consumer
     * @param startParticipantId
     */
    private void mapConsumerToIflow( 
        final MapFlow consumer, 
        final String startParticipantId,
        final String endParticipantId,
        final boolean aemOutput )
    {
        final String eventName = consumer.isDirectConsumer() ? consumer.getFlowDesignation() : consumer.getQueueListenerAddress();

        // log.debug("START - Create Receiver and BusinessLogic Processes for input [{}]", eventName);
        log.debug("START - Create Receiver Process for input [{}]", eventName);

        final TProcess receiverProcess = createReceiverProcess(consumer);
        // final TProcess businessLogicProcess = createBusinessLogicProcess(consumer);

        // Add call from receiverProcess to businessLogicProcess
        // addCallActivityBeforeEndEvent(
        //     receiverProcess, 
        //     createCallActivityToProcess( 
        //         String.format( SapIflowUtils.ACT_BL_TEMPLATE_NAME, eventName ),
        //         businessLogicProcess.getId()
        //     )
        // );

        // TODO - Evaluate where sequences should be generated 1
        // generateSequences( receiverProcess );
        // generateSequences( businessLogicProcess );

        final TParticipant inboundParticipantProc = createGenericParticipant( 
            receiverProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            receiverProcess.getName(),
            receiverProcess.getId() );

        // final TParticipant businessLogicParticipant = createGenericParticipant(
        //     businessLogicProcess.getId(), 
        //     SapIflowUtils.PARTICIPANT_IFL_INT,
        //     businessLogicProcess.getName(),
        //     businessLogicProcess.getId() );
        
        final TMessageFlow inputMessageFlow = createSubscribeMessageFlow(
            consumer, 
            startParticipantId, 
            receiverProcess );

        // TODO - Output Message Flow: HTTP or AEM: aemOutput
        TMessageFlow outputMessageFlow;
        if ( aemOutput ) {
            outputMessageFlow = createPublishMessageFlow(receiverProcess, endParticipantId);
        } else {
            outputMessageFlow = createHttpOutMessageFlow(receiverProcess, endParticipantId);
        }

        // TODO - Should this be here?
        processes.add( receiverProcess );
        // processes.add( businessLogicProcess );
        participants.add( inboundParticipantProc );
        // participants.add( businessLogicParticipant );
        //
        messageFlows.add( inputMessageFlow );
        messageFlows.add( outputMessageFlow );

        inputParticipantsAndProcesses.add( new ParticipantAndProcess(inboundParticipantProc, receiverProcess) );
        // inputParticipantsAndProcesses.add( new ParticipantAndProcess(businessLogicParticipant, businessLogicProcess));
        // createBpmnShapesForParticipantAndProcess( inboundParticipantProc, receiverProcess );
        // createBpmnShapesForParticipantAndProcess( businessLogicParticipant, businessLogicProcess);
        // log.info("DONE - Create Receiver and BusinessLogic Processes for input [{}]", eventName);
        log.info("DONE - Create Receiver Process for input [{}]", eventName);
    }

    /**
     * Create input processes, one for each consumer identified in the input
     * @param consumer
     * @param startParticipantId
     */
    private void mapHttpSourceToIflow(
        final String startParticipantId,
        final String endParticipantId,
        final boolean aemOutput ) {

        log.debug("START - Create HTTPS Input Process");

        // TODO - May add stub business logic flow here - if so, move to a method
        final TProcess receiverProcess = createGenericProcess(
            "Initiate Event from Source System",
            "Initiate Event", 
            "Send to Destination" );
        addEmptyMessageEventDefinitions(receiverProcess);
        addExtensionProperties(receiverProcess, extConfigs.getInboundProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(receiverProcess, extConfigs.getInboundProcess());

        // TODO - Evaluate where sequences should be generated 2
        // generateSequences( receiverProcess );

        final TParticipant inboundParticipantProc = createGenericParticipant( 
            receiverProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            receiverProcess.getName(),
            receiverProcess.getId() );

        final TMessageFlow inputMessageFlow = createHttpsInMessageFlow(startParticipantId, receiverProcess);

        TMessageFlow outputMessageFlow;
        if ( aemOutput ) {
            outputMessageFlow = createPublishMessageFlow(receiverProcess, endParticipantId);
        } else {
            outputMessageFlow = createHttpOutMessageFlow(receiverProcess, endParticipantId);
        }

        // TODO - Should this be here?
        processes.add( receiverProcess );
        participants.add( inboundParticipantProc );
        messageFlows.add( inputMessageFlow );
        messageFlows.add( outputMessageFlow );

        inputParticipantsAndProcesses.add( new ParticipantAndProcess(inboundParticipantProc, receiverProcess) );
        // createBpmnShapesForParticipantAndProcess( inboundParticipantProc, receiverProcess );
        log.info("DONE - Create Receiver for HTTP input");
    }

    /**
     * Create inbound receiver process
     * @param consumer
     * @return
     */
    private TProcess createReceiverProcess( final MapFlow consumer ) {

        final String eventSource = consumer.isDirectConsumer() ? consumer.getFlowDesignation() : consumer.getQueueListenerAddress();
        final String consumerOrQueue = consumer.isDirectConsumer() ? "Topic Consumer" : "Queue";
        final String processName = String.format( SapIflowUtils.PROCESS_INB_NAME_TEMPLATE, eventSource ) + " " + consumerOrQueue;

        final TProcess receiverProcess = createGenericProcess(
            processName,
            "Receive Event", 
            "Send to Receiver Destination" );

        addEmptyMessageEventDefinitions(receiverProcess);       // Conform with samples
        addExtensionProperties(receiverProcess, extConfigs.getInboundProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(receiverProcess, extConfigs.getInboundProcess());

        final String validateSchemaName = getSchemaName(consumer.getJsonSchemaReference());
        final String validateCaName = "Validate inbound event against schema " + validateSchemaName;
        final TCallActivity validateSchemaCallActivity = 
                createValidateSchemaCallActivity(
                    validateCaName, 
                    validateSchemaName
        );
        addCallActivityBeforeEndEvent( receiverProcess, validateSchemaCallActivity );

        // Added for re-configure
        final String mapToSchemaName = getSchemaName(consumer.getJsonSchemaReference());
        final String stubMapCa = String.format("Stub map from %s to destination format", mapToSchemaName);
        final TCallActivity stubMapCallActivity = 
            createInboundStubMapCallActivity(
                    stubMapCa, 
                    mapToSchemaName
        );
        addCallActivityBeforeEndEvent(receiverProcess, stubMapCallActivity);

        return receiverProcess;
    }

    /**
     * Create inbound business logic process
     * @param consumer
     * @return
     */
    // private TProcess createBusinessLogicProcess( final MapFlow consumer ) {

    //     final String eventName = consumer.isDirectConsumer() ? consumer.getFlowDesignation() : consumer.getQueueListenerAddress();

    //     final TProcess businessLogicProcess = createGenericProcess(
    //         String.format(
    //             SapIflowUtils.PROCESS_INB_BL_NAME_TEMPLATE, 
    //             eventName
    //         ),
    //         String.format( SapIflowUtils.ACT_BL_START_NAME_TEMPLATE, eventName ), 
    //         String.format( SapIflowUtils.ACT_BL_END_NAME_TEMPLATE, eventName )
    //     );

    //     addExtensionProperties(businessLogicProcess, extConfigs.getCalledProcess().getProcessExtensions());
    //     addExtensionPropertiesToStartAndEndEvents(businessLogicProcess, extConfigs.getCalledProcess());

    //     final String mapToSchemaName = getSchemaName(consumer.getJsonSchemaReference());
    //     final String stubMapCa = String.format("Stub map from %s to destination format", mapToSchemaName);
    //     final TCallActivity stubMapCallActivity = 
    //         createInboundStubMapCallActivity(
    //                 stubMapCa, 
    //                 mapToSchemaName
    //         );
    //     addCallActivityBeforeEndEvent(businessLogicProcess, stubMapCallActivity);

    //     return businessLogicProcess;
    // }

    private TMessageFlow createHttpsInMessageFlow(
        final String sourceRef,
        final TProcess targetProcess
    ) {

        String targetRef = "";
        for ( JAXBElement<? extends TFlowElement> jaxbElt : targetProcess.getFlowElement() ) {
            if ( jaxbElt.getValue() instanceof TStartEvent ) {
                TStartEvent startEvent = ( TStartEvent )( jaxbElt.getValue() );
                targetRef = startEvent.getId();
                break;
            }
        }

        final String messageFlowName = "From_HTTPS_Input_" + messageFlowCounter++;
        final TMessageFlow httpsInMessageFlow = 
            createGenericMessageFlow(
                messageFlowName,
                sourceRef, 
                targetRef
        );
        addExtensionProperties(httpsInMessageFlow, extConfigs.getMessageFlow().getHttpSender());
        addExtensionProperty(httpsInMessageFlow, "Description", "Receive input from HTTPS adaptor");
        addExtensionProperty(httpsInMessageFlow, "Name", messageFlowName);
        addExtensionProperty(httpsInMessageFlow, "system", getParticipantNameById(sourceRef));

        return httpsInMessageFlow;
    }

    private TMessageFlow createSubscribeMessageFlow(
        final MapFlow consumer,
        final String sourceRef,
        final TProcess targetProcess
    ) {

        String targetRef = "";
        for ( JAXBElement<? extends TFlowElement> jaxbElt : targetProcess.getFlowElement() ) {
            if ( jaxbElt.getValue() instanceof TStartEvent ) {
                TStartEvent startEvent = ( TStartEvent )( jaxbElt.getValue() );
                targetRef = startEvent.getId();
                break;
            }
        }

        final String messageFlowName = "From_AdvancedEventMesh_" + messageFlowCounter++;
        final TMessageFlow subscribeMessageFlow = 
            createGenericMessageFlow(
                messageFlowName,
                sourceRef, 
                targetRef
        );
        addExtensionProperties(subscribeMessageFlow, extConfigs.getMessageFlow().getAllMessageFlows());
        addExtensionProperties(subscribeMessageFlow, extConfigs.getMessageFlow().getSubscription() );

        // TODO - Defaulting to 'AUTOMATIC_IMMEDIATE' because Solace IFlow connector does not support all modes
        // Determine proper mapping of ackMode for IFlow and implement
        addExtensionProperty(
            subscribeMessageFlow, 
            "ackMode", 
            "AUTOMATIC_ON_EXCHANGE_COMPLETE" );
            // ( consumer.isDirectConsumer() ? "AUTOMATIC_IMMEDIATE" : 
            //         ( ( consumer.getQueueListenerAckMode() != null && consumer.getQueueListenerAckMode().length() > 0 ) ?
            //             consumer.getQueueListenerAckMode() : "AUTOMATIC_IMMEDIATE" ) ) );
        addExtensionProperty(
            subscribeMessageFlow, 
            "consumerMode", 
            ( consumer.isDirectConsumer() ? "DIRECT" : "GUARANTEED" ) );
        String description;
        if ( consumer.isDirectConsumer() ) {
            StringBuilder topics = new StringBuilder();
            consumer.getDirectListenerTopics().forEach( t -> {
                topics.append("\n" + t);
            });
            description = String.format("Receive input messages from AEM on topic(s):%s", topics.toString());
        } else {
            description = String.format("Receive input messages from AEM on queue [%s]", consumer.getQueueListenerAddress());
        }
        addExtensionProperty( subscribeMessageFlow, "Description", description);
        addExtensionProperty( subscribeMessageFlow, "Name", messageFlowName );
        // addExtensionProperty( subscribeMessageFlow, "Name", "AdvancedEventMesh" );
        addExtensionProperty(
            subscribeMessageFlow, 
            "queueName", 
            ( consumer.isDirectConsumer() ? "" : consumer.getQueueListenerAddress() ) );

        // Add topic list as extension property
        final StringBuilder topicBuilder = new StringBuilder();
        if ( consumer.isDirectConsumer() && consumer.getDirectListenerTopics() != null ) {
            consumer.getDirectListenerTopics().forEach( topic -> {
                topicBuilder.append( "<row><cell id='listObjectValue'>" );
                topicBuilder.append( topic );
                topicBuilder.append( "</cell></row>");
            } );
        }
        addExtensionProperty( subscribeMessageFlow, "topicSubscriptions", topicBuilder.toString());

        return subscribeMessageFlow;
    }

    /**
     * Create output processes, one for each publisher in the input
     * @param publisher
     * @param endParticipantId
     */
    private void mapPublisherToIflow(
        final MapSubFlowEgress publisher, 
        final int index, 
        final String endParticipantId ) {

        final String eventName = publisher.getMessageName();

        log.debug("START - Create Sender and EventGenerator Processes for output event [{}]", eventName);

        // final TProcess senderProcess = createSenderProcess(publisher);
        final TProcess eventGeneratorProcess = createEventGeneratorProcess(publisher, index);

        // Add Call Activity from sender to 
        // addCallActivityBeforeEndEvent(senderProcess, 
        //     createCallActivityToProcess(
        //         String.format( SapIflowUtils.ACT_SEND_GENERATE_NAME_TEMPLATE, eventName ), 
        //         eventGeneratorProcess.getId()
        //     )
        // );

        // final TParticipant sendParticipant = createGenericParticipant( 
        //     senderProcess.getId(), 
        //     SapIflowUtils.PARTICIPANT_IFL_INT,
        //     senderProcess.getName(),
        //     senderProcess.getId() );

        final TParticipant eventGeneratorParticipant = createGenericParticipant(
            eventGeneratorProcess.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            eventGeneratorProcess.getName(),
            eventGeneratorProcess.getId() );

        // final TMessageFlow messageFlow = createPublishMessageFlow(
        //     publisher, 
        //     senderProcess,
        //     endParticipantId );

        // TODO - Should this be done here?
        // generateSequences( senderProcess );
        // generateSequences( eventGeneratorProcess );
        
        // TODO - Should this be done here?
        // processes.add( senderProcess );
        processes.add( eventGeneratorProcess );
        // participants.add( sendParticipant );
        participants.add( eventGeneratorParticipant );
        // messageFlows.add( messageFlow );

        outputParticipantsAndProcesses.add( new ParticipantAndProcess(eventGeneratorParticipant, eventGeneratorProcess) );
        // createBpmnShapesForParticipantAndProcess(sendParticipant, senderProcess);
        // createBpmnShapesForParticipantAndProcess(eventGeneratorParticipant, eventGeneratorProcess);
        log.debug("DONE - Create Sender and EventGenerator Processes for output event [{}]", eventName);
    }

    /**
     * Create Sender process
     * Connects to Event Mesh Receiver for publication
     * @param publisher
     * @return
     */
    // private TProcess createSenderProcess( final MapSubFlowEgress publisher ) {

    //     final String eventName = publisher.getMessageName();

    //     final TProcess senderProcess = createGenericProcess(
    //         String.format( SapIflowUtils.PROCESS_OUT_SEND_NAME_TEMPLATE, eventName ),
    //         SapIflowUtils.ACT_SEND_START_NAME, 
    //         String.format( SapIflowUtils.ACT_SEND_END_NAME_TEMPLATE, eventName ) );

    //     //
    //     addEmptyMessageEventDefinitions(senderProcess);     // Conform with samples
    //     addExtensionProperties(senderProcess, extConfigs.getOutboundProcess().getProcessExtensions());
    //     addExtensionPropertiesToStartAndEndEvents(senderProcess, extConfigs.getOutboundProcess());

    //     final String stubMapSchemaName = getSchemaName(publisher.getJsonSchemaReference());
    //     final TCallActivity stubMapCallActivity = createOutboundStubMapCallActivity(stubMapSchemaName, publisher.getMessageName() );
    //     addCallActivityBeforeEndEvent( senderProcess, stubMapCallActivity );

    //     return senderProcess;
    // }

    /**
     * Create Event Generator Process
     * Derives dynamic topic name and applies validation map to outbound message
     * @param publisher
     * @return
     */
    private TProcess createEventGeneratorProcess( final MapSubFlowEgress publisher, final int index ) {

        final String eventName = publisher.getMessageName();

        final TProcess eventGeneratorProcess = createGenericProcess(
            String.format( SapIflowUtils.PROCESS_OUT_GEN_NAME_TEMPLATE, eventName ), 
            String.format( SapIflowUtils.ACT_GEN_START_NAME_TEMPLATE, eventName ),
            String.format( SapIflowUtils.ACT_GEN_END_NAME_TEMPLATE, eventName )
        );
        //
        addExtensionProperties(eventGeneratorProcess, extConfigs.getCalledProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(eventGeneratorProcess, extConfigs.getCalledProcess());

        final String stubMapSchemaName = getSchemaName(publisher.getJsonSchemaReference());
        final TCallActivity stubMapCallActivity = createOutboundStubMapCallActivity(stubMapSchemaName, publisher.getMessageName() );
        addCallActivityBeforeEndEvent( eventGeneratorProcess, stubMapCallActivity );

        addCallActivityBeforeEndEvent(
            eventGeneratorProcess, 
            // createCallActivityDefineTopic(publisher.getPublishAddress(), List.copyOf(publisher.getSetVariables().keySet())));
            createCallActivityDefineTopic(publisher.getPublishAddress()));

        addCallActivityBeforeEndEvent(
            eventGeneratorProcess, 
            createCallActivityTopicParameters( publisher, index ));

        addCallActivityBeforeEndEvent(
            eventGeneratorProcess, 
            createCallActivityComposeTopic());

        final String validateSchemaName = getSchemaName(publisher.getJsonSchemaReference());
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
        // final MapSubFlowEgress publisher,
        final TProcess sourceProcess,
        final String targetRef ) {

        String sourceRef = "";
        for ( JAXBElement<? extends TFlowElement> jaxbElt : sourceProcess.getFlowElement() ) {
            if ( jaxbElt.getValue() instanceof TEndEvent ) {
                TEndEvent endEvent = ( TEndEvent )( jaxbElt.getValue() );
                sourceRef = endEvent.getId();
                break;
            }
        }

        final String messageFlowName = "To_AdvancedEventMesh_" + messageFlowCounter++;
        final TMessageFlow publishMessageFlow = 
            createGenericMessageFlow(
                messageFlowName,
                sourceRef, 
                targetRef
            );
        addExtensionProperties(publishMessageFlow, extConfigs.getMessageFlow().getAllMessageFlows());
        addExtensionProperties(publishMessageFlow, extConfigs.getMessageFlow().getPublication() );
        
        addExtensionProperty(publishMessageFlow, "deliveryMode", "PERSISTENT");
        // addExtensionProperty(publishMessageFlow, "Description", String.format("Publish Message [%s] to AEM on Topic [%s]", publisher.getMessageName() == null ? "?" : publisher.getMessageName(), publisher.getPublishAddress()));
        addExtensionProperty(publishMessageFlow, "Description", "Publish Message to Event Mesh");
        addExtensionProperty(publishMessageFlow, "Name", messageFlowName);

        return publishMessageFlow;
    }

    private TMessageFlow createHttpOutMessageFlow(
        final TProcess sourceProcess,
        final String targetRef
    ) {

        String sourceRef = "";
        for ( JAXBElement<? extends TFlowElement> jaxbElt : sourceProcess.getFlowElement() ) {
            if ( jaxbElt.getValue() instanceof TEndEvent ) {
                TEndEvent endEvent = ( TEndEvent )( jaxbElt.getValue() );
                sourceRef = endEvent.getId();
                break;
            }
        }

        final String messageFlowName = "To_HTTP_Server_" + messageFlowCounter++;
        final TMessageFlow httpOutMessageFlow = 
            createGenericMessageFlow(
                messageFlowName,
                sourceRef, 
                targetRef
            );
        addExtensionProperties(httpOutMessageFlow, extConfigs.getMessageFlow().getHttpReceiver());
        addExtensionProperty(httpOutMessageFlow, "Description", "HTTP adaptor to write output");
        addExtensionProperty(httpOutMessageFlow, "Name", messageFlowName);
        addExtensionProperty(httpOutMessageFlow, "system", getParticipantNameById(targetRef));

        return httpOutMessageFlow;
    }

    // private TCallActivity createCallActivityDefineTopic( final String topicPattern, final List<String> parameters ) {
    private TCallActivity createCallActivityDefineTopic( final String topicPattern ) {
        // final String COMPOSED_TOPIC_PARAMETER = "<row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>%s</cell><cell id='Default'></cell><cell id='Name'>gpath_composedTopicPattern</cell><cell id='Datatype'></cell></row>";
        // final String TOPIC_VAR_PARAMETER = "<row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>CHANGE.ME</cell><cell id='Default'></cell><cell id='Name'>gpath_topicexp_%s</cell><cell id='Datatype'></cell></row>";
        final String COMPOSED_TOPIC_PARAMETER = "<row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>%s</cell><cell id='Default'></cell><cell id='Name'>composedTopic</cell><cell id='Datatype'></cell></row>";
        final TCallActivity ca = createGenericCallActivity( "Set composedTopic Property" );

        String propertyTable = String.format(COMPOSED_TOPIC_PARAMETER, topicPattern);
        // for (String p : parameters ) {
        //     propertyTable += String.format(TOPIC_VAR_PARAMETER, p);
        // }
        addExtensionProperty(ca, "propertyTable", propertyTable);
        addExtensionProperties(ca, extConfigs.getCallActivity().getDefineTopic());

        return ca;
    }

    // private TCallActivity createCallActivityExtractParameters() {
    //     final TCallActivity ca = createGenericCallActivity( "Extract Parameters Script" );
    //     addExtensionProperties(ca, extConfigs.getCallActivity().getGroovyScript());
    //     addExtensionProperty(ca, "script", "extractField.groovy");
    //     return ca;
    // }

    private TCallActivity createCallActivityTopicParameters(final MapSubFlowEgress publisher, final int index) {
        if (publisher.getSetVariables() == null || publisher.getSetVariables().size() == 0) {
            return null;
        }
        final TCallActivity ca = createGenericCallActivity( "Set Dynamic Topic Parameters" );
        addExtensionProperties(ca, extConfigs.getCallActivity().getGroovyScript());
        addExtensionProperty(ca, "script", "topicParameters.groovy");
        addExtensionProperty(ca, "scriptFunction", "defineTopicParams_" + Integer.toString(index));
        return ca;
    }

    private TCallActivity createCallActivityComposeTopic() {
        final TCallActivity ca = createGenericCallActivity( "Compose Topic Script" );
        addExtensionProperties(ca, extConfigs.getCallActivity().getGroovyScript());
        addExtensionProperty(ca, "script", "composeTopic.groovy");
        addExtensionProperty(ca, "scriptFunction", "composeTopic");
        return ca;
    }

    // private TCallActivity createCallActivityExtractTopicVariable( final String variableName ) {
    //     final String EXTRACT_VARIABLE_TEMPLATE = "<row><cell>%s</cell><cell></cell><cell>expression</cell><cell>%s</cell><cell>local</cell></row>";
    //     final String extractVariableXml = String.format( EXTRACT_VARIABLE_TEMPLATE, variableName, ( "$." + variableName ) );
    //     final TCallActivity ca = createGenericCallActivity( SapIflowUtils.ACT_GEN_EXTRACT_TOPIC_PREFIX + variableName);
    //     addExtensionProperties(ca, extConfigs.getCallActivity().getTopicAssembly());
    //     addExtensionProperty( ca, "variable", extractVariableXml );
    //     return ca;
    // }

    // private TCallActivity createCallActivityGenerateComposedTopic( final String topicPattern ) {
    //     final TCallActivity ca = createGenericCallActivity( SapIflowUtils.ACT_GEN_COMPOSED_TOPIC_NAME );
    //     addExtensionProperties(ca, extConfigs.getCallActivity().getTopicAssembly());
    //     addExtensionProperty( ca, "variable", topicPattern );
    //     return ca;
    // }

    private TCallActivity createCallActivityToProcess( final String formattedName, final String processToCall ) {

        final TCallActivity ca = createGenericCallActivity(formattedName);
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
        final String schemaName,
        final String messageName
    ) {

        final String stubMapCaName = String.format("Map source format to schema %s for event %s", schemaName, messageName );
        final TCallActivity ca = createGenericCallActivity( stubMapCaName );
        final String mappingName = getOutboundStubMappingName(schemaName);
        addExtensionProperties(ca, extConfigs.getCallActivity().getMapping());
        addExtensionProperty(ca, "mappingname", mappingName);
        addExtensionProperty(ca, "mappingpath", getMappingPath(mappingName));
        addExtensionProperty(ca, "mappinguri", getMappingUri(mappingName));

        return ca;
    }

    private String getSchemaName( final String schemaReference ) {
        String schemaName;
        try {
            schemaName = inputSource.getSchemaMap().get( schemaReference ).getName();
        } catch ( NullPointerException npexc ) {
            schemaName = "SCHEMA_NOT_FOUND";
        }
        return schemaName;
    }

    private String getValidateMappingName( final String schemaName ) {
        return "Validate" + ( schemaName != null ? schemaName : "NULL" );
    }

    private String getInboundStubMappingName( final String schemaName ) {
        return ( schemaName != null ? schemaName : "NULL" ) + "ToDestinationFormat";
    }

    private String getOutboundStubMappingName( final String schemaName ) {
        return "SourceFormatTo" + ( schemaName != null ? schemaName : "NULL" );
    }

    private String getMappingPath( final String mappingName ) {
        return SapIflowUtils.MAPPING_PATH_TEMPLATE + ( mappingName != null ? mappingName : "NULL" );
    }

    private String getMappingUri( final String mappingName ) {
        return mappingName != null ? String.format( SapIflowUtils.MAPPING_URI_TEMPLATE, mappingName) : "NULL";
    }

    /**
     * Add extension properties from specified list to a BPMN element
     * @param target
     * @param properties
     */
    private void addExtensionProperties( 
        final TBaseElement target, 
        final List<SapIflowExtensionConfig.ExtProperty> properties )
    {
        if ( target.getExtensionElements() == null ) {
            target.setExtensionElements( bpmnFactory.createTExtensionElements() );
        }
        properties.forEach( prop -> {
            target.getExtensionElements().getAny().add(
                createPropertyInstance(
                    prop.getKey(), ( prop.getValue() == null ? "" : prop.getValue() ) 
                ) 
            );
        } );
    }

    private void addExtensionProperty( final TBaseElement target, final String key, final String value ) {
        if ( target.getExtensionElements() == null ) {
            target.setExtensionElements( bpmnFactory.createTExtensionElements() );
        }
        target.getExtensionElements().getAny().add( createPropertyInstance( key, value) );
    }

    private JAXBElement<TSapIflowProperty> createPropertyInstance( String key, String value ) {

        TSapIflowProperty property = iflFactory.createTSapIflowProperty();
        property.setKey(key);
        property.setValue(value);

        return iflFactory.createProperty(property);
    }

    private void addExtensionPropertiesToStartAndEndEvents( final TProcess process, final ProcessExt processExtProperties )  {
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

    /**
     * Call activities are created with StartEvent and EndEvent
     * This method inserts a call activity before the EndEvent
     * @param process
     * @param activity
     */
    private void addCallActivityBeforeEndEvent( final TProcess process, final TCallActivity activity ) {
        if ( activity == null ) {
            return;
        }
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
    private void generateSequences( final TProcess process ) {

        boolean firstItem = true;
        String  currentSeqFlowId = "", lastSeqFlowId = "";
        Object  lastEventObject = null;

        final List<TSequenceFlow> sequenceFlows = new ArrayList<>();

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

                final TSequenceFlow sf = bpmnFactory.createTSequenceFlow();
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

                final TSequenceFlow sf = bpmnFactory.createTSequenceFlow();
                sf.setId( lastSeqFlowId );
                sf.setSourceRef( lastEventObject );
                sf.setTargetRef( fe );
                sequenceFlows.add(sf);
            }
        }

        i = null;   // Avoid conflict
        sequenceFlows.forEach( sf -> {
            process.getFlowElement().add( bpmnFactory.createSequenceFlow(sf) );
        } );
    }

    private TCallActivity createGenericCallActivity( final String formattedName ) {
        final TCallActivity ca = bpmnFactory.createTCallActivity();
        ca.setId( SapIflowUtils.ACT_CALL_PREFIX + objectIncrementer++ );
        ca.setName(formattedName);
        return ca;
    }

    private TMessageFlow createGenericMessageFlow(
        final String name,
        final String sourceRef,
        final String targetRef
    ) {

        final TMessageFlow messageFlow = bpmnFactory.createTMessageFlow();
        messageFlow.setId( SapIflowUtils.MSGFLOW_ID_PREFIX + objectIncrementer++ );
        messageFlow.setName( name );
        messageFlow.setSourceRef( new QName( sourceRef ) );
        messageFlow.setTargetRef( new QName( targetRef ) );
        // addExtensionProperties(messageFlow, extConfigs.getMessageFlow().getAllMessageFlows() );

        return messageFlow;
    }

    private void addEmptyMessageEventDefinitions( final TProcess process ) {
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
        final String procName, 
        final String startEventName,
        final String endEventName ) {

        final long processId = objectIncrementer++;
        final long startEndEventId = objectIncrementer++;

        final TProcess process = bpmnFactory.createTProcess();
        final TStartEvent startEvent = bpmnFactory.createTStartEvent();
        final TEndEvent endEvent = bpmnFactory.createTEndEvent();

        process.setId( SapIflowUtils.PROCESS_ID_PREFIX + processId );
        process.setName( procName );

        startEvent.setId( SapIflowUtils.ACT_START_EVENT_PREFIX + startEndEventId );
        startEvent.setName( startEventName );

        endEvent.setId( SapIflowUtils.ACT_END_EVENT_PREFIX + startEndEventId );
        endEvent.setName( endEventName );

        process.getFlowElement().add( bpmnFactory.createStartEvent( startEvent ) );
        process.getFlowElement().add( bpmnFactory.createEndEvent( endEvent ) );

        return process;
    }

    private TParticipant createGenericParticipant( 
        final String type,
        final String name
    ) {
        return createGenericParticipant(
            String.valueOf( objectIncrementer++ ), 
            type, 
            name);
    }

    private TParticipant createGenericParticipant(
        final String processId,
        final String type,
        final String name
    ) {
        return createGenericParticipant(processId, type, name, null);
    }

    private TParticipant createGenericParticipant(
        final String processId,
        final String type,
        final String name,
        final String processRef
    ) {
        final TParticipant participant = bpmnFactory.createTParticipant();
        participant.setId( SapIflowUtils.PARTICIPANT_ID_PREFIX + processId );
        // TODO - IFL:TYPE
        participant.setName( name );
        if ( processRef != null ) {
            participant.setProcessRef( new QName( processRef) );
        }
        participant.getOtherAttributes().put( iflFactory.getSapIflowType_QName() , type );
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
        final BPMNShape shape = createGenericBPMNShape( participant.getId(), h, w, x, y );
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
        final BPMNShape shape = createGenericBPMNShape( flowElement.getId(), h, w, x, y );
        return shape;
    }

    private BPMNShape addBpmnShapeFromCallActivity( final TCallActivity callActivity, final double x, final double y) {
        final double h = BPMN_CALL_ACT_H, w = BPMN_CALL_ACT_W;
        final BPMNShape shape = createGenericBPMNShape(callActivity.getId(), h, w, x, y);
        return shape;
    }

    private BPMNShape addBpmnShapeFromParticipantProcess( 
        final TParticipant participant, final double x, final double y, final double w )
    {
        final double h = BPMN_PART_PROC_H;
        final BPMNShape shape = createGenericBPMNShape(participant.getId(), h, w, x, y);
        shapes.put(shape.getId(), shape);
        return shape;
    }

    private BPMNShape createGenericBPMNShape(
        final String id, final double h, final double w, final double x, final double y ) 
    {
        final BPMNShape shape = diFactory.createBPMNShape();
        shape.setBpmnElement( new QName( id ) );
        shape.setId( SapIflowUtils.BPMN_SHAPE_PREFIX + id );
        shape.setBounds( createBounds(h, w, x, y) );
        return shape;
    }

    private Bounds createBounds( final double h, final double w, final double x, final double y ) {
        final Bounds bounds = dcFactory.createBounds();
        bounds.setHeight( h );
        bounds.setWidth( w );
        bounds.setX(x);
        bounds.setY(y);
        return bounds;
    }

    private void updateBpmnShapePositionX(final BPMNShape bpmnShape, final double x) {
        bpmnShape.getBounds().setX(x);
    }

    private void createBpmnShapesForParticipantAndProcess( final TParticipant participant, final TProcess process ) {
    
        double xPos = processBoundaryX;

        for ( JAXBElement<? extends TFlowElement> jaxbElement : process.getFlowElement() ) {
            final TFlowElement elt = jaxbElement.getValue();
            if ( !(elt instanceof TStartEvent || elt instanceof TEndEvent || elt instanceof TCallActivity ) ) {
                continue;
            }
            final double x = getFlowEltX(xPos);
            final double y = getFlowEltY(elt, processBoundaryY);
            addBpmnShapeFromFlowElement(elt, x, y);
            xPos += advanceXPos(elt);
        }

        // Create BPMN Shapes for Sequence Flows
        // Can only do this after all of the shapes to join have been created
        process.getFlowElement().forEach( jaxbElement -> {
            final TFlowElement elt = jaxbElement.getValue();
            if ( (elt instanceof TSequenceFlow ) ) {
                TSequenceFlow sf = ( TSequenceFlow )elt;
                createBpmnEdgeFromSequenceFlow(sf);
            }
        } );

        addBpmnShapeFromParticipantProcess(participant, processBoundaryX, processBoundaryY, ( xPos - processBoundaryX + BPMN_FLOW_ELT_SEP_X) );

        processBoundaryY += BPMN_PART_PROC_H + BPMN_PROC_SEP_Y;
        processBoundaryMaxX = Math.max(xPos, processBoundaryMaxX);
    }

    private double getFlowEltY( final TFlowElement elt, final double boundaryY ) {
        double dimY = ( elt instanceof TCallActivity ? BPMN_CALL_ACT_H : BPMN_START_END_EVENT_H);
        return boundaryY + ( BPMN_PART_PROC_H / 2 ) - ( dimY / 2 );
    }

    private double getFlowEltX( final double boundaryX ) {
        return boundaryX + BPMN_FLOW_ELT_SEP_X;
    }

    private double advanceXPos( final TFlowElement elt ) {
        double advanceShapeX = ( elt instanceof TCallActivity ? BPMN_CALL_ACT_W : BPMN_START_END_EVENT_W ); 
        return advanceShapeX + BPMN_FLOW_ELT_SEP_X;
    }

    /**
     * BPMNEdge Handling methods
     * These methods are used to connect BPMNShape objects
     */

    private void createBpmnEdgeFromSequenceFlow( final TSequenceFlow sequenceFlow ) {
        final BPMNEdge edge = diFactory.createBPMNEdge();
        edge.setBpmnElement( new QName(sequenceFlow.getId()) );
        edge.setId( SapIflowUtils.BPMN_EDGE_PREFIX + sequenceFlow.getId() );
        final String bpmnSourceRefId = SapIflowUtils.BPMN_SHAPE_PREFIX + getSourceRefIdFromSequenceFlow(sequenceFlow);
        final String bpmnTargetRefId = SapIflowUtils.BPMN_SHAPE_PREFIX + getTargetRefIdFromSequenceFlow(sequenceFlow);
        edge.setSourceElement( new QName( bpmnSourceRefId ) );
        edge.setTargetElement( new QName( bpmnTargetRefId ) );

        final BPMNShape leftShape = shapes.get(bpmnSourceRefId);
        final BPMNShape rightShape = shapes.get(bpmnTargetRefId);
        connectShapesLeftToRight(edge, leftShape, rightShape);
        edges.add(edge);
    }

    private void createBpmnEdgeFromMessageFlow( final TMessageFlow messageFlow ) {
        final BPMNEdge edge = diFactory.createBPMNEdge();
        edge.setBpmnElement( new QName(messageFlow.getId()) );
        edge.setId( SapIflowUtils.BPMN_EDGE_PREFIX + messageFlow.getId() );
        final String bpmnSourceRefId = SapIflowUtils.BPMN_SHAPE_PREFIX + messageFlow.getSourceRef().getLocalPart();
        final String bpmnTargetRefId = SapIflowUtils.BPMN_SHAPE_PREFIX + messageFlow.getTargetRef().getLocalPart();
        edge.setSourceElement( new QName( bpmnSourceRefId ) );
        edge.setTargetElement( new QName( bpmnTargetRefId ) );

        final BPMNShape leftShape = shapes.get(bpmnSourceRefId);
        final BPMNShape rightShape = shapes.get(bpmnTargetRefId);
        connectShapesLeftToRight(edge, leftShape, rightShape);
        edges.add(edge);
    }

    private void connectShapesLeftToRight( final BPMNEdge edge, final BPMNShape leftShape, final BPMNShape rightShape ) {
        final double leftX = leftShape.getBounds().getX() + leftShape.getBounds().getWidth();
        final double leftY = leftShape.getBounds().getY() + ( leftShape.getBounds().getHeight() / 2 );
        final double rightX = rightShape.getBounds().getX();
        final double rightY = rightShape.getBounds().getY() + ( rightShape.getBounds().getHeight() / 2 );
        
        final Point leftPoint = dcFactory.createPoint();
        leftPoint.setX(leftX);
        leftPoint.setY(leftY);
        
        final Point rightPoint = dcFactory.createPoint();
        rightPoint.setX(rightX);
        rightPoint.setY(rightY);

        edge.getWaypoint().add(leftPoint);
        edge.getWaypoint().add(rightPoint);
    }

    private String getSourceRefIdFromSequenceFlow( final Object sequenceFlowObject ) {
        if ( sequenceFlowObject instanceof TSequenceFlow ) {
            TSequenceFlow sf = ( TSequenceFlow )sequenceFlowObject;
            return getReferencedObjectId(sf.getSourceRef());
        }
        return "";
    }

    private String getTargetRefIdFromSequenceFlow( final Object sequenceFlowObject ) {
        if ( sequenceFlowObject instanceof TSequenceFlow ) {
            TSequenceFlow sf = ( TSequenceFlow )sequenceFlowObject;
            return getReferencedObjectId(sf.getTargetRef());
        }
        return "";
    }

    private String getReferencedObjectId( final Object baseElementObject ) {
        if ( baseElementObject instanceof TBaseElement ) {
            TBaseElement baseElement = ( TBaseElement )baseElementObject;
            return baseElement.getId();
        }
        log.warn( "Object of class: [{}] is not valid to use as a reference" );
        return "";
    }
}
