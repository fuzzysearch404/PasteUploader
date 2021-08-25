package uploader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.Main;

/**
 * Utility class for parsing the response JSON with Jackson.
 * 
 * @author Roberts Ziedins
 *
 */
public class JSONParser {

	private final ObjectMapper MAPPER = new ObjectMapper();
	private JsonNode jsonNode;
	
	public JSONParser(String jsonString) throws JsonMappingException, JsonProcessingException {
		jsonNode = MAPPER.readTree(jsonString);
	}
	
	public String beautifyJSON() {
		return jsonNode.toPrettyString();
	}
	
	public String getJSONKeyValue() {
		return jsonNode.path(Main.copyJSONFieldName).asText();
	}

}
