package com.bluestone.attendance;

import static org.junit.Assert.*;

import com.bluestone.attendance.utils.PasswordHasher;

import org.junit.Test;

public class PasswordHasherTest {

    @Test
    public void testHashAndVerify() {
        String password = "Blue@123";
        byte[] salt = PasswordHasher.generateSalt();
        assertNotNull(salt);
        assertEquals(16, salt.length);

        byte[] hash = PasswordHasher.hashPassword(password.toCharArray(), salt);
        assertNotNull(hash);
        assertEquals(32, hash.length); // 256 bits = 32 bytes

        // Correct password should verify
        assertTrue(PasswordHasher.verifyPassword("Blue@123".toCharArray(), salt, hash));

        // Incorrect password should fail
        assertFalse(PasswordHasher.verifyPassword("WrongPassword".toCharArray(), salt, hash));
        assertFalse(PasswordHasher.verifyPassword("blue@123".toCharArray(), salt, hash));
    }

    @Test
    public void testHexConversion() {
        byte[] original = new byte[]{0x12, 0x34, 0x56, (byte) 0xab, (byte) 0xef};
        String hex = PasswordHasher.toHex(original);
        assertEquals("123456abef", hex);

        byte[] decoded = PasswordHasher.fromHex(hex);
        assertArrayEquals(original, decoded);
    }
}
