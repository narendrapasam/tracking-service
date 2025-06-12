package com.app.tracking.utility;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class SecureCodeGenerator {
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
   
	   public String generateRandomCode(int length) {
	        ThreadLocalRandom random = ThreadLocalRandom.current();
	        StringBuilder sb = new StringBuilder(length);
	        for (int i = 0; i < length; i++) {
	            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
	        }
	        return sb.toString();
	    }
}
