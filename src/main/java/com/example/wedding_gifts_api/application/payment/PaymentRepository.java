package com.example.wedding_gifts_api.application.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.wedding_gifts_api.core.domain.exceptions.common.MyException;
import com.example.wedding_gifts_api.core.domain.exceptions.payment.PaymentExecutionException;
import com.example.wedding_gifts_api.core.domain.exceptions.payment.PaymentNotFoundException;
import com.example.wedding_gifts_api.core.domain.model.Payment;
import com.example.wedding_gifts_api.core.domain.model.util.PaymentStatus;
import com.example.wedding_gifts_api.core.usecases.payment.IPaymentRepository;
import com.example.wedding_gifts_api.infra.dtos.payment.GetPaymentByPaidDTO;
import com.example.wedding_gifts_api.infra.jpa.JpaPaymentRepository;

@Repository
@SuppressWarnings("null")
public class PaymentRepository implements IPaymentRepository {

    @Autowired
    private JpaPaymentRepository jpaRepository;

    @Override
    public Payment savePayment(Payment payment) throws Exception {
        try {
            return jpaRepository.save(payment);
        } catch (Exception e) {
            throw new PaymentExecutionException("Payment can't be saved", e);
        }
    }

    @Override
    public Payment createPayment(Payment payment) throws Exception {
        try {
            return savePayment(payment);
        } catch (MyException e) {
            throw e;
        }
    }

    @Override
    public Payment paid(UUID id) throws Exception {
        try {
            Payment payment = getById(id);

            payment.setPaymentStatus(PaymentStatus.COMPLETE);
            payment.setPaid(LocalDateTime.now());

            return savePayment(payment);
        } catch (MyException e){
            throw e;
        } catch (Exception e) {
            throw new PaymentExecutionException("Paided can't be confirmed", e);
        }
    }

    @Override
    public void deletePayment(UUID id) throws Exception {
        try {
            Payment payment = getById(id);

            jpaRepository.delete(payment);
        } catch (MyException e){
            throw e;
        } catch (Exception e) {
            throw new PaymentExecutionException("Payment can't be deleted", e);
        }
    }

    @Override
    public void deleteAll(List<Payment> payments) throws Exception {
        try {
            jpaRepository.deleteAll(payments);
        } catch (Exception e) {
            throw new PaymentExecutionException("Payment can't be deleted", e);
        }
    }

    @Override
    public Payment getById(UUID id) throws Exception {
        return jpaRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(String.format("Payment with id %s not found", id.toString())));
    }

    @Override
    public Page<Payment> getAllPayments(UUID account, Pageable paging) {
        return jpaRepository.findByAccountId(account, paging);
    }

    @Override
    public Page<Payment> getByIsPaid(UUID account, GetPaymentByPaidDTO paidFilter, Pageable paging) {
        return jpaRepository.findByIsPaidAndAccountId(paidFilter.isPaid(), account, paging);
    }
    
}
