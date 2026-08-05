package trchicuong.ezweb.services;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import trchicuong.ezweb.models.CartItem;
import trchicuong.ezweb.models.Product;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String SESSION_KEY = "cart";

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Product product) {
        List<CartItem> cart = getCart(session);
        boolean exists = cart.stream().anyMatch(i -> i.getProductId().equals(product.getId()));
        if (!exists) {
            cart.add(CartItem.builder()
                    .productId(product.getId())
                    .title(product.getTitle())
                    .thumbnail(product.getThumbnail())
                    .slug(product.getSlug())
                    .price(product.getEffectivePrice())
                    .build());
            session.setAttribute(SESSION_KEY, cart);
        }
    }

    public void removeFromCart(HttpSession session, Long productId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(i -> i.getProductId().equals(productId));
        session.setAttribute(SESSION_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }

    public int getCartCount(HttpSession session) {
        return getCart(session).size();
    }

    public long getCartTotal(HttpSession session) {
        return getCart(session).stream().mapToLong(CartItem::getPrice).sum();
    }

    public boolean isInCart(HttpSession session, Long productId) {
        return getCart(session).stream().anyMatch(i -> i.getProductId().equals(productId));
    }
}
