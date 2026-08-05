package trchicuong.ezweb.models;

import lombok.*;

import java.io.Serializable;

/** Session-scoped cart item (không lưu DB) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    private Long productId;
    private String title;
    private String thumbnail;
    private String slug;
    private Long price; // effective price at time of add
}
