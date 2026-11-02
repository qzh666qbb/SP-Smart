package com.example.training.physical.application.service;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.physical.domain.model.PhysicalTestBatch;
import com.example.training.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class LegacyBatchTest {
    @Test void defaultBatchFallbackIsUnambiguousAndDoesNotReopenClosedBatch() {
        var repository = mock(PhysicalTestRepository.class);
        var service = new PhysicalTestApplicationService(repository, null, null, null);
        when(repository.findAvailableBatches()).thenReturn(List.of(PhysicalTestBatch.builder().id(2L).build()));
        assertEquals(2L, service.resolveLegacyBatchId());
        when(repository.findAvailableBatches()).thenReturn(List.of());
        assertThrows(BusinessException.class, service::resolveLegacyBatchId);
        when(repository.findAvailableBatches()).thenReturn(List.of(PhysicalTestBatch.builder().id(1L).status("IN_PROGRESS").build(), PhysicalTestBatch.builder().id(3L).status("PENDING").build()));
        assertThrows(BusinessException.class, service::resolveLegacyBatchId);
        verify(repository, never()).saveBatch(any());
    }
}
