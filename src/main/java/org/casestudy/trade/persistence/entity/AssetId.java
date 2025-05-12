package org.casestudy.trade.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetId implements Serializable {

    private String customerName;
    private String assetName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetId that)) return false;
        return Objects.equals(customerName, that.customerName) &&
                Objects.equals(assetName, that.assetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerName, assetName);
    }
}
