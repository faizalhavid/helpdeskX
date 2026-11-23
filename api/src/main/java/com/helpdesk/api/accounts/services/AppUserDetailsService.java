package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.entities.AppUserDetails;
import com.helpdesk.api.accounts.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCaseAndIsDeletedFalse(username)
                .map(AppUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
