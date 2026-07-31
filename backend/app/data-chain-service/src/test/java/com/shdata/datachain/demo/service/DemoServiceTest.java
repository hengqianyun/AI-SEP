package com.shdata.datachain.demo.service;

import com.shdata.datachain.demo.entity.DemoEntity;
import com.shdata.datachain.demo.model.DemoRequest;
import com.shdata.datachain.demo.repository.DemoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private DemoRepository demoRepository;

    private DemoService demoService;

    @BeforeEach
    void setUp() {
        demoService = new DemoService(demoRepository);
    }

    @Test
    void shouldCreateDemo() {
        DemoRequest request = new DemoRequest();
        request.setName(" Demo ");
        request.setDescription(" Description ");

        when(demoRepository.save(any(DemoEntity.class))).thenAnswer(invocation -> {
            DemoEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        DemoEntity result = demoService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Demo");
        assertThat(result.getDescription()).isEqualTo("Description");
    }

    @Test
    void shouldGetDemo() {
        DemoEntity entity = DemoEntity.builder().id(1L).name("Demo").build();
        when(demoRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(demoService.get(1L)).isSameAs(entity);
    }
}

