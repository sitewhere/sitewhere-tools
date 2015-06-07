#SiteWhere Java Agent
The SiteWhere Java agent provides a base client platform which runs on any 
device that supports Java. The agent allows a device to interact with
SiteWhere over the MQTT transport by sending and receiving messages encoded
in a [Google Protocol Buffers] (https://developers.google.com/protocol-buffers/) format. The agent supports dynamic 
device registration and sending data events such as measurements, locations,
and alerts to SiteWhere. It also supports receiving commands from SiteWhere
and triggering Java logic based on the requests.

##Agent Usage Example
The agent project includes an example that shows how round-trip processing
is accomplished for a test device. The device registers itself as a Raspberry Pi
based on the specification token provided in the SiteWhere sample data.
Once registered, it waits for commands from SiteWhere and sends data events
in response to the commands.

###Configuring SiteWhere Command Processing
The default configuration for SiteWhere needs to be changed in order to support
sending commands to the agent. Specifically, a routing rule needs to be added
so that commands for Raspberry Pi devices are encoded and routed properly.
The following sections need to be updated:

```XML
	<!-- Device command routing -->
	<sw:command-routing>
		<sw:specification-mapping-router defaultDestination="default">
			<sw:mapping specification="7dfd6d63-5e8d-4380-be04-fc5c73801dfb"
				destination="raspberry-pi"/>
		</sw:specification-mapping-router>
	</sw:command-routing>
	
	<!-- Outbound command destinations -->
	<sw:command-destinations>

		<!-- Delivers commands via MQTT -->
		<sw:mqtt-command-destination destinationId="default"
			hostname="localhost" port="1883">
			<sw:protobuf-command-encoder/>
			<sw:hardware-id-topic-extractor commandTopicExpr="SiteWhere/commands/%s"
				systemTopicExpr="SiteWhere/system/%s"/>
		</sw:mqtt-command-destination>

		<!-- Raspberry Pi Java agent uses hybrid encoder -->
		<sw:mqtt-command-destination destinationId="raspberry-pi"
			hostname="localhost" port="1883">
			<sw:java-protobuf-hybrid-encoder/>
			<sw:hardware-id-topic-extractor commandTopicExpr="SiteWhere/commands/%s"
				systemTopicExpr="SiteWhere/system/%s"/>
		</sw:mqtt-command-destination>

	</sw:command-destinations>
```

After updating the configuration, restart the SiteWhere server and it should be
ready to send commands to the agent.

###Running the Example
The agent project includes a jar file with the compiled code from the project including
the required dependencies. To run the example agent, download (or build) the jar file
and copy the example configuration file from the **config** directory. The contents
are in the standard Java properties file format and will be similar to the values
below:

```INI
mqtt.hostname=localhost
command.processor.classname=com.example.ExampleCommandProcessor
device.hardware.id=123-TEST-439829343897429
device.specification.token=7dfd6d63-5e8d-4380-be04-fc5c73801dfb
```

If you are using a cloud instance for SiteWhere, edit the MQTT hostname to correspond to 
the IP address or hostname of the remote instance. You can also change the hardware id
to the value the device should be registered under in SiteWhere. The device specification
token indicates the type of hardware the device uses. The default value corresponds to
the Raspberry Pi specification in the default SiteWhere sample data.

Start the agent by entering:

    java -jar sitewhere-java-agent-x.y.z.jar
    
Note that **z.y.z** above should be replaced by the version number of the agent. The
agent should start and the logs produced in the console will reflect that the device
has registered with SiteWhere successfully. The next step is to send a command from
SiteWhere to affect the device.

Open the SiteWhere administrative application, log in, then click the green arrow
next to the first site in the list. A Raspberry Pi device should appear at the top 
of the assignments list along with the label **Unassociated Device**, which 
indicates it has not been associated with an asset. To send a command to the 
agent from SiteWhere, click the green arrow on the device assignment and open
the **Command Invocations** tab. Click the **Invoke Command** button and choose
**Ping** from the list of commands and **Invoke** to invoke it. A new command
invocation will show up at the top of the list and there should be output in the
agent log indicating that it sent a response to the command. To see the response
that was sent from the agent, click the icon to the right of the command invocation
in the administrative interface to view the invocation.	Click the **Responses**
tab and there should be a response of **'Acknowledged'**.

At this point the device has registered with SiteWhere, SiteWhere has sent a command
which executed Java code on the device, and the device sent a response to SiteWhere 
which was correlated with the original command.

###Building the Example
The example agent is written in Java and may be compiled and packaged using 
[Maven] (https://maven.apache.org/). Execute the following command to build and
package the agent:

    mvn clean install
    
The results of the build are located in the **target** folder under the root. The jar
file will be named **sitewhere-java-agent-x.y.z.jar** (where x.y.z is the version).
Once built, the jar can be used as mentioned in the previous section to run the agent.
