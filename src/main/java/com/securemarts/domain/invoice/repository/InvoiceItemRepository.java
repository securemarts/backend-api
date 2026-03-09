package com.securemarts.domain.invoice.repository;

import com.securemarts.domain.invoice.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceIdOrderById(Long invoiceId);
}
