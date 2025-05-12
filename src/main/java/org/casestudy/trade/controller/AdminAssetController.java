package org.casestudy.trade.controller;

import lombok.RequiredArgsConstructor;
import org.casestudy.trade.persistence.entity.AssetEntity;
import org.casestudy.trade.persistence.repository.AssetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/assets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssetController {

    private final AssetRepository assetRepository;

    @GetMapping("/{customerName}")
    public ResponseEntity<List<AssetEntity>> getAssetsByCustomer(@PathVariable String customerName) {
        List<AssetEntity> assets = assetRepository.findByIdCustomerName(customerName);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AssetEntity>> getAllAssets() {
        return ResponseEntity.ok(assetRepository.findAll());
    }
}

