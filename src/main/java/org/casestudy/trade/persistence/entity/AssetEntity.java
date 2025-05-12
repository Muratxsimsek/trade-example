package org.casestudy.trade.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ASSET")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetEntity {

    @EmbeddedId
    private AssetId id;

    @Min(value = 0)
    private int size;

    @Min(value = 0)
    private int usableSize;

    @Version
    private Long version;

    public AssetEntity(String customerName, String assetName, int size, int usableSize) {
        this.id = new AssetId(customerName, assetName);
        this.size = size;
        this.usableSize = usableSize;
    }

    public String getCustomerName() {
        return id.getCustomerName();
    }

    public String getAssetName() {
        return id.getAssetName();
    }

}

