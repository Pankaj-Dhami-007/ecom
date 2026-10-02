package com.dhami.authservice.security;

import com.dhami.authservice.entity.UserEntity;
import com.dhami.authservice.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserEntityRepository userEntityRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

      UserEntity dbUser =  userEntityRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException(" User not found "));
            return new UserPrincipal(dbUser);
    }
}
