package com.engineerbiz.service.repository;

import com.engineerbiz.service.entity.EngineeringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EngineeringServiceRepositoryTest {

    @Autowired
    private EngineeringServiceRepository repository;

    @Test
    void testSearchServices() {
        Page<EngineeringService> page = repository.searchServices(null, null, null, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertThat(page).isNotNull();
    }
}
