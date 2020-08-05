package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JSONParserGeneralTest {

	//@formatter:off
	private static final Map<String, JSONTestFileDefinition> TEST_PARSING = makeMap(
			"test_general/001.json", new JSONTestFileDefinition(Map.of(
			"code", (byte)0,
		    "rid", "0",
		    "data", Map.of(
		    	"subid", "-7766883411351472375",
		        "data", Map.of(
		            "region", Map.of(
		                "123", Map.of(
		                    "alias", "Europe",
		                    "game", Map.of(
		                        "11811809", Map.of(
		                            "id", (int)11811809,
		                            "team1_name", "Zorya Luhansk",
		                            "team2_name", "SC Braga",
		                            "market", Map.of(
		                                "188597332", Map.of(
		                                    "type", "P1XP2",
		                                    "name", "Ganador del Partido",
		                                    "event", Map.of(
		                                        "624566458", Map.of(
		                                            "price", 2.39f,
		                                            "name", "W1"
		                                        ),
		                                        "624566459", Map.of(
		                                            "price", 3.01f,
		                                            "name", "X"
		                                        ),
		                                        "624566460", Map.of(
		                                            "price", 2.82f,
		                                            "name", "W2"
		                                        )
		                                    )
		                                )
		                            )
		                        ),
		                        "11811810", Map.of(
		                            "id", (int)11811810,
		                            "team1_name", "Olympiacos Piraeus",
		                            "team2_name", "FC Luzern",
		                            "market", Map.of(
		                                "188597340", Map.of(
		                                    "type", "P1XP2",
		                                    "name", "Ganador del Partido",
		                                    "event", Map.of(
		                                        "624566476", Map.of(
		                                            "price", 1.34f,
		                                            "name", "W1"
		                                        ),
		                                        "624566477", Map.of(
		                                            "price", 4.29f,
		                                            "name", "X"
		                                        ),
		                                        "624566478", Map.of(
		                                            "price", 7.92f,
		                                            "name", "W2"
		                                        )
		                                    )
		                                )
		                            )
		                        ),
		                        "11844220", Map.of(
		                            "id", 11844220,
		                            "team1_name", "NK Domzale",
		                            "team2_name", "FC Ufa",
		                            "market", Map.of(
		                                "189338624", Map.of(
		                                    "type", "P1XP2",
		                                    "name", "Ganador del Partido",
		                                    "event", Map.of(
		                                        "626913821", Map.of(
		                                            "price", 2.35f,
		                                            "name", "W1"
		                                        ),
		                                        "626913822", Map.of(
		                                            "price", 2.86f,
		                                            "name", List.of("X", "Y", "Z")
		                                        ),
		                                        "626913823", Map.of(
		                                            "price", 3.03f,
		                                            "name", "W2"
		                                        )
		                                    )
		                                )
		                            )
		                        )
		                    )
		                )
		            )
		        )
		    )
		)),
		"test_general/002.json", new JSONTestFileDefinition(List.of(
		         Map.of("Firstname", "John", "Lastname", "Smith", "City", "Boston", "State", "MA", "Children",
		        		 List.of( Map.of("Name", "Callie","Age", (byte)5), Map.of("Name", "Griffin", "Age", (byte)3), Map.of("Name", "Luke", "Age", (byte)1))),
		         Map.of("Firstname", "Henry", "Lastname", "Rhodes", "City", "New York", "State", "NY", "Children",
		        		 List.of( Map.of("Name", "Howard", "Age", (byte)15), Map.of("Name", "Robert", "Age", (byte)11))),
		         Map.of("Firstname", "Allison", "Lastname", "Berman", "City", "Los Angeles", "State", "CA", "Children",
		         		 List.of( Map.of("Name", "Jeff", "Age", (byte)35), Map.of("Name", "Roxanne", "Age", (byte)33), Map.of("Name", "Claudia", "Age", (byte)31), Map.of("Name", "Denzel", "Age", (byte)11)))
		         		))
	);
	//@formatter:on

	BSONParser bsonParser = new BSONParser();
	JSONParser jsonParser = new JSONParser();

	@Test
	void testGeneral() {

		TEST_PARSING.entrySet().stream().forEach(e -> testEachCase(e.getKey(), e.getValue()));
	}

	void testEachCase(String key, JSONTestFileDefinition def) {

		try (BufferedInputStream bis = new BufferedInputStream(
				this.getClass().getClassLoader().getResourceAsStream(key))) {
			String jsonText = new String(bis.readAllBytes());
			try {
				BSON b = jsonParser.parseJSONString(jsonText);
				if (b.getType() == BSON.ARRAY) {
					List<Object> list = b.getAsList(jsonParser.getKeySubstitutor());
					assertEquals(def.getValue(), list);
				} else if (b.getType() == BSON.OBJECT) {
					assertTrue(mapEqual(def.getValue(), (Object) b.getAsMap(jsonParser.getKeySubstitutor())));
				} else {
					assertEquals(def.getValue(), b.getValue());
				}
			} catch (Exception ex) {
				if (def.isException()) {
					assertEquals(def.getValue(), ex.getMessage());
				} else {
					System.err.println(key);
					System.err.println(jsonText);
					System.err.println("--------------------\n");
					ex.printStackTrace();
					throw new RuntimeException(ex);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean mapEqual(Object src, Object dest) {

		Map<String, Object> a = (Map<String, Object>) src;
		Map<String, Object> b = (Map<String, Object>) dest;

		assertEquals(a.keySet(), b.keySet());

		for (String key : a.keySet()) {

			if (a.get(key) instanceof Map && !mapEqual(a.get(key), b.get(key)))
				return false;
			assertEquals(a.get(key), b.get(key));
		}

		return true;
	}

	private static Map<String, JSONTestFileDefinition> makeMap(Object... objects) {

		Map<String, JSONTestFileDefinition> map = new LinkedHashMap<>();

		for (int i = 0; i < objects.length; i += 2) {
			map.put((String) objects[i], (JSONTestFileDefinition) objects[i + 1]);
		}

		return map;
	}
}
