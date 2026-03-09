package com.securemarts.domain.invoice.repository;

import com.securemarts.domain.invoice.entity.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {

    List<InvoicePayment> findByInvoice_IdOrderByPaidAtAsc(Long invoiceId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM InvoicePayment p WHERE p.invoice.id = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") Long invoiceId);
}
