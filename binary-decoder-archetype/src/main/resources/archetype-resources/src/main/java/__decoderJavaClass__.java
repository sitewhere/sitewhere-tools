#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sitewhere.rest.model.device.event.DeviceLocation;
import com.sitewhere.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.sitewhere.rest.model.device.provisioning.DecodedDeviceEventRequest;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.provisioning.IDecodedDeviceEventRequest;
import com.sitewhere.spi.device.provisioning.IDeviceEventDecoder;

/**
 * Custom binary decoder.
 */
public class ${decoderJavaClass} implements IDeviceEventDecoder<byte[]> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.device.provisioning.IDeviceEventDecoder#decode(java.lang.Object)
	 */
	@Override
	public List<IDecodedDeviceEventRequest> decode(byte[] payload) throws SiteWhereException {
		List<IDecodedDeviceEventRequest> events = new ArrayList<IDecodedDeviceEventRequest>();
		events.add(decodeLocation(payload));
		return events;
	}

	/**
	 * Decode a {@link DeviceLocation} from a binary payload.
	 * 
	 * @param payload
	 * @return
	 * @throws SiteWhereException
	 */
	protected IDecodedDeviceEventRequest decodeLocation(byte[] payload) throws SiteWhereException {
		DecodedDeviceEventRequest request = new DecodedDeviceEventRequest();
		request.setHardwareId(parseHardwareId(payload));
		request.setOriginator(parseOriginator(payload));

		// Values below would be decoded from the binary payload.
		DeviceLocationCreateRequest location = new DeviceLocationCreateRequest();
		location.setLatitude(33.7550);
		location.setLongitude(-84.3900);
		location.setElevation(333.0);
		location.setEventDate(new Date());
		location.addOrReplaceMetadata("method", "gps");

		return request;
	}

	/**
	 * Parse hardware id from binary payload. The hardware id is the unique identifier
	 * that SiteWhere uses to reference a device. Hardware id is required.
	 * 
	 * @param payload
	 * @return
	 */
	protected String parseHardwareId(byte[] payload) {
		return "parsed-hardware-id"; // Insert your own parse logic here.
	}

	/**
	 * Parse originator from the binary payload. The originator is the event id of a
	 * SiteWhere event that caused this event to fire. It is useful for linking responses
	 * to SiteWhere commands. Originator may be null.
	 * 
	 * @param payload
	 * @return
	 */
	protected String parseOriginator(byte[] payload) {
		return null;
	}
}