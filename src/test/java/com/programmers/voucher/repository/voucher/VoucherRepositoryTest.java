package com.programmers.voucher.repository.voucher;

import com.programmers.voucher.MysqlTestContainer;
import com.programmers.voucher.controller.customer.dto.CustomerCreateRequest;
import com.programmers.voucher.model.customer.Customer;
import com.programmers.voucher.model.voucher.FixedAmountVoucher;
import com.programmers.voucher.model.voucher.PercentDiscountVoucher;
import com.programmers.voucher.model.voucher.Voucher;
import com.programmers.voucher.repository.customer.CustomerRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VoucherRepositoryTest extends MysqlTestContainer {

    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Customer insertSingleCustomerData() {
        String email = "taehee@gmail.com";
        CustomerCreateRequest customerCreateRequest = new CustomerCreateRequest("taehee", email);
        customerRepository.save(customerCreateRequest);
        return customerRepository.findByEmail(email).get();
    }

    private Voucher insertSingleVoucherData() {
        Voucher voucher = getVoucher();
        return voucherRepository.save(voucher);
    }

    private void insertAllVouchersData() {
        for (Voucher voucher : getVouchers()) {
            voucherRepository.save(voucher);
        }
    }

    private Voucher getVoucher() {
        return new FixedAmountVoucher(UUID.randomUUID(), 5000);
    }

    private List<Voucher> getVouchers() {
        return List.of(
                new FixedAmountVoucher(UUID.randomUUID(), 5000),
                new PercentDiscountVoucher(UUID.randomUUID(), 40)
        );
    }

    @Test
    @Order(1)
    @DisplayName("데이터베이스에 바우처를 저장한다.")
    void save() {
        //given
        Voucher newVoucher = new FixedAmountVoucher(UUID.randomUUID(), 5000);

        //when
        Voucher result = voucherRepository.save(newVoucher);

        //then
        assertThat(result.getVoucherId())
                .isEqualTo(newVoucher.getVoucherId());
    }

    @Test
    @Order(2)
    @DisplayName("데이터베이스에서 모든 바우처 목록을 조회한다.")
    void findAll() {
        //given
        insertAllVouchersData();

        //when
        List<Voucher> result = voucherRepository.findAll();

        //then
        assertThat(result)
                .hasSize(3);
    }

    @Test
    @Order(3)
    @DisplayName("데이터베이스에 저장된 바우처를 고객에게 할당한다.")
    void assign() {
        //given
        Voucher voucher = insertSingleVoucherData();
        Customer customer = insertSingleCustomerData();
        voucher.setCustomer(customer);

        //when
        voucherRepository.assign(voucher);

        //then
        assertThat(voucherRepository.findAllByEmail("taehee@gmail.com"))
                .hasSize(1);
    }

    @Test
    @Order(4)
    @DisplayName("데이터베이스에서 해당 고객이 가진 모든 바우처 목록을 조회한다.")
    void findAllByEmail() {
        //given
        String email = "taehee@gmail.com";

        //when
        List<Voucher> result = voucherRepository.findAllByEmail(email);

        //then
        assertThat(result)
                .hasSize(1);
    }

    @Test
    @DisplayName("데이터베이스에서 바우처 아이디를 통해 조회한다.")
    void findById() {
        //given
        Voucher voucher = insertSingleVoucherData();

        //when
        Voucher result = voucherRepository.findById(voucher.getVoucherId()).get();

        //then
        assertThat(result.getVoucherId())
                .isEqualTo(voucher.getVoucherId());
    }

    @Test
    @DisplayName("데이터베이스에서 없는 바우처 아이디를 통해 조회 시 Optional empty를 반환한다.")
    void findByIdWhenNull() {
        //given

        //when
        Optional<Voucher> result = voucherRepository.findById(UUID.randomUUID());

        //then
        assertThat(result)
                .isEmpty();
    }

    @Test
    @DisplayName("데이터베이스에서 바우처 아이디를 통해 수정한다.")
    void update() {
        //given
        Voucher voucher = insertSingleVoucherData();
        Voucher updatedVoucher = new PercentDiscountVoucher(voucher.getVoucherId(), 30);

        //when
        voucherRepository.update(updatedVoucher);
        Voucher result = voucherRepository.findById(voucher.getVoucherId()).get();

        //then
        assertThat(result.getDiscountValue())
                .isEqualTo(30);
    }

    @Test
    @Order(5)
    @DisplayName("데이터베이스에서 모든 바우처 목록을 삭제한다.")
    void deleteALL() {
        //given

        //when
        voucherRepository.deleteAll();

        //then
        assertThat(voucherRepository.findAll())
                .isEmpty();
    }

    @Test
    @DisplayName("데이터베이스에서 없는 바우처 아이디를 통해 조회 시 예외를 발생시킨다.")
    void deleteByEmail() {
        assertThatThrownBy(() -> voucherRepository.deleteByEmail("hello@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
