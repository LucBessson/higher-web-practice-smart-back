package ru.yandex.practicum.smart.services;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.yandex.practicum.smart.domain.Feature;
import ru.yandex.practicum.smart.repositories.FeatureRepository;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicApiService {
    private final RequestMappingHandlerMapping handlerMapping;
    private final FeatureRepository featureRepository;
    private final DynamicSqlService sqlService;
    private final Map<Long, RequestMappingInfo> mappings = new ConcurrentHashMap<>();

    public DynamicApiService(
            RequestMappingHandlerMapping handlerMapping,
            FeatureRepository featureRepository,
            DynamicSqlService sqlService
    ) {
        this.handlerMapping = handlerMapping;
        this.featureRepository = featureRepository;
        this.sqlService = sqlService;
    }

    @PostConstruct
    public void registerSavedApis() {
        featureRepository.findByEnabledTrue().stream()
                .filter(f -> "API".equalsIgnoreCase(f.getType()))
                .forEach(this::register);
    }

    public void refresh() {
        mappings.values().forEach(handlerMapping::unregisterMapping);
        mappings.clear();
        registerSavedApis();
    }

    public void register(Feature feature) {
        if (!feature.isEnabled() || feature.getPath() == null || feature.getMethod() == null) return;

        RequestMethod requestMethod = RequestMethod.valueOf(feature.getMethod().toUpperCase(Locale.ROOT));
        RequestMappingInfo info = RequestMappingInfo.paths(feature.getPath())
                .methods(requestMethod)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .build();

        DynamicEndpoint endpoint = new DynamicEndpoint(feature, sqlService);
        handlerMapping.registerMapping(info, endpoint, DynamicEndpoint.HANDLER_METHOD);
        mappings.put(feature.getId(), info);
    }

    public static class DynamicEndpoint {
        public static final Method HANDLER_METHOD;

        static {
            try {
                HANDLER_METHOD = DynamicEndpoint.class.getMethod(
                        "handle", HttpServletRequest.class
                );
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private final Feature feature;
        private final DynamicSqlService sqlService;

        public DynamicEndpoint(Feature feature, DynamicSqlService sqlService) {
            this.feature = feature;
            this.sqlService = sqlService;
        }

        public ResponseEntity<?> handle(HttpServletRequest request) {
            if (feature.getCode() == null || feature.getCode().isBlank()) {
                return ResponseEntity.ok(Map.of(
                        "feature", feature.getName(),
                        "message", "Dynamic endpoint is registered"
                ));
            }

            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) params.put(key, values[0]);
            });

            try {
                return ResponseEntity.ok(sqlService.query(feature.getCode(), params));
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", e.getMessage(),
                        "featureId", feature.getId()
                ));
            }
        }
    }
}
