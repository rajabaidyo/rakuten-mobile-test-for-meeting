package com.rakutenmobile.messageapi;

import com.rakutenmobile.messageapi.lib.profanity.DefaultImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
class MessageApiApplicationTests {

	@Autowired
	private DefaultImpl defaultProfanity;

	@Test
	void contextLoads() {
	}
	@Test
	void testProfanity() {
		assertTrue(defaultProfanity.check("harry potter is my favorite"));
		assertFalse(defaultProfanity.check("chamber is unknown"));
		assertFalse(defaultProfanity.check("my name is kenny"));
		assertTrue(defaultProfanity.check("my favorite film is chamber of secrets"));
	}

}
