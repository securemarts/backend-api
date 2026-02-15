package com.shopper.domain.pos.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.catalog.repository.ProductVariantRepository;
import com.shopper.domain.inventory.entity.InventoryItem;
import com.shopper.domain.inventory.entity.InventoryMovement;
import com.shopper.domain.inventory.repository.InventoryItemRepository;
import com.shopper.domain.inventory.repository.InventoryMovementRepository;
import com.shopper.domain.inventory.repository.LocationRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.onboarding.service.SubscriptionLimitsService;
import com.shopper.domain.pos.dto.*;
import com.shopper.domain.pos.entity.*;
import com.shopper.domain.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class POSService {

    private final POSRegisterRepository registerRepository;
    private final POSSessionRepository sessionRepository;
    private final OfflineTransactionRepository offlineTransactionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final SyncLogRepository syncLogRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public POSRegisterResponse createRegister(Long storeId, CreatePOSRegisterRequest request) {
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        var limits = subscriptionLimitsService.getLimitsForBusiness(store.getBusiness());
        if (limits.getMaxPosRegisters() == 0) {
            throw new BusinessRuleException("Your Pro trial has ended. Subscribe to Pro to use POS.");
        }
        if (registerRepository.countByStoreId(storeId) >= limits.getMaxPosRegisters()) {
            throw new BusinessRuleException("POS register limit reached for your plan (" + limits.getMaxPosRegisters() + "). Upgrade for more.");
        }
        POSRegister r = new POSRegister();
        r.setStoreId(storeId);
        r.setName(request.getName());
        r.setLocationId(request.getLocationId());
        r.setDeviceId(request.getDeviceId());
        r.setActive(request.isActive());
        r = registerRepository.save(r);
        return POSRegisterResponse.from(r);
    }

    @Transactional(readOnly = true)
    public List<POSRegisterResponse> listRegisters(Long storeId) {
        return registerRepository.findByStoreId(storeId).stream()
                .map(POSRegisterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public POSRegisterResponse getRegister(Long storeId, String registerPublicId) {
        POSRegister r = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        return POSRegisterResponse.from(r);
    }

    @Transactional
    public POSSessionResponse openSession(Long storeId, String registerPublicId, OpenSessionRequest request) {
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        if (!subscriptionLimitsService.isPosEnabled(subscriptionLimitsService.getEffectivePlan(store.getBusiness()))) {
            throw new BusinessRuleException("Your Pro trial has ended. Subscribe to Pro to use POS.");
        }
        POSRegister register = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        if (!register.isActive()) throw new BusinessRuleException("Register is not active");
        if (sessionRepository.findByRegisterIdAndStatus(register.getId(), POSSession.SessionStatus.OPEN).isPresent()) {
            throw new BusinessRuleException("Register already has an open session");
        }
        POSSession session = new POSSession();
        session.setRegisterId(register.getId());
        session.setOpeningCashAmount(request.getOpeningCashAmount() != null ? request.getOpeningCashAmount() : BigDecimal.ZERO);
        session.setOpenedBy(request.getOpenedBy());
        session = sessionRepository.save(session);
        CashMovement opening = new CashMovement();
        opening.setSessionId(session.getId());
        opening.setType(CashMovement.MovementType.OPENING);
        opening.setAmount(session.getOpeningCashAmount());
        cashMovementRepository.save(opening);
        POSSessionResponse resp = POSSessionResponse.from(session);
        resp.setRegisterId(register.getPublicId());
        return resp;
    }

    @Transactional
    public POSSessionResponse closeSession(Long storeId, String registerPublicId, String sessionPublicId, CloseSessionRequest request) {
        POSRegister register = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        POSSession session = sessionRepository.findByPublicId(sessionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSSession", sessionPublicId));
        if (!session.getRegisterId().equals(register.getId())) throw new ResourceNotFoundException("POSSession", sessionPublicId);
        if (session.getStatus() == POSSession.SessionStatus.CLOSED) throw new BusinessRuleException("Session already closed");
        session.setClosingCashAmount(request.getClosingCashAmount());
        session.setClosedAt(Instant.now());
        session.setStatus(POSSession.SessionStatus.CLOSED);
        session = sessionRepository.save(session);
        CashMovement closing = new CashMovement();
        closing.setSessionId(session.getId());
        closing.setType(CashMovement.MovementType.CLOSING);
        closing.setAmount(session.getClosingCashAmount() != null ? session.getClosingCashAmount() : BigDecimal.ZERO);
        cashMovementRepository.save(closing);
        POSSessionResponse resp = POSSessionResponse.from(session);
        resp.setRegisterId(register.getPublicId());
        return resp;
    }

    @Transactional(readOnly = true)
    public POSSessionResponse getCurrentSession(Long storeId, String registerPublicId) {
        POSRegister register = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        POSSession session = sessionRepository.findByRegisterIdAndStatus(register.getId(), POSSession.SessionStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("POSSession", "no open session"));
        POSSessionResponse resp = POSSessionResponse.from(session);
        resp.setRegisterId(register.getPublicId());
        return resp;
    }

    @Transactional
    public POSSyncResponse sync(Long storeId, String registerPublicId, POSSyncRequest request, String idempotencyKey) {
        POSRegister register = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        POSSession session = sessionRepository.findByRegisterIdAndStatus(register.getId(), POSSession.SessionStatus.OPEN)
                .orElseThrow(() -> new BusinessRuleException("No open session for this register"));
        List<SyncedTransactionDto> accepted = new ArrayList<>();
        List<ConflictDto> conflicts = new ArrayList<>();
        if (request.getTransactions() != null) {
            for (OfflineTransactionDto dto : request.getTransactions()) {
                Optional<OfflineTransaction> existing = offlineTransactionRepository.findByRegisterIdAndClientId(register.getId(), dto.getClientId());
                if (existing.isPresent()) {
                    accepted.add(SyncedTransactionDto.builder()
                            .clientId(dto.getClientId())
                            .publicId(existing.get().getPublicId())
                            .version(existing.get().getVersion())
                            .build());
                    continue;
                }
                try {
                    OfflineTransaction tx = new OfflineTransaction();
                    tx.setStoreId(storeId);
                    tx.setRegisterId(register.getId());
                    tx.setSessionId(session.getId());
                    tx.setClientId(dto.getClientId());
                    tx.setAmount(dto.getAmount());
                    tx.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "NGN");
                    tx.setClientCreatedAt(dto.getClientCreatedAt());
                    tx.setSyncedAt(Instant.now());
                    if (dto.getItems() != null) {
                        for (OfflineTransactionItemDto itemDto : dto.getItems()) {
                            var variant = productVariantRepository.findByPublicId(itemDto.getProductVariantPublicId())
                                    .orElseThrow(() -> new BusinessRuleException("Variant not found: " + itemDto.getProductVariantPublicId()));
                            OfflineTransactionItem item = new OfflineTransactionItem();
                            item.setOfflineTransaction(tx);
                            item.setProductVariantId(variant.getId());
                            item.setQuantity(itemDto.getQuantity());
                            item.setUnitPrice(itemDto.getUnitPrice());
                            item.setTotalPrice(itemDto.getTotalPrice());
                            tx.getItems().add(item);
                        }
                    }
                    tx = offlineTransactionRepository.save(tx);
                    deductInventoryForTransaction(storeId, register.getLocationId(), tx);
                    CashMovement sale = new CashMovement();
                    sale.setSessionId(session.getId());
                    sale.setType(CashMovement.MovementType.SALE);
                    sale.setAmount(tx.getAmount());
                    sale.setReason("POS sync: " + tx.getPublicId());
                    cashMovementRepository.save(sale);
                    accepted.add(SyncedTransactionDto.builder().clientId(dto.getClientId()).publicId(tx.getPublicId()).version(tx.getVersion()).build());
                } catch (Exception e) {
                    conflicts.add(ConflictDto.builder().clientId(dto.getClientId()).reason(e.getMessage()).build());
                }
            }
        }
        SyncLog log = new SyncLog();
        log.setStoreId(storeId);
        log.setRegisterId(register.getId());
        log.setClientSyncToken(request.getClientSyncToken());
        log.setServerSyncToken(UUID.randomUUID().toString());
        log.setConflictCount(conflicts.size());
        syncLogRepository.save(log);
        return POSSyncResponse.builder()
                .accepted(accepted)
                .conflicts(conflicts)
                .serverSyncToken(log.getServerSyncToken())
                .build();
    }

    private void deductInventoryForTransaction(Long storeId, Long preferredLocationId, OfflineTransaction tx) {
        for (OfflineTransactionItem line : tx.getItems()) {
            List<InventoryItem> candidates;
            if (preferredLocationId != null) {
                candidates = inventoryItemRepository.findByProductVariantIdAndLocationId(line.getProductVariantId(), preferredLocationId)
                        .map(List::of).orElse(Collections.emptyList());
            } else {
                candidates = inventoryItemRepository.findByStoreIdAndProductVariant_IdOrderByQuantityAvailableDesc(storeId, line.getProductVariantId());
            }
            if (candidates.isEmpty()) {
                candidates = inventoryItemRepository.findByStoreId(storeId).stream()
                        .filter(ii -> ii.getProductVariant().getId().equals(line.getProductVariantId()))
                        .toList();
            }
            InventoryItem item = candidates.stream().filter(ii -> ii.getQuantityAvailable() >= line.getQuantity()).findFirst()
                    .orElse(candidates.isEmpty() ? null : candidates.get(0));
            if (item == null) throw new BusinessRuleException("Insufficient inventory for variant " + line.getProductVariantId());
            if (item.getQuantityAvailable() < line.getQuantity()) throw new BusinessRuleException("Insufficient quantity for variant");
            item.setQuantityAvailable(item.getQuantityAvailable() - line.getQuantity());
            inventoryItemRepository.save(item);
            InventoryMovement mov = new InventoryMovement();
            mov.setInventoryItem(item);
            mov.setQuantityDelta(-line.getQuantity());
            mov.setMovementType(InventoryMovement.MovementType.SALE.name());
            mov.setReferenceType("POS_SYNC");
            mov.setReferenceId(tx.getPublicId());
            inventoryMovementRepository.save(mov);
        }
    }

    @Transactional(readOnly = true)
    public CashDrawerResponse getCashDrawer(Long storeId, String registerPublicId) {
        POSRegister register = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        var sessionOpt = sessionRepository.findByRegisterIdAndStatus(register.getId(), POSSession.SessionStatus.OPEN);
        if (sessionOpt.isEmpty()) {
            return CashDrawerResponse.builder().balance(BigDecimal.ZERO).sessionPublicId(null).status("CLOSED").build();
        }
        POSSession session = sessionOpt.get();
        List<CashMovement> movements = cashMovementRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        BigDecimal balance = movements.stream().map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return CashDrawerResponse.builder()
                .balance(balance)
                .sessionPublicId(session.getPublicId())
                .status(session.getStatus().name())
                .build();
    }

    @Transactional
    public void addCashMovement(Long storeId, String registerPublicId, CashMovementRequest request) {
        POSRegister register = registerRepository.findByStoreIdAndPublicId(storeId, registerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("POSRegister", registerPublicId));
        POSSession session = sessionRepository.findByRegisterIdAndStatus(register.getId(), POSSession.SessionStatus.OPEN)
                .orElseThrow(() -> new BusinessRuleException("No open session"));
        CashMovement.MovementType type = CashMovement.MovementType.valueOf(request.getType());
        if (type != CashMovement.MovementType.WITHDRAWAL && type != CashMovement.MovementType.DEPOSIT) {
            throw new BusinessRuleException("Only WITHDRAWAL or DEPOSIT allowed for reconciliation");
        }
        BigDecimal amount = request.getAmount();
        if (type == CashMovement.MovementType.WITHDRAWAL) amount = amount.negate();
        CashMovement mov = new CashMovement();
        mov.setSessionId(session.getId());
        mov.setType(type);
        mov.setAmount(amount);
        mov.setReason(request.getReason());
        cashMovementRepository.save(mov);
    }
}
