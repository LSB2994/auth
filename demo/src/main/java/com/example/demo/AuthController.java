package com.example.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login"; // Render the login page (login.html)
    }

    @GetMapping("/home")
    public String home() {
        return "home"; // Render the home page after login
    }

//    @GetMapping("/register")
//    public String registerForm(Model model) {
//        model.addAttribute("user", new User());
//        return "register";
//    }

    @PostMapping("/register")
    public String register(User user) {
        System.out.println(user.getEmail());
        userService.register(user);
        return "hi";
    }

//    @GetMapping("/verify-otp")
//    public String verifyOtpForm(@RequestParam String email, Model model) {
//        model.addAttribute("email", email);
//        return "verify-otp";
//    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (userService.verifyOtp(email, otp)) {
            return "redirect:/login";
        }
        return "redirect:/verify-otp?error&email=" + email;
    }
}
