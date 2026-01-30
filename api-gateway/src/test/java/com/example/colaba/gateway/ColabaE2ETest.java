//package com.example.colaba.gateway;
//
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import io.restassured.response.Response;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.Network;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.utility.DockerImageName;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.greaterThan;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SuppressWarnings("resource")
//@Disabled
//public class ColabaE2ETest {
//
//    @LocalServerPort
//    private int port;
//
//    private static final Network network = Network.newNetwork();
//
//    private static final PostgreSQLContainer<?> postgres;
//    private static final GenericContainer<?> discovery;
//    private static final GenericContainer<?> config;
//    private static final GenericContainer<?> userService;
//    private static final GenericContainer<?> projectService;
//    private static final GenericContainer<?> taskService;
//    private static final GenericContainer<?> authService;
//
//    static {
//        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
//                .withNetwork(network)
//                .withNetworkAliases("postgres")
//                .withUsername("colaba_user")
//                .withPassword("colaba_password")
//                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2));
//
//        config = new GenericContainer<>(DockerImageName.parse("colaba-config-server:latest"))
//                .withNetwork(network)
//                .withNetworkAliases("config-server")
//                .withExposedPorts(8888)
//                .withEnv("EUREKA_CLIENT_ENABLED", "false")
//                .withEnv("CONFIG_REPO_URI", "https://github.com/COLABA-Tech/colaba-config-repo.git")
//                .withEnv("INTERNAL_API_KEY", "tvulOBWkyfz+TfDMFKWxiZxsBXy8ODfzqX+4TnNSQD+Z+ihYaNS4n2j+1ios3rRM")
//                .withEnv("JWT_SECRET", "7zzEDnrdXZsk1E1kEMCfc9Tklyc/UyCk8b1i+eH2+DRZCil6CwzROZv1tUD5G8Jew/7pHQuHOUhMAwn8ulfdDw==")
//                .withEnv("ADMIN_EMAIL", "admin@colaba.org")
//                .withEnv("ADMIN_PASSWORD_HASH", "$2a$10$/bKX7J/xipBe99m841P0i.P1sn5tXCVH3Lngu6N6ZDPjdiSopMDx.")
//                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));
//
//        discovery = new GenericContainer<>(DockerImageName.parse("colaba-discovery-server:latest"))
//                .withNetwork(network)
//                .withNetworkAliases("discovery-server")
//                .withExposedPorts(8761)
//                .withEnv("SERVER_PORT", "8761")
//                .withEnv("EUREKA_HOSTNAME", "discovery-server");
//
//        userService = new GenericContainer<>(DockerImageName.parse("colaba-user-service:latest"))
//                .withNetwork(network)
//                .withNetworkAliases("user-service")
//                .withExposedPorts(8081)
//                .withEnv("MANAGEMENT_PORT", "8081")
//                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/user_db")
//                .withEnv("SPRING_DATASOURCE_USERNAME", "colaba_user")
//                .withEnv("SPRING_DATASOURCE_PASSWORD", "colaba_password")
//                .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://discovery-server:8761/eureka")
//                .withEnv("SPRING_CLOUD_CONFIG_URI", "http://config-server:8888")
//                .dependsOn(postgres, config, discovery)
//                .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).forStatusCode(200));
//
//        projectService = new GenericContainer<>(DockerImageName.parse("colaba-project-service:latest"))
//                .withNetwork(network)
//                .withNetworkAliases("project-service")
//                .withExposedPorts(8081)
//                .withEnv("MANAGEMENT_PORT", "8081")
//                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/user_db")
//                .withEnv("SPRING_DATASOURCE_USERNAME", "colaba_user")
//                .withEnv("SPRING_DATASOURCE_PASSWORD", "colaba_password")
//                .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://discovery-server:8761/eureka")
//                .withEnv("SPRING_CLOUD_CONFIG_URI", "http://config-server:8888")
//                .dependsOn(postgres, config, discovery)
//                .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).forStatusCode(200));
//
//        taskService = new GenericContainer<>(DockerImageName.parse("colaba-task-service:latest"))
//                .withNetwork(network)
//                .withNetworkAliases("task-service")
//                .withExposedPorts(8081)
//                .withEnv("MANAGEMENT_PORT", "8081")
//                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/user_db")
//                .withEnv("SPRING_DATASOURCE_USERNAME", "colaba_user")
//                .withEnv("SPRING_DATASOURCE_PASSWORD", "colaba_password")
//                .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://discovery-server:8761/eureka")
//                .withEnv("SPRING_CLOUD_CONFIG_URI", "http://config-server:8888")
//                .dependsOn(postgres, config, discovery)
//                .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).forStatusCode(200));
//
//        authService = new GenericContainer<>(DockerImageName.parse("colaba-auth-service:latest"))
//                .withNetwork(network)
//                .withNetworkAliases("auth-service")
//                .withExposedPorts(8081)
//                .withEnv("MANAGEMENT_PORT", "8081")
//                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/user_db")
//                .withEnv("SPRING_DATASOURCE_USERNAME", "colaba_user")
//                .withEnv("SPRING_DATASOURCE_PASSWORD", "colaba_password")
//                .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://discovery-server:8761/eureka")
//                .withEnv("SPRING_CLOUD_CONFIG_URI", "http://config-server:8888")
//                .dependsOn(postgres, config, discovery)
//                .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).forStatusCode(200));
//
//        // Start in order
//        postgres.start();
//        config.start();
//        discovery.start();
//        userService.start();
//        projectService.start();
//        taskService.start();
//        authService.start();
//
//        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
//        System.setProperty("spring.datasource.username", postgres.getUsername());
//        System.setProperty("spring.datasource.password", postgres.getPassword());
//        System.setProperty("eureka.client.service-url.defaultZone",
//                "http://" + discovery.getHost() + ":" + discovery.getMappedPort(8761) + "/eureka");
//        System.setProperty("spring.cloud.config.uri",
//                "http://" + config.getHost() + ":" + config.getMappedPort(8888));
//    }
//
//    @BeforeEach
//    void init() {
//        RestAssured.baseURI = "http://localhost:" + port;
//    }
//
//    private String login() {
//        String loginPayload = "{\"username\": \"Admin\", \"password\": \"Admin123\"}";
//        Response loginResponse = given().contentType(ContentType.JSON).body(loginPayload).post("/auth/login");
//        return loginResponse.jsonPath().getString("token");
//    }
//
//    @Test
//    void testBasicWorkflow() {
//        String jwt = login();
//
//        // Создать проект
//        String projectPayload = "{\"name\": \"Test Project\", \"description\": \"E2E test\"}";
//        Response projectResponse = given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(projectPayload).post("/api/projects");
//        projectResponse.then().statusCode(201);
//        Long projectId = projectResponse.jsonPath().getLong("id");
//
//        // Создать таску
//        String taskPayload = "{\"title\": \"Test Task\", \"projectId\": " + projectId + ", \"description\": \"Task desc\"}";
//        Response taskResponse = given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(taskPayload).post("/api/tasks");
//        taskResponse.then().statusCode(201);
//        Long taskId = taskResponse.jsonPath().getLong("id");
//
//        // Создать комментарий
//        String commentPayload = "{\"text\": \"Test Comment\", \"taskId\": " + taskId + "}";
//        given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(commentPayload).post("/api/comments")
//                .then().statusCode(201);
//
//        // Проверка: Получить таску и убедиться в комментарии
//        given().header("Authorization", "Bearer " + jwt).get("/api/tasks/" + taskId)
//                .then().statusCode(200).body("comments.size()", equalTo(1)).body("comments[0].text", equalTo("Test Comment"));
//    }
//
//    @Test
//    void testUserManagement() {
//        String jwt = login();
//
//        // Получить всех пользователей (пагінація)
//        given().header("Authorization", "Bearer " + jwt).get("/api/users?page=0&size=10")
//                .then().statusCode(200).body("totalElements", greaterThan(0));
//
//        // Обновить пользователя (предполагаем ID=1 для теста)
//        String updateUserPayload = "{\"username\": \"updateduser\", \"email\": \"updated@example.com\"}";
//        given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(updateUserPayload).put("/api/users/1")
//                .then().statusCode(200).body("username", equalTo("updateduser"));
//    }
//
//    @Test
//    void testProjectManagement() {
//        String jwt = login();
//
//        // Создать проект
//        String projectPayload = "{\"name\": \"Proj2\", \"description\": \"Proj desc\"}";
//        Long projectId = given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(projectPayload).post("/api/projects")
//                .then().statusCode(201).extract().jsonPath().getLong("id");
//
//        // Добавить члена в проект
//        String memberPayload = "{\"userId\": 2, \"role\": \"EDITOR\"}";  // Предполагаем userId=2 существует
//        given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(memberPayload).post("/api/projects/" + projectId + "/members")
//                .then().statusCode(200);
//
//        // Создать тег
//        String tagPayload = "{\"name\": \"Tag1\", \"color\": \"#FF0000\", \"projectId\": " + projectId + "}";
//        Long tagId = given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(tagPayload).post("/api/tags")
//                .then().statusCode(201).extract().jsonPath().getLong("id");
//
//        // Присвоить тег таске (сначала создать таску)
//        String taskPayload = "{\"title\": \"Task2\", \"projectId\": " + projectId + "}";
//        Long taskId = given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(taskPayload).post("/api/tasks")
//                .then().statusCode(201).extract().jsonPath().getLong("id");
//
//        given().header("Authorization", "Bearer " + jwt).post("/api/task-tags/task/" + taskId + "/tag/" + tagId)
//                .then().statusCode(200);
//    }
//
//    @Test
//    void testCleanup() {
//        String jwt = login();
//
//        // Создать и удалить проект
//        String projectPayload = "{\"name\": \"ToDelete\", \"description\": \"Delete test\"}";
//        Long projectId = given().header("Authorization", "Bearer " + jwt)
//                .contentType(ContentType.JSON).body(projectPayload).post("/api/projects")
//                .then().statusCode(201).extract().jsonPath().getLong("id");
//
//        given().header("Authorization", "Bearer " + jwt).delete("/api/projects/" + projectId)
//                .then().statusCode(204);
//
//        // Проверка: проект не найден
//        given().header("Authorization", "Bearer " + jwt).get("/api/projects/" + projectId)
//                .then().statusCode(404);
//    }
//}
