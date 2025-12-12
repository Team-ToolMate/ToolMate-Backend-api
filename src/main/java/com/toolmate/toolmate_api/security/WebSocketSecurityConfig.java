//package com.toolmate.toolmate_api.security;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
//import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.context.annotation.Bean;
//
//@Configuration
//@EnableWebSocketSecurity
//public class WebSocketSecurityConfig {
//
//    // Configure HTTP security to allow WebSocket handshake
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/ws/**").permitAll() // allow websocket endpoint
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**")); // disable CSRF for websocket
//        return http.build();
//    }
//
//    // Configure inbound messages (STOMP)
//    public void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//        messages
//                .simpDestMatchers("/app/**").authenticated()
//                .simpSubscribeDestMatchers("/topic/**", "/queue/**").authenticated()
//                .anyMessage().authenticated();
//    }
//}
