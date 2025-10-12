package com.rentora.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/monthly/invoices")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyInvoiceController {
}
