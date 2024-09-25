
# Parallel Gateway example
Notes for development :-)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- <bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:ifl="http:///com.sap.ifl.model/Ifl.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="Definitions_1"> -->
<bpmn2:parallelGateway id="ParallelGateway_40" name="Sequential Multicast 1" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:ifl="http:///com.sap.ifl.model/Ifl.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <bpmn2:extensionElements>
        <ifl:property>
            <key>routingSequenceTable</key>
            <value>&lt;row&gt;&lt;cell&gt;1&lt;/cell&gt;&lt;cell&gt;SequenceFlow_41&lt;/cell&gt;&lt;/row&gt;&lt;row&gt;&lt;cell&gt;2&lt;/cell&gt;&lt;cell&gt;SequenceFlow_44&lt;/cell&gt;&lt;/row&gt;</value>
        </ifl:property>
        <ifl:property>
            <key>componentVersion</key>
            <value>1.1</value>
        </ifl:property>
        <ifl:property>
            <key>activityType</key>
            <value>SequentialMulticast</value>
        </ifl:property>
        <ifl:property>
            <key>cmdVariantUri</key>
            <value>ctype::FlowstepVariant/cname::SequentialMulticast/version::1.1.0</value>
        </ifl:property>
        <ifl:property>
            <key>subActivityType</key>
            <value>parallel</value>
        </ifl:property>
    </bpmn2:extensionElements>
    <bpmn2:incoming>SequenceFlow_26</bpmn2:incoming>
    <bpmn2:outgoing>SequenceFlow_44</bpmn2:outgoing>
    <bpmn2:outgoing>SequenceFlow_41</bpmn2:outgoing>
</bpmn2:parallelGateway>
```

## Sequence Flows for Sequential Multi-cast
```xml
<bpmn2:sequenceFlow id="SequenceFlow_44" name="Branch 2" sourceRef="ParallelGateway_40" targetRef="CallActivity_43"/>
<bpmn2:sequenceFlow id="SequenceFlow_41" name="Branch 1" sourceRef="ParallelGateway_40" targetRef="CallActivity_23"/>
<bpmn2:sequenceFlow id="SequenceFlow_24" sourceRef="StartEvent_4" targetRef="CallActivity_5"/>
<bpmn2:sequenceFlow id="SequenceFlow_27" sourceRef="CallActivity_23" targetRef="EndEvent_4"/>
<bpmn2:sequenceFlow id="SequenceFlow_45" sourceRef="CallActivity_43" targetRef="EndEvent_4"/>
<bpmn2:sequenceFlow id="SequenceFlow_25" sourceRef="CallActivity_5" targetRef="CallActivity_6"/>
<bpmn2:sequenceFlow id="SequenceFlow_26" sourceRef="CallActivity_6" targetRef="ParallelGateway_40"/>
```

## `routingSequenceTable` extension property for Sequential Multi-cast
**Property Name** = `routingSequenceTable`

### Serialized Example
```
&lt;row&gt;&lt;cell&gt;1&lt;/cell&gt;&lt;cell&gt;SequenceFlow_41&lt;/cell&gt;&lt;/row&gt;&lt;row&gt;&lt;cell&gt;2&lt;/cell&gt;&lt;cell&gt;SequenceFlow_44&lt;/cell&gt;&lt;/row&gt;
```
### Example routingSequenceTable property
```xml
<row>
    <cell>1</cell>
    <cell>SequenceFlow_41</cell>
</row>
<row>
    <cell>2</cell>
    <cell>SequenceFlow_44</cell>
</row>
```
