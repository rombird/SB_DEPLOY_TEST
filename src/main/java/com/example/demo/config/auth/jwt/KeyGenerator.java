package com.example.demo.config.auth.jwt;

import java.security.SecureRandom;

public class KeyGenerator {
    // 암호화 키 생성
    public static byte[] keyGen(){

        SecureRandom secureRandom = new SecureRandom(); // 난수값 생성
        byte[] keyBytes = new byte[256 / 8]; // 256비트 키 생성 (토큰에서 사용하는 키)
        secureRandom.nextBytes(keyBytes); // 난수로 바이트 배열 생성
        System.out.println("KeyGenerator getKeygen Key : " + keyBytes);
        return keyBytes;
    }
}
