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
import java.util.List;

import javax.xml.namespace.QName;

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

import com.solace.ep.muleflow.mapper.model.MapFlow;
import com.solace.ep.muleflow.mapper.model.MapMuleDoc;
import com.solace.ep.muleflow.mapper.model.MapSubFlowEgress;
import com.solace.ep.muleflow.mapper.sap.iflow.SapIflowExtensionConfig.ProcessExt;
import com.solace.ep.muleflow.mapper.sap.iflow.model.TSapIflowProperty;

import jakarta.xml.bind.JAXBElement;

public class SapIflowMapper {

    private long objectIncrementer = 1;

    private ObjectFactory bpmnFactory = new ObjectFactory();

    private com.solace.ep.muleflow.mapper.sap.iflow.model.ObjectFactory propFactory = new com.solace.ep.muleflow.mapper.sap.iflow.model.ObjectFactory();

    private List<TParticipant> participants = new ArrayList<>();

    private List<TProcess> processes = new ArrayList<>();

    private List<TMessageFlow> messageFlows = new ArrayList<>();

    private TDefinitions out = null;

    private SapIflowExtensionConfig extConfigs;

    public SapIflowMapper() throws Exception {
        extConfigs = SapIflowUtils.parseExtensionConfig( "src/main/resources/sap/iflow/extension-elements.yaml" );
    }
    
    // TODO - this is for testing
    public TDefinitions getOut() {
        return this.out;
    }

    // TODO - this is for testing
    public JAXBElement<TDefinitions> getJaxbOut() {
        return bpmnFactory.createDefinitions(this.out);
    }

    public void createSapIflow( MapMuleDoc input ) {

        String startParticipantId = "", endParticipantId = "";

        // Create input mesh participants (static)
        if ( input.getMapFlows().size() > 0 ) {
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
        }

        // Create output mesh participants (static)
        if ( input.getMapEgressSubFlows().size() > 0 ) {
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
        }

        // Create 
        for ( MapFlow ingress : input.getMapFlows() ) {
            mapIngressToIflow(ingress, startParticipantId);
        }

        for ( MapSubFlowEgress egress : input.getMapEgressSubFlows() ) {
            mapEgressToIflow(egress, endParticipantId);
        }

        TCollaboration collaboration = bpmnFactory.createTCollaboration();
        collaboration.setId( SapIflowUtils.COLLAB_ID_PREFIX + "1" );
        // TODO - Set collab name to App Name + Version
        collaboration.setName( SapIflowUtils.COLLAB_NAME_DEFAULT );
        addExtensionProperties(collaboration, extConfigs.getCollaboration());
        collaboration.getParticipant().addAll(participants);
        collaboration.getMessageFlow().addAll(messageFlows);

        TDefinitions definitions = bpmnFactory.createTDefinitions();
        definitions.setId( SapIflowUtils.DEFINITIONS_PREFIX + "1" );
        definitions.getRootElement().add( bpmnFactory.createCollaboration(collaboration) );
        processes.forEach( process -> {
            definitions.getRootElement().add( bpmnFactory.createProcess( process ) );
        });

        // TODO - This is for testing
        this.out = definitions;

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
            receiverProcess.getFlowElement().get(0).getValue().getId() );
        
        processes.add( receiverProcess );
        processes.add( businessLogicProcess );
        participants.add( inboundParticipantProc );
        participants.add( businessLogicParticipant );
        messageFlows.add( messageFlow );
        
    }

    private TProcess createReceiverProcess( MapFlow ingress ) {

        String eventName = ingress.isDirectConsumer() ? ingress.getFlowDesignation() : ingress.getQueueListenerAddress();

        TProcess receiverProcess = createGenericProcess(
            String.format(
                SapIflowUtils.PROCESS_INB_NAME_TEMPLATE, 
                eventName
            ),
            eventName, 
            SapIflowUtils.ACT_INB_END_NAME );

        //
        addExtensionProperties(receiverProcess, extConfigs.getInboundProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(receiverProcess, extConfigs.getInboundProcess());

        // TODO - update mapping name and path
        TCallActivity validateSchemaCallActivity = createValidateSchemaCallActivity(eventName, "mapping name", "mapping path" );
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

        addCallActivityBeforeEndEvent(
            businessLogicProcess, 
            createStubMapCallActivity( eventName, "mappingUri", "mappingPath")
        );

        return businessLogicProcess;
    }

    private TMessageFlow createSubscribeMessageFlow(
        MapFlow ingress,
        String sourceRef,
        String targetRef
    ) {
        //
        // final String topicCell = "<cell id='listObjectValue'>$$__SUBSCRIPTION_TOPIC__$$</cell>";
        //
        TMessageFlow subscribeMessageFlow = createGenericMessageFlow(ingress.getFlowDesignation(), sourceRef, targetRef);
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
        addExtensionProperty( subscribeMessageFlow, "Description", "");
        addExtensionProperty( subscribeMessageFlow, "direction", "Sender");
        addExtensionProperty( subscribeMessageFlow, "Name", ingress.getFlowDesignation() );
        addExtensionProperty(
            subscribeMessageFlow, 
            "queueName", 
            ( ingress.isDirectConsumer() ? "" : ingress.getQueueListenerAddress() ) );
        addExtensionProperty( subscribeMessageFlow, "system", "EventMeshSender");

        // Add topic list as extension property
        StringBuilder topicBuilder = new StringBuilder();
        if ( ingress.isDirectConsumer() && ingress.getDirectListenerTopics() != null ) {
            topicBuilder.append( "<row>" );
            for ( String topic : ingress.getDirectListenerTopics() ) {
                topicBuilder.append( "<cell id='listObjectValue'>" );
                topicBuilder.append( topic );
                topicBuilder.append( "</cell>");
            }
            topicBuilder.append( "</row>" );
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
            senderProcess.getFlowElement().get(0).getValue().getId(),
            endParticipantId );

        generateSequences( senderProcess );
        generateSequences( eventGeneratorProcess );
        
        processes.add( senderProcess );
        processes.add( eventGeneratorProcess );
        participants.add( sendParticipant );
        participants.add( eventGeneratoParticipant );
        messageFlows.add( messageFlow );
    }

    private TProcess createSenderProcess( MapSubFlowEgress egress ) {

        String eventName = egress.getMessageName();

        TProcess senderProcess = createGenericProcess(
            String.format( SapIflowUtils.PROCESS_OUT_SEND_NAME_TEMPLATE, eventName ),
            SapIflowUtils.ACT_SEND_START_NAME, 
            SapIflowUtils.ACT_SEND_END_NAME_TEMPLATE );

        //
        addExtensionProperties(senderProcess, extConfigs.getOutboundProcess().getProcessExtensions());
        addExtensionPropertiesToStartAndEndEvents(senderProcess, extConfigs.getOutboundProcess());

        // TODO - update mapping name and path
        TCallActivity stubMapCallActivity = createStubMapCallActivity(eventName, "mapping name", "mapping path" );
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

        // TODO - call topic variable extract from loop?
        addCallActivityBeforeEndEvent(
            eventGeneratorProcess,
            createCallActivityExtractTopicVariable("topicVariable"));

        // TODO - How to build composed topic?
        addCallActivityBeforeEndEvent(
            eventGeneratorProcess, 
            createCallActivityGenerateComposedTopic());

        // TODO - Mapping name/path?
        addCallActivityBeforeEndEvent(
            eventGeneratorProcess, 
            createValidateSchemaCallActivity(eventName, "mapping name", "")
        );

        return eventGeneratorProcess;
    }

    private TMessageFlow createPublishMessageFlow( 
        MapSubFlowEgress egress,
        String sourceRef,
        String targetRef ) {

        //
        TMessageFlow publishMessageFlow = createGenericMessageFlow(egress.getMessageName(), sourceRef, targetRef);
        addExtensionProperties(publishMessageFlow, extConfigs.getMessageFlow().getPublication() );
        
        addExtensionProperty(publishMessageFlow, "deliveryMode", "DIRECT");
        addExtensionProperty(publishMessageFlow, "Description", "");
        addExtensionProperty(publishMessageFlow, "Name", egress.getMessageName());
        addExtensionProperty(publishMessageFlow, "system", "EventMeshReceiver");

        return publishMessageFlow;
    }

    private TCallActivity createCallActivityExtractTopicVariable( String variableName ) {
        // TODO - How to create extract variable block?
        TCallActivity ca = createGenericCallActivity( SapIflowUtils.ACT_GEN_EXTRACT_TOPIC_PREFIX + variableName);
        addExtensionProperties(ca, extConfigs.getCallActivity().getTopicAssembly());
        addExtensionProperty( ca, "variable", variableName );
        return ca;
    }

    private TCallActivity createCallActivityGenerateComposedTopic() {
        // TODO - How to create composed topic block?
        TCallActivity ca = createGenericCallActivity( SapIflowUtils.ACT_GEN_COMPOSED_TOPIC_NAME );
        addExtensionProperties(ca, extConfigs.getCallActivity().getTopicAssembly());
        addExtensionProperty( ca, "variable", "value" );
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
        String name,
        String mappingName,
        String mappingPath ) {

        // TODO - Add mapping elements
        TCallActivity ca = createGenericCallActivity(String.format( SapIflowUtils.ACT_VALIDATE_SCHEMA_TEMPLATE, name ));
        addExtensionProperties(ca, extConfigs.getCallActivity().getMapping());
        return ca;
    }

    private TCallActivity createStubMapCallActivity(
        String name,
        String mappingUri,
        String mappingPath
    ) {

        // TODO - Make content type dynamic
        TCallActivity ca = createGenericCallActivity(String.format( SapIflowUtils.ACT_BL_STUBMAP_TEMPLATE, name, "JSON" ));
        addExtensionProperties(ca, extConfigs.getCallActivity().getMapping());
        return ca;
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

    private void addCallActivityBeforeEndEvent( TProcess process, TCallActivity activity ) {

        process.getFlowElement().add(
            process.getFlowElement().size() - 1, 
            bpmnFactory.createCallActivity(activity)
        );
    }

    private void addExtensionPropertiesToStartAndEndEvents( TProcess process, ProcessExt processExtProperties )  {
        for (JAXBElement<? extends TFlowElement> event : process.getFlowElement()) {
            if ( event.getValue().getClass() == TStartEvent.class ) {
                addExtensionProperties(event.getValue(), processExtProperties.getStartEvent());
                continue;
            }
            if ( event.getValue().getClass() == TEndEvent.class ) {
                addExtensionProperties(event.getValue(), processExtProperties.getEndEvent());
                break;
            }
        }
    }

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

                if ( fe.getClass() == TStartEvent.class ) {
                    TStartEvent se = ( TStartEvent )fe;
                    se.getOutgoing().add( new QName( currentSeqFlowId ) );
                }

                lastSeqFlowId = currentSeqFlowId;
                lastEventObject = fe;

                continue;
            }

            if ( i.hasNext() ) {

                currentSeqFlowId = SapIflowUtils.SEQ_FLOW_PREFIX + objectIncrementer++;

                if ( fe.getClass() == TCallActivity.class ) {
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

                if ( fe.getClass() == TEndEvent.class ) {
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
        addExtensionProperties(messageFlow, extConfigs.getMessageFlow().allMessageFlows );

        return messageFlow;
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
        endEvent.setId( SapIflowUtils.ACT_END_EVENT_PREFIX + startEndEventId );
        endEvent.setName( endEventName );

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

}
