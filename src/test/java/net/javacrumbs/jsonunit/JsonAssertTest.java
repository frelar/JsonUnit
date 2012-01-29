/**
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

public class JsonAssertTest {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void testEquals() {
		assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n}");
		assertJsonEquals("{\"foo\":\"bar\",\"test\": 1}", "{\n\"test\": 1,\n\"foo\":\"bar\"}");
		assertJsonEquals("{}", "{}");
	}
	@Test
	public void testEqualsNode() throws IOException {
		assertJsonEquals(MAPPER.readValue("{\"test\":1}", ObjectNode.class) , MAPPER.readValue("{\"test\": 1}", ObjectNode.class));
	}
	@Test
	public void testEqualsNodeFail() throws IOException {
		try {
			assertJsonEquals(MAPPER.readValue("{\"test\":1}", ObjectNode.class) , MAPPER.readValue("{\"test\": 2}", ObjectNode.class));
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidJsonActual() {
		assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n");
	}
	
	@Test
	public void testObjectName() {
		try {
			assertJsonEquals("{\"test\":1}", "{\n\"foo\": 1\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent keys found in node \"\". Expected [test], got [foo].\n", e.getMessage());
		}
	}
	
	@Test
	public void testExtraRootKey() {
		try {
			assertJsonEquals("{\"test\":1}", "{\n\"test\": 1\n, \"foo\": 2}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent keys found in node \"\". Expected [test], got [foo, test].\n", e.getMessage());
		}
	}
	
	@Test
	public void testMissingRootKey() {
		try {
			assertJsonEquals("{\"test\":1, \"foo\": 2}", "{\n\"test\": 1\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent keys found in node \"\". Expected [foo, test], got [test].\n", e.getMessage());
		}
	}
	
	@Test
	public void testDifferentNumericValue() {
		try {
			assertJsonEquals("{\"test\":1}", "{\n\"test\": 2\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 2.\n", e.getMessage());
		}
	}
	
	@Test
	public void testDifferentBooleanValue() {
		try {
			assertJsonEquals("{\"test\":true}", "{\n\"test\": false\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected true, got false.\n", e.getMessage());
		}
	}
	
	@Test
	public void testSameArrayValue() {
		assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2, 3]\n}");
	}

	@Test
	public void testDifferentArrayLength() {
		try {
			assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2]\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nArray \"test\" has different length. Expected 3, got 2.\n", e.getMessage());
		}
	}

	@Test
	public void testDifferentArrayValue() {
		try {
			assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, 2, 5]\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent value found in node \"test[2]\". Expected 3, got 5.\n", e.getMessage());
		}
	}
	@Test
	public void testDifferentArrayType() {
		try {
			assertJsonEquals("{\"test\":[1, 2, 3]}", "{\n\"test\": [1, false, 3]\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent types found in node \"test[1]\". Expected NUMBER, got BOOLEAN.\n", e.getMessage());
		}
	}
	
	@Test
	public void testComplexOk() {
			assertJsonEquals("{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false}", 
					"{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false}");
		
	}

	@Test
	public void testComplexErrors() {
		try {
			assertJsonEquals("{\"test\":[1, 2, {\"child\":{\"value1\":1, \"value2\":true, \"value3\": \"test\", \"value4\":{\"leaf\":5}}}], \"root2\": false, \"root3\" :1}", 
				"{\"test\":[5, false, {\"child\":{\"value1\":5, \"value2\":\"true\", \"value3\": \"test\", \"value4\":{\"leaf2\":5}}, \"child2\":{}}], \"root4\": \"bar\"}");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\n" + 
					"Different keys found in node \"\". Expected [root2, root3, test], got [root4, test].\n" + 
					"Different value found in node \"test[0]\". Expected 1, got 5.\n" + 
					"Different types found in node \"test[1]\". Expected NUMBER, got BOOLEAN.\n" + 
					"Different keys found in node \"test[2]\". Expected [child], got [child, child2].\n" + 
					"Different value found in node \"test[2].child.value1\". Expected 1, got 5.\n" + 
					"Different types found in node \"test[2].child.value2\". Expected BOOLEAN, got STRING.\n" + 
					"Different keys found in node \"test[2].child.value4\". Expected [leaf], got [leaf2].\n"  
					, e.getMessage());
		}
		
	}
	
	@Test
	public void testDifferentNumericTypes() {
		try {
			assertJsonEquals("{\"test\":1}", "{\n\"test\": 1.0\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent value found in node \"test\". Expected 1, got 1.0.\n", e.getMessage());
		}
	}
	
	@Test
	public void testDifferentType() {
		try {
			assertJsonEquals("{\"test\":1}", "{\n\"test\": \"something\"\n}");
			fail("Exception expected");
		} catch (AssertionError e) {
			assertEquals("JSON documents are different:\nDifferent types found in node \"test\". Expected NUMBER, got STRING.\n", e.getMessage());
		}
	}
	

}
