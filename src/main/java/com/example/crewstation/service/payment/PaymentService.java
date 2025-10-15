package com.example.crewstation.service.payment;

import com.example.crewstation.domain.guest.GuestVO;
import com.example.crewstation.dto.payment.PaymentDTO;
import com.example.crewstation.dto.payment.status.PaymentCriteriaDTO;
import com.example.crewstation.dto.payment.status.PaymentStatusDTO;
import com.example.crewstation.util.Criteria;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface PaymentService {
    public Map<String, Object> requestPayment(PaymentStatusDTO paymentStatusDTO);

    public void completePayment(Long purchaseId, PaymentDTO paymentDTO);

    public List<PaymentCriteriaDTO> selectPayment(int page);

    public PaymentCriteriaDTO getPaymentDetail(Long id);




    default GuestVO toVO(PaymentStatusDTO  paymentStatusDTO) {
        return GuestVO.builder()
                .memberId(paymentStatusDTO.getMemberId())
                .guestPhone(paymentStatusDTO.getMemberPhone())
                .guestOrderNumber(paymentStatusDTO.getGuestOrderNumber())
                .addressDetail(paymentStatusDTO.getAddressDetail())
                .address(paymentStatusDTO.getAddress())
                .addressZipCode(paymentStatusDTO.getAddressZipCode())
                .build();
    }

}
