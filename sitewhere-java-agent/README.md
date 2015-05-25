SiteWhere Java Agent
=====================
The SiteWhere Java agent provides a base client platform which runs on any 
device that supports Java. The agent allows a device to interact with
SiteWhere over the MQTT transport by sending and receiving messages encoded
in a `Google Protocol Buffers <https://developers.google.com/protocol-buffers/>`_ format. The agent supports dynamic 
device registration and sending data events such as measurements, locations,
and alerts to SiteWhere. It also supports receiving commands from SiteWhere
and triggering Java logic based on the requests.
