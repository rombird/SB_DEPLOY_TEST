package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<Signature, Byte> {
}
