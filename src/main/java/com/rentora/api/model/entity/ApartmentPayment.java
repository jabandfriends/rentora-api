package com.rentora.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "apartment_payment_methods")
@Data
public class ApartmentPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_name", nullable = false)
    private MethodType methodName;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false)
    private MethodType methodType = MethodType.bank_transfer;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "promptpay_number")
    private String promptpayNumber;

    @Column(name = "promptpay_qr_url")
    private String promptpayQrUrl;


    private String instructions;

    @Column(name = "is_active" )
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "id")
    private User createdBy;

    public enum MethodType {
      bank_transfer,promptpay,credit_card,cash,cheque
    }

}
