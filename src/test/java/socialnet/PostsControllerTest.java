package socialnet;

import org.jetbrains.annotations.NotNull;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import socialnet.security.jwt.JwtUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = { PostsControllerTest.Initializer.class })
public class PostsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    private final String TEST_EMAIL = "user1@email.com";

    @ClassRule
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:12.14");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + container.getJdbcUrl(),
                "spring.datasource.username=" + container.getUsername(),
                "spring.datasource.password=" + container.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    private String getToken(String email) {
        return jwtUtils.generateJwtToken(email);
    }

    @Test
    @DisplayName("Загрузка контекста")
    @Transactional
    public void contextLoads() {
        assertThat(mockMvc).isNotNull();
        assertThat(jwtUtils).isNotNull();
    }

    @Test
    @DisplayName("Неавторизованный пользователь")
    @Transactional
    public void accessDenied() throws Exception {
        mockMvc
            .perform(get("/api/v1/post/1"))
            .andDo(print())
            .andExpect(unauthenticated())
            .andReturn();
    }

    @Test
    @DisplayName("Получение поста по существующему ID")
    @Transactional
    public void getPostByExistsId() throws Exception {
        mockMvc
            .perform(get("/api/v1/post/1").header("authorization", getToken(TEST_EMAIL)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andReturn();
    }

    @Test
    @DisplayName("Получение поста по несуществующему ID")
    @Transactional
    public void getPostByNotExistsId() throws Exception {
        /*mockMvc
            .perform(get("/api/v1/post/0").header("authorization", getToken(TEST_EMAIL)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityNotFoundException))
            .andExpect(result -> assertEquals("Post with id = 0 not found", result.getResolvedException().getMessage()));*/
    }
}
