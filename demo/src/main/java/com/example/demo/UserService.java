package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final int MAX_OTP = 1_000_000;
    private static final SecureRandom secureRandom = new SecureRandom();

    public User register(User user) {
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate OTP using SecureRandom
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        user.setEnabled(false);

        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
        System.out.println("User registered, OTP sent to: " + user.getEmail());
        return user;
    }

    private String generateOtp() {
        int otpValue = secureRandom.nextInt(MAX_OTP);
        return String.format("%06d", otpValue);
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByEmail(email));

        if (optionalUser.isEmpty()) {
            System.out.println("User not found for email: " + email);
            return false;
        }

        User user = optionalUser.get();

        // Check for expiration first to avoid unnecessary string comparisons
        if (LocalDateTime.now().isAfter(user.getOtpExpiration())) {
            System.out.println("OTP expired for user: " + email);
            return false;
        }

        if (!user.getOtp().equals(otp)) {
            System.out.println("Invalid OTP provided for user: " + email);
            return false;
        }

        // Clear OTP data and enable the user after successful verification
        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
        System.out.println("OTP verified for user: " + email);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), new ArrayList<>()
        );
    }
}
