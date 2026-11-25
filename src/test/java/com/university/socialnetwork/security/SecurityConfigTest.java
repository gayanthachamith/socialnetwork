package com.university.socialnetwork.security;

import com.university.socialnetwork.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestSecurityEndpointsController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpointsShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void anonymousRequestToProtectedEndpointShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/feed"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void studentShouldBeForbiddenFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@university.edu", roles = "ADMIN")
    void adminShouldAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }
}

@RestController
class TestSecurityEndpointsController {

    @GetMapping("/register")
    String register() {
        return "register";
    }

    @GetMapping("/feed")
    String feed() {
        return "feed";
    }

    @GetMapping("/admin/users")
    String adminUsers() {
        return "admin-users";
    }
}