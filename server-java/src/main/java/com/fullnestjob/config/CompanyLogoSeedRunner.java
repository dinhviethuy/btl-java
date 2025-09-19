package com.fullnestjob.config;

import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.companies.repo.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URI;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Order(40)
public class CompanyLogoSeedRunner implements CommandLineRunner {
	private final CompanyRepository companyRepository;
    @Value("${server.port:8000}")
    private int serverPort;
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    @Value("${app.upload.base-url:}")
    private String uploadBaseUrlOverride;
    @Value("${app.admin.email:}")
    private String adminEmail;
    @Value("${app.admin.password:}")
    private String adminPassword;

	public CompanyLogoSeedRunner(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

    @Override
    public void run(String... args) throws Exception {
        boolean hasSpecificFlag = hasAny(args, "--seed-admin", "--seed.admin", "--seed-permissions", "--seed.permissions", "--seed-logos", "--seed.logos", "--seed-logos-direct", "--seed.logos.direct", "--seed-data", "--seed.data", "--seed-all", "--seed.all");
        // Chỉ chạy khi có cờ seed-logos/seed-logos-direct hoặc seed-all
        boolean shouldRun = hasAny(args, "--seed-logos", "--seed.logos", "--seed-logos-direct", "--seed.logos.direct", "--seed-all", "--seed.all");
        if (!shouldRun) return;
        boolean directOnly = hasAny(args, "--seed-logos-direct", "--seed.logos.direct");
        updateCompanyLogos(directOnly);
    }

    @Transactional
    public void updateCompanyLogos(boolean directOnly) throws IOException {
		Path appDir = Paths.get(System.getProperty("user.dir"));
		List<Path> candidates = List.of(
				appDir.resolve("data/logo"),
				appDir.getParent() != null ? appDir.getParent().resolve("data/logo") : appDir.resolve("data/logo")
		);
		Optional<Path> logoDirOpt = candidates.stream().filter(p -> Files.exists(p) && Files.isDirectory(p)).findFirst();
		if (logoDirOpt.isEmpty()) {
			System.out.println("CompanyLogoSeedRunner: data/logo not found");
			return;
		}
		Path logoDir = logoDirOpt.get();
        // Prepare upload base URL
        String ctx = (contextPath != null && !contextPath.isBlank()) ? contextPath : "";
        if (!ctx.isBlank() && !ctx.startsWith("/")) ctx = "/" + ctx;
        String baseUrl = uploadBaseUrlOverride != null && !uploadBaseUrlOverride.isBlank()
                ? trimTrailingSlash(uploadBaseUrlOverride)
                : ("http://localhost:" + serverPort + ctx);
        String uploadUrl = buildApiUrl(baseUrl, "/files/upload");
        String loginUrl = buildApiUrl(baseUrl, "/auth/login");
        RestTemplate restTemplate = new RestTemplate();
        String accessToken = null;
        if (!directOnly) {
            if (adminEmail != null && !adminEmail.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
                accessToken = fetchAccessToken(restTemplate, loginUrl, adminEmail, adminPassword);
                if (accessToken == null) {
                    System.out.println("CompanyLogoSeedRunner: failed to login admin for upload auth; will fallback to direct copy");
                }
            } else {
                System.out.println("CompanyLogoSeedRunner: missing admin creds; will fallback to direct copy");
            }
        }

		List<Company> companies = companyRepository.findAll();
		Map<String, Company> normalizedToCompany = companies.stream()
				.collect(Collectors.toMap(
						c -> normalize(c.getName()),
						c -> c,
						(a, b) -> a
				));

		int processed = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(logoDir, path -> Files.isRegularFile(path))) {
			for (Path file : stream) {
				String fn = file.getFileName().toString().toLowerCase();
				if (!(fn.endsWith(".png") || fn.endsWith(".jpg") || fn.endsWith(".jpeg"))) continue;
				String base = fn.replaceFirst("\\.[^.]+$", "");
				// remove trailing timestamps or ids after last dash
				int dash = base.indexOf('-');
				String keyCandidate = dash > -1 ? base.substring(0, dash) : base;
				String normalized = normalize(keyCandidate);
				Company c = normalizedToCompany.get(normalized);
				if (c == null) {
					// Try contains strategy
					for (Map.Entry<String, Company> e : normalizedToCompany.entrySet()) {
						if (normalized.contains(e.getKey()) || e.getKey().contains(normalized)) { c = e.getValue(); break; }
					}
				}
				if (c == null) continue;

                if (!directOnly && accessToken != null && !accessToken.isBlank()) {
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                        headers.add("folder_type", "company");
                        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

                        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                        body.add("fileUpload", new FileSystemResource(file.toFile()));

                        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                        ResponseEntity<java.util.Map> resp = restTemplate.postForEntity(uploadUrl, requestEntity, java.util.Map.class);
                        Object urlObj = resp.getBody() != null ? resp.getBody().get("url") : null;
                        if (urlObj instanceof String url && !url.isBlank()) {
                            c.setLogo(url);
                            companyRepository.save(c);
                            processed++;
                            continue;
                        }
                    } catch (Exception ex) {
                        System.out.println("CompanyLogoSeedRunner: upload failed for " + file + " - " + ex.getMessage() + "; fallback to direct copy");
                    }
                }
				// direct copy fallback or direct-only mode
                Path uploadsCompanyDir = appDir.resolve("uploads").resolve("company");
                Files.createDirectories(uploadsCompanyDir);
                String ext = fn.substring(fn.lastIndexOf('.'));
                String newFileName = java.util.UUID.randomUUID().toString() + ext;
                Path target = uploadsCompanyDir.resolve(newFileName);
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
				String url = buildApiUrl(baseUrl, "/files/company/" + newFileName);
                c.setLogo(url);
                companyRepository.save(c);
                processed++;
			}
		}
        // Normalize any leftover relative logos to absolute using base origin
        String origin = originFromBaseUrl(baseUrl);
        int normalized = 0;
        for (Company c : companies) {
            String logo = c.getLogo();
            if (logo != null && logo.startsWith("/")) {
                c.setLogo(origin + logo);
                companyRepository.save(c);
                normalized++;
            }
        }
        System.out.println("CompanyLogoSeedRunner: logos processed=" + processed + ", normalized=" + normalized + " from " + logoDir);
	}

	private static String normalize(String s) {
		if (s == null) return "";
		String lower = s.toLowerCase(Locale.ROOT).trim();
		// keep letters and digits only
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lower.length(); i++) {
			char ch = lower.charAt(i);
			if (Character.isLetterOrDigit(ch)) sb.append(ch);
		}
		return sb.toString();
	}

	private static boolean hasAny(String[] args, String... keys) {
		if (args == null || args.length == 0) return false;
		for (String a : args) {
			for (String k : keys) {
				if (k.equals(a)) return true;
			}
		}
		return false;
	}

    private static String trimTrailingSlash(String s) {
        if (s == null) return null;
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static String buildApiUrl(String baseUrl, String pathAfterV1) {
        String b = trimTrailingSlash(baseUrl);
        String p = pathAfterV1.startsWith("/") ? pathAfterV1 : ("/" + pathAfterV1);
        if (b != null && (b.endsWith("/v1") || b.endsWith("/v1/"))) {
            return b + p;
        }
        return b + "/v1" + p;
    }

    private static String originFromBaseUrl(String baseUrl) {
        try {
            URI u = new URI(baseUrl);
            String scheme = u.getScheme() != null ? u.getScheme() : "http";
            String host = u.getHost();
            int port = u.getPort();
            if (host == null) {
                // Fallback: in case of base URL without scheme
                return baseUrl.replaceAll("/+$", "");
            }
            return scheme + "://" + host + (port > -1 ? ":" + port : "");
        } catch (Exception e) {
            return baseUrl.replaceAll("/+$", "");
        }
    }

    private static String fetchAccessToken(RestTemplate restTemplate, String loginUrl, String email, String password) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("username", email);
            payload.put("password", password);
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> req = new org.springframework.http.HttpEntity<>(payload, headers);
            org.springframework.http.ResponseEntity<java.util.Map> resp = restTemplate.postForEntity(loginUrl, req, java.util.Map.class);
            Object token = resp.getBody() != null ? resp.getBody().get("access_token") : null;
            return token instanceof String ? (String) token : null;
        } catch (Exception e) {
            System.out.println("CompanyLogoSeedRunner: login failed - " + e.getMessage());
            return null;
        }
    }
}


