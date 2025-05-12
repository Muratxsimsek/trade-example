package org.casestudy.trade.controller;

import lombok.RequiredArgsConstructor;
import org.casestudy.trade.persistence.entity.AssetEntity;
import org.casestudy.trade.persistence.repository.AssetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer/assets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAssetController {

    private final AssetRepository assetRepository;

    @GetMapping
    public ResponseEntity<List<AssetEntity>> getCustomerAssets( @AuthenticationPrincipal UserDetails userDetails) {
        String customerName = userDetails.getUsername();
        List<AssetEntity> assets = assetRepository.findByIdCustomerName(customerName);
        return ResponseEntity.ok(assets);
    }
}

