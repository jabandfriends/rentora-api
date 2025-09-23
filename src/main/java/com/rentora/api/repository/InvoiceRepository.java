import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor {

    // @Query("SELECT i FROM Invoice i " +
    //        "JOIN i.units u " +
    //        "JOIN u.apartments a " +
    //        "JOIN a.contracts c " +
    //        "JOIN c.users t " +
    //        "WHERE au.user.id = :userId")
    // Page<Invoice> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    //Page<Invoice> getAllInvoices(UUID userId,Pageable pageable);

}
