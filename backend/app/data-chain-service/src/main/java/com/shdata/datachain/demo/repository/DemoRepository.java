package com.shdata.datachain.demo.repository;

import com.shdata.datachain.demo.entity.DemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DemoRepository
        extends JpaRepository<DemoEntity, Long>, QuerydslPredicateExecutor<DemoEntity> {
}

