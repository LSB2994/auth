package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5)); // OTP valid for 5 mins
        user.setEnabled(false);
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getOtp().equals(otp) || LocalDateTime.now().isAfter(user.getOtpExpiration())) {
            return false;
        }
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }
}
