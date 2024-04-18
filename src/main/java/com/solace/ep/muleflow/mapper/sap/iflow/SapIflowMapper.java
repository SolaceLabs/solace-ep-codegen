package com.solace.ep.muleflow.mapper.sap.iflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.di.BPMNEdge;
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
import org.omg.spec.bpmn._20100524.model.TProcessType;
import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
import org.omg.spec.bpmn._20100524.model.TStartEvent;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.ep.muleflow.mapper.model.MapFlow;
import com.solace.ep.muleflow.mapper.model.MapMuleDoc;
import com.solace.ep.muleflow.mapper.model.MapSubFlowEgress;
import com.solace.ep.muleflow.mapper.sap.iflow.model.TSapIflowProperty;
import com.solace.ep.muleflow.mule.model.core.MuleFlow;

import jakarta.xml.bind.JAXBElement;

public class SapIflowMapper {

    private long objectIncrementer = 1;

    private ObjectFactory bpmnFactory = new ObjectFactory();

    private com.solace.ep.muleflow.mapper.sap.iflow.model.ObjectFactory propFactory = new com.solace.ep.muleflow.mapper.sap.iflow.model.ObjectFactory();

    private List<TParticipant> participants = new ArrayList<>();

    private List<TProcess> processes = new ArrayList<>();

    private List<TMessageFlow> messageFlows = new ArrayList<>();

    private List<BPMNShape> shapes = new ArrayList<>();

    private List<BPMNEdge> sequenceFlows = new ArrayList<>();

    private List<BPMNEdge> flows = new ArrayList<>();

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

        if ( input.getMapFlows().size() > 0 ) {
            TParticipant eventMeshSender = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SEND );

            TParticipant sourceSystem = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_SEND, 
                SapIflowUtils.PARTICIPANT_NAME_SRC );
            
            startParticipantId = eventMeshSender.getId();

            participants.add(eventMeshSender);
            participants.add(sourceSystem);
        }

        if ( input.getMapEgressSubFlows().size() > 0 ) {
            TParticipant eventMeshReceiver = createGenericParticipant( 
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_RECV );

            TParticipant destinationSystemReceiver = createGenericParticipant(
                SapIflowUtils.PARTICIPANT_IFL_RECV, 
                SapIflowUtils.PARTICIPANT_NAME_DEST );
            
            endParticipantId = eventMeshReceiver.getId();

            participants.add(eventMeshReceiver);
            participants.add(destinationSystemReceiver);
        }

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
        collaboration.getParticipant().addAll(participants);
        collaboration.getMessageFlow().addAll(messageFlows);

        TDefinitions definitions = bpmnFactory.createTDefinitions();
        definitions.setId( SapIflowUtils.DEFINITIONS_PREFIX + "1" );
        definitions.getRootElement().add( bpmnFactory.createCollaboration(collaboration) );
        for ( TProcess p : processes ) {
            definitions.getRootElement().add( bpmnFactory.createProcess(p) );
        }

        // TODO - This is for testing
        this.out = definitions;

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

    private TCallActivity createCallActivityToProcess( String formattedName, String processToCall ) {

        TCallActivity ca = createGenericCallActivity(formattedName);
        // TODO Add process ref elements
        return ca;
    }

    private TCallActivity createValidateSchemaCallActivity( 
        String name,
        String mappingName,
        String mappingPath ) {

        // TODO - Add mapping elements
        TCallActivity ca = createGenericCallActivity(String.format( SapIflowUtils.ACT_VALIDATE_SCHEMA_TEMPLATE, name ));
        return ca;
    }

    private TCallActivity createStubMapCallActivity(
        String name,
        String mappingUri,
        String mappingPath
    ) {

        // TODO - Make content type dynamic
        TCallActivity ca = createGenericCallActivity(String.format( SapIflowUtils.ACT_BL_STUBMAP_TEMPLATE, name, "JSON" ));
        // TODO - Add stub map extension elements

        return ca;
    }

    private void mapEgressToIflow( MapSubFlowEgress egress, String endParticipantId ) {

        String eventName = egress.getMessageName();

        TProcess sender = createGenericProcess(
            String.format( SapIflowUtils.PROCESS_OUT_SEND_NAME_TEMPLATE, eventName ),
            SapIflowUtils.ACT_SEND_START_NAME, 
            String.format( SapIflowUtils.ACT_SEND_END_NAME_TEMPLATE, eventName ) );

        TProcess eventGenerator = createGenericProcess(
            String.format(
                SapIflowUtils.PROCESS_OUT_GEN_NAME_TEMPLATE, 
                eventName
            ), 
            String.format( SapIflowUtils.ACT_GEN_START_NAME_TEMPLATE, eventName ),
            String.format( SapIflowUtils.ACT_GEN_END_NAME_TEMPLATE, eventName )
        );

        addCallActivityBeforeEndEvent(sender, 
            createCallActivityToProcess(
                String.format( SapIflowUtils.ACT_SEND_GENERATE_NAME_TEMPLATE, eventName ), eventGenerator.getId()) );

        TParticipant sendParticipant = createGenericParticipant( 
            sender.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            sender.getName(),
            sender.getId() );

        TParticipant eventGeneratoParticipant = createGenericParticipant(
            eventGenerator.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            eventGenerator.getName(),
            eventGenerator.getId() );

        TMessageFlow messageFlow = createGenericMessageFlow(
            eventName, 
            sender.getFlowElement().get(0).getValue().getId(),
            endParticipantId );
        addExtensionProperties(messageFlow, extConfigs.getMessageFlow().getPublication() );

        generateSequences( sender );
        generateSequences( eventGenerator );
        
        processes.add( sender );
        processes.add( eventGenerator );
        participants.add( sendParticipant );
        participants.add( eventGeneratoParticipant );
        messageFlows.add( messageFlow );
    }

    private void mapIngressToIflow( MapFlow ingress, String startParticipantId ) {

        String eventName = ingress.isDirectConsumer() ? ingress.getFlowDesignation() : ingress.getQueueListenerAddress();

        TProcess receiver = createGenericProcess(
            String.format(
                SapIflowUtils.PROCESS_INB_NAME_TEMPLATE, 
                eventName
            ),
            eventName, 
            SapIflowUtils.ACT_INB_END_NAME );

        TProcess businessLogic = createGenericProcess(
            String.format(
                SapIflowUtils.PROCESS_INB_BL_NAME_TEMPLATE, 
                eventName
            ), 
            String.format( SapIflowUtils.ACT_BL_START_NAME_TEMPLATE, eventName ),
            String.format( SapIflowUtils.ACT_BL_END_NAME_TEMPLATE, eventName )
        );

        addCallActivityBeforeEndEvent( 
            receiver, 
            createValidateSchemaCallActivity(eventName, "mapping name", "mapping path" )
        );
        addCallActivityBeforeEndEvent(
            receiver, 
            createCallActivityToProcess( String.format( SapIflowUtils.ACT_BL_TEMPLATE_NAME, eventName ), businessLogic.getId() )
        );
        addCallActivityBeforeEndEvent(
            businessLogic, 
            createStubMapCallActivity( eventName, "mappingUri", "mappingPath")
        );

        generateSequences( receiver );
        generateSequences( businessLogic );

        TParticipant inboundParticipantProc = createGenericParticipant( 
            receiver.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            receiver.getName(),
            receiver.getId() );

        TParticipant businessLogicParticipant = createGenericParticipant(
            businessLogic.getId(), 
            SapIflowUtils.PARTICIPANT_IFL_INT,
            businessLogic.getName(),
            businessLogic.getId() );
        
        TMessageFlow messageFlow = createGenericMessageFlow(
            eventName, 
            startParticipantId, 
        receiver.getFlowElement().get(0).getValue().getId() );
        addExtensionProperties(messageFlow, extConfigs.getMessageFlow().getSubscription() );
        
        processes.add( receiver );
        processes.add( businessLogic );
        participants.add( inboundParticipantProc );
        participants.add( businessLogicParticipant );
        messageFlows.add( messageFlow );
        
    }

    private void addExtensionProperties( TBaseElement target, List<SapIflowExtensionConfig.ExtProperty> properties ) {
        if ( target.getExtensionElements() == null ) {
            target.setExtensionElements( bpmnFactory.createTExtensionElements() );
        }
        for ( SapIflowExtensionConfig.ExtProperty prop : properties ) {
            target.getExtensionElements().getAny().add( createPropertyInstance( prop.getKey(), ( prop.getValue() == null ? "" : prop.getValue() ) ) );
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

    private TProcess createGenericProcess( 
        String procName, 
        String startEventName,
        String endEventName ) {

        final long processId = objectIncrementer++;
        final long startEndEventId = objectIncrementer++;

        //
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

        return participant;
    }

}
