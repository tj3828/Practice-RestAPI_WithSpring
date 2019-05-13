package me.tj3828.restapiwithspring.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * @author tj3828
 */
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String username);
}

