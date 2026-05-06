package com.demoform.web.controller;

import com.demoform.common.dto.PageResult;
import com.demoform.user.dto.UserVO;
import com.demoform.user.service.UserService;
import com.demoform.web.filter.JwtAuthFilter;
import com.demoform.web.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理控制器测试
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AuthenticationManager authenticationManager;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListUsers() throws Exception {
        UserVO vo = UserVO.builder()
                .id(1L).username("admin").email("admin@test.com")
                .status(1).roles(List.of("ROLE_ADMIN"))
                .createdAt(LocalDateTime.now()).build();
        when(userService.listUsers(anyInt(), anyInt(), any()))
                .thenReturn(PageResult.of(1, 1, 10, List.of(vo)));

        mockMvc.perform(get("/api/users?page=1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserDetail() throws Exception {
        UserVO vo = UserVO.builder()
                .id(1L).username("admin").email("admin@test.com")
                .status(1).roles(List.of("ROLE_ADMIN"))
                .createdAt(LocalDateTime.now()).build();
        when(userService.getUserDetail(1L)).thenReturn(vo);

        mockMvc.perform(get("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(delete("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAssignRoles() throws Exception {
        doNothing().when(userService).assignRoles(anyLong(), any());

        mockMvc.perform(put("/api/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
