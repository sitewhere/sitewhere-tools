SiteWhere Java Agent
=====================
The SiteWhere Java agent provides a base client platform which runs on any 
device that supports Java. The agent allows a device to interact with
SiteWhere over the MQTT transport by sending and receiving messages encoded
in a [Google Protocol Buffers] (https://developers.google.com/protocol-buffers/) format. The agent supports dynamic 
device registration and sending data events such as measurements, locations,
and alerts to SiteWhere. It also supports receiving commands from SiteWhere
and triggering Java logic based on the requests.

Agent Usage Example
-------------------
The agent project includes an example that shows how round-trip processing
is accomplished for a test device. The device registers itself as a Raspberry Pi
based on the specification token provided in the SiteWhere sample data.
Once registered, it waits for commands from SiteWhere and sends data events
in response to the commands.

Running the Example
-------------------
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
agent should start	