package org.casestudy.trade.persistence.repository;

import org.casestudy.trade.persistence.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
    Optional<AssetEntity> findByIdCustomerNameAndIdAssetName(String customerName, String assetName);

    List<AssetEntity> findByIdCustomerName(String customerName);

}