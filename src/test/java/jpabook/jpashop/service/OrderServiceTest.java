package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void order_item () throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("jpa", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("order status should be ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("order item count should be exactly same", 1, getOrder.getOrderItems().size());
        assertEquals("order price should be price * quantity", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("item stock should be decreased by order quantity", 8, book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void order_over_total_quantity() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("jpa", 10000, 10);

        int orderCount = 11;

        //when
        orderService.order(member.getId(), item.getId(), orderCount);

        //then
        fail("NotEnoughStockException should be occurred.");
    }

    @Test
    public void cancel_order() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("jpa", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);
        System.out.println("item stock : " + item.getStockQuantity());

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("status should be CANCEL when the order canceled.", OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals("stock should be increased when the order canceled.", 10, item.getStockQuantity());
    }

    private Item createBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("seoul", "street", "123-123"));
        em.persist(member);
        return member;
    }
}